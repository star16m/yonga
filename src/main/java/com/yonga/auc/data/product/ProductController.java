package com.yonga.auc.data.product;

import com.yonga.auc.common.PageWrapper;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.category.CategoryRepository;
import com.yonga.auc.data.category.detail.Brand;
import com.yonga.auc.data.category.detail.Keijo;
import com.yonga.auc.data.category.detail.Maker;
import com.yonga.auc.data.log.LogService;
import com.yonga.auc.data.product2.NewProduct;
import com.yonga.auc.data.product2.NewProductRepository;
import com.yonga.auc.data.product2.NewProductService;
import com.yonga.auc.data.product2.ProductSearchOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
class ProductController {

	@Autowired
	private NewProductService newProductService;
	@Autowired
	private NewProductRepository newProductRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private LogService logService;
	@PatchMapping("/selects/options")
	public @ResponseBody String selectsMaker(HttpSession session, @RequestBody ProductSearchOption productSearchOption) {
		log.info("selects option [{}]", productSearchOption);
		session.setAttribute("selectsOption", productSearchOption);
//
//		session.setAttribute("selectsMaker", (List<Integer>)optionMap.get("selectsMaker"));
//		session.setAttribute("selectsBrand", (List<Integer>)optionMap.get("selectsBrand"));
//		session.setAttribute("selectsKeijo", (List<Integer>)optionMap.get("selectsKeijo"));
//		session.setAttribute("viewProductImage", optionMap.get("viewProductImage"));
		return "success";
	}
	@GetMapping("/product")
	public String showProductList(HttpSession session, Map<String, Object> model) {
		// remove maker
		session.removeAttribute("selectsOption");
//		session.removeAttribute("selectsType");
//		session.removeAttribute("selectsType");
//		session.removeAttribute("selectsKeijo");
//		session.removeAttribute("viewProductImage");
		return showProductList(session, Optional.empty(), model, PageRequest.of(0, 10));
	}
    @GetMapping("/product/{categoryId}")
    public String showProductList(HttpSession session, @PathVariable(value="categoryId", required=false) Optional<Integer> pathCategoryId, Map<String, Object> model, Pageable pageable) {
    	// find model value
    	findModelValue(session, model, pathCategoryId, pageable, null);
//    	if (true && pathCategoryId.isPresent()) {
//    		log.info("전체 제품 갯수 : {}", this.productService.findAllProductNum());
//    		log.info("카테고리 없는 것 : {}", this.productService.findCategoryIdNull());
//    		log.info("제품 갯수 : {}", this.productService.findProductNum(pathCategoryId.get()));
//		}
        return "product/product";
    }
    @GetMapping("/product/{categoryId}/{uketsukeNo}")
    public String showProductList(HttpSession session, @PathVariable("categoryId") Integer categoryId, @PathVariable(value="uketsukeNo", required = false) String uketsukeNo, Map<String, Object> model, Pageable pageable) {
		NewProduct product = null;
		if (uketsukeNo != null) {
			// find product value
			product = this.newProductService.findNewProductByUketsukeNo(uketsukeNo);
			if (product != null) {
//				product.setImageList(product.getImageList().stream().sorted(Comparator.comparing(ProductImage::getName)).collect(Collectors.toList()));
				model.put("product", product);
			}
		}
		// find model value
		findModelValue(session, model, Optional.of(categoryId), pageable, product);
		return "product/product";
    }
    
