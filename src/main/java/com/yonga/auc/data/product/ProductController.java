package com.yonga.auc.data.product;

import com.yonga.auc.common.PageWrapper;
import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.category.CategoryRepository;
import com.yonga.auc.data.category.detail.Brand;
import com.yonga.auc.data.category.detail.Keijo;
import com.yonga.auc.data.category.detail.Maker;
import com.yonga.auc.data.log.LogService;
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
	private ProductService productService;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private LogService logService;
	@PatchMapping("/selects/options")
	public @ResponseBody String selectsMaker(HttpSession session, @RequestBody ProductSearchOption productSearchOption) {
		log.info("selects option [{}]", productSearchOption);
		session.setAttribute("selectsOption", productSearchOption);
		return "success";
	}
	@GetMapping("/product")
	public String showProductList(HttpSession session, Map<String, Object> model) {
		return showProductList(session, Optional.empty(), model, PageRequest.of(0, 10));
	}
    @GetMapping("/product/{categoryId}")
    public String showProductList(HttpSession session, @PathVariable(value="categoryId", required=false) Optional<Integer> pathCategoryId, Map<String, Object> model, Pageable pageable) {
    	// find model value
    	findModelValue(session, model, pathCategoryId, pageable, null);
        return "product/product";
    }
    @GetMapping("/product/{categoryId}/{uketsukeNo}")
    public String showProductList(HttpSession session, @PathVariable("categoryId") Integer categoryId, @PathVariable(value="uketsukeNo", required = false) String uketsukeNo, Map<String, Object> model, Pageable pageable) {
		Product product = null;
		if (uketsukeNo != null) {
			// find product value
			product = this.productService.findNewProductByUketsukeNo(uketsukeNo);
			if (product != null) {
				model.put("product", product);
			}
		}
		// find model value
		findModelValue(session, model, Optional.of(categoryId), pageable, product);
		return "product/product";
    }
    
    @SuppressWarnings("unchecked")
	private void findModelValue(HttpSession session, Map<String, Object> model, Optional<Integer> categoryId, Pageable pageable, Product product) {
    	// category list
    	model.put("categoryList", this.categoryRepository.findCategory());
    	if (categoryId.isPresent() && categoryId.get() > 0) {
    		// currentCategory value
			Category currentCategory = this.categoryRepository.getOne(categoryId.get());
    		model.put("currentCategory", currentCategory);
    		// maker list
    		List<Maker> makerInfo = this.productService.findMaker(currentCategory);
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
    		List<Brand> brandInfo = this.productService.findBrand(currentCategory);
    		List<Integer> selectsBrandList = null;
    		model.put("brandList", brandInfo);
    		if (!session.isNew() && searchOption != null && searchOption.getSelectsBrand() != null) {
				selectsBrandList = searchOption.getSelectsBrand();
    		} else {
				selectsBrandList = brandInfo.stream().map(i -> i.getBrandCd()).collect(Collectors.toList());
    		}
    		// keijo list
			List<Keijo> keijoInfo = this.productService.findKeijo(currentCategory);
    		List<Integer> selectsKeijoList = null;
			model.put("keijoList", keijoInfo);
			if (!session.isNew() && searchOption != null && searchOption.getSelectsKeijo() != null) {
				selectsKeijoList = searchOption.getSelectsKeijo();
			} else {
				selectsKeijoList = keijoInfo.stream().map(i -> i.getKeijoCd()).collect(Collectors.toList());
			}
    		// product list
    		Page<Product> productPage = this.productService.findProductList(categoryId.get(), selectsMakerList, selectsBrandList, selectsKeijoList, pageable);
    		PageWrapper<Product> page = new PageWrapper<> (productPage, "/product/" + categoryId.get());
    		model.put("page", page);
    		// left & right product
			if (product != null) {
				List<Product> contents = productPage.getContent();
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
							Page<Product> previousProductPage = this.productService.findProductList(categoryId.get(), selectsMakerList, selectsBrandList, selectsKeijoList, pageable.previousOrFirst());
							Product previousProduct = previousProductPage.getContent().get(pageable.getPageSize() - 1);
							leftProduct = previousProduct.getUketsukeBng();
							previousPage = previousProductPage.getPageable().getPageNumber();
						}
					}
					// right 가 없는 경우
					if (rightProduct == null) {
						if (productPage.getTotalPages() <= productPage.getPageable().next().getPageNumber()) {
							// last page
						} else {
							Page<Product> nextProductPage = this.productService.findProductList(categoryId.get(), selectsMakerList, selectsBrandList, selectsKeijoList, productPage.getPageable().next());
							Product nextProduct = nextProductPage.getContent().get(0);
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