    @SuppressWarnings("unchecked")
	private void findModelValue(HttpSession session, Map<String, Object> model, Optional<Integer> categoryId, Pageable pageable, NewProduct product) {
    	// category list
    	model.put("categoryList", this.categoryRepository.findCategory());
    	if (categoryId.isPresent() && categoryId.get() > 0) {
    		// currentCategory value
			Category currentCategory = this.categoryRepository.getOne(categoryId.get());
    		model.put("currentCategory", currentCategory);
    		// maker list
    		List<Maker> makerInfo = this.newProductService.findMaker(currentCategory);
			ProductSearchOption searchOption = null;
			if (session.getAttribute("selectsOption") != null) {
				searchOption = (ProductSearchOption) session.getAttribute("selectsOption");
			}
    		List<Integer> selectsMakerList = null;
    		model.put("makerList", makerInfo);
    		if (!session.isNew() && searchOption != null && searchOption.getSelectsMaker() != null) {
    			selectsMakerList = searchOption.getSelectsMaker();
    		} else {
    			selectsMakerList = makerInfo.stream().map(i -> i.getMakerCd()).collect(Collectors.toList());
    		}
    		// brand type list
    		List<Brand> brandInfo = this.newProductService.findBrand(currentCategory);
    		List<Integer> selectsBrandList = null;
    		model.put("brandList", brandInfo);
    		if (!session.isNew() && searchOption != null && searchOption.getSelectsBrand() != null) {
				selectsBrandList = searchOption.getSelectsBrand();
    		} else {
				selectsBrandList = brandInfo.stream().map(i -> i.getBrandCd()).collect(Collectors.toList());
    		}
    		// keijo list
			List<Keijo> keijoInfo = this.newProductService.findKeijo(currentCategory);
    		List<Integer> selectsKeijoList = null;
			model.put("keijoList", keijoInfo);
			if (!session.isNew() && searchOption != null && searchOption.getSelectsKeijo() != null) {
				selectsKeijoList = searchOption.getSelectsKeijo();
			} else {
				selectsKeijoList = keijoInfo.stream().map(i -> i.getKeijoCd()).collect(Collectors.toList());
			}
    		// product list
    		Page<NewProduct> productPage = this.newProductService.findProductList(categoryId.get(), selectsMakerList, selectsBrandList, selectsKeijoList, pageable);
    		PageWrapper<NewProduct> page = new PageWrapper<> (productPage, "/product/" + categoryId.get());
    		model.put("page", page);
    		// left & right product
			if (product != null) {
				List<NewProduct> contents = productPage.getContent();
				int currentProductIndex = -1;
				for (int i = 0; i < contents.size(); i++) {
					if (contents.get(i).getUketsukeBng().equals(product.getUketsukeBng())) {
						currentProductIndex = i;
						break;
					}
				}
				if (currentProductIndex > -1) {
					// founded product
					String leftProduct = currentProductIndex == 0 ? null : contents.get(currentProductIndex - 1).getUketsukeBng();
					String rightProduct = currentProductIndex == contents.size() - 1 ? null : contents.get(currentProductIndex + 1).getUketsukeBng();
					Integer previousPage = productPage.getPageable().getPageNumber();
					Integer nextPage = productPage.getPageable().getPageNumber();
					// left 가 없는 경우
					if (leftProduct == null) {
						if (!productPage.getPageable().hasPrevious()) {
							// first page
						} else {
							Page<NewProduct> previousProductPage = this.newProductService.findProductList(categoryId.get(), selectsMakerList, selectsBrandList, selectsKeijoList, pageable.previousOrFirst());
							NewProduct previousProduct = previousProductPage.getContent().get(pageable.getPageSize() - 1);
							leftProduct = previousProduct.getUketsukeBng();
							previousPage = previousProductPage.getPageable().getPageNumber();
						}
					}
					// right 가 없는 경우
					if (rightProduct == null) {
						if (productPage.getTotalPages() <= productPage.getPageable().next().getPageNumber()) {
							// last page
						} else {
							Page<NewProduct> nextProductPage = this.newProductService.findProductList(categoryId.get(), selectsMakerList, selectsBrandList, selectsKeijoList, productPage.getPageable().next());
							NewProduct nextProduct = nextProductPage.getContent().get(0);
							rightProduct = nextProduct.getUketsukeBng();
							nextPage = nextProductPage.getPageable().getPageNumber();
						}
					}
					model.put("previousProduct", leftProduct);
					model.put("nextProduct", rightProduct);
					model.put("previousPage", previousPage);
					model.put("nextPage", nextPage);
				}
			}
    	}
    }
}