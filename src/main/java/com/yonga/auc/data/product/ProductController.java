package com.yonga.auc.data.product;

import com.yonga.auc.common.PageWrapper;
import com.yonga.auc.data.category.CategoryRepository;
import com.yonga.auc.data.log.LogService;
import com.yonga.auc.data.product.image.ProductImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
	public @ResponseBody String selectsMaker(HttpSession session, @RequestBody Map<String, Object> optionMap) {
		log.info("selects maker [{}], type [{}], keijo [{}]", optionMap.get("selectsMaker"), optionMap.get("selectsType"), optionMap.get("selectsKeijo"));
		session.setAttribute("selectsMaker", optionMap.get("selectsMaker"));
		session.setAttribute("selectsType", optionMap.get("selectsType"));
		session.setAttribute("selectsKeijo", optionMap.get("selectsKeijo"));
		session.setAttribute("viewProductImage", optionMap.get("viewProductImage"));
		return "success";
	}
	@GetMapping("/product")
	public String showProductList(HttpSession session, Map<String, Object> model) {
		// remove maker
		session.removeAttribute("selectsMaker");
		session.removeAttribute("selectsType");
		session.removeAttribute("selectsKeijo");
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
		Product product = null;
		if (uketsukeNo != null) {
			// find product value
			product = this.productService.findProductByUketsukeNo(uketsukeNo);
			if (product != null) {
				product.setImageList(product.getImageList().stream().sorted(Comparator.comparing(ProductImage::getName)).collect(Collectors.toList()));
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
    		model.put("currentCategory", this.categoryRepository.getOne(categoryId.get()));
    		// maker list
    		List<Map<String, Object>> makerInfo = this.productService.findProductMaker(categoryId.get());
    		List<String> selectsMakerList = null;
    		model.put("makerList", makerInfo);
    		if (!session.isNew() && session.getAttribute("selectsMaker") != null) {
    			log.debug("maker is {}", session.getAttribute("selectsMaker"));
    			selectsMakerList = (List<String>) session.getAttribute("selectsMaker");
    		} else {
    			selectsMakerList = makerInfo.stream().map(i -> i.get("maker").toString()).collect(Collectors.toList());
    		}
    		// type list
    		List<Map<String, Object>> typeInfo = this.productService.findProductType(categoryId.get());
    		List<String> selectsTypeList = null;
    		model.put("typeList", typeInfo);
    		if (!session.isNew() && session.getAttribute("selectsType") != null) {
    			log.debug("maker is {}", session.getAttribute("selectsType"));
    			selectsTypeList = (List<String>) session.getAttribute("selectsType");
    		} else {
    			selectsTypeList = typeInfo.stream().map(i -> i.get("type").toString()).collect(Collectors.toList());
    		}
    		// keijo list
			List<Map<String, Object>> keijoInfo = this.productService.findProductKeijo(categoryId.get());
    		List<String> selectsKeijoList = null;
			model.put("keijoList", keijoInfo);
			if (!session.isNew() && session.getAttribute("selectsKeijo") != null) {
				log.debug("keijo is {}", session.getAttribute("selectsKeijo"));
				selectsKeijoList = (List<String>) session.getAttribute("selectsKeijo");
			} else {
				selectsKeijoList = keijoInfo.stream().map(i -> i.get("keijo").toString()).collect(Collectors.toList());
			}
    		// product list
    		Page<Product> productPage = this.productService.findProductList(categoryId.get(), selectsMakerList, selectsTypeList, selectsKeijoList, pageable);
    		PageWrapper<Product> page = new PageWrapper<> (productPage, "/product/" + categoryId.get());
    		model.put("page", page);
    		// left & right product
			if (product != null) {
				List<Product> contents = productPage.getContent();
				int currentProductIndex = -1;
				for (int i = 0; i < contents.size(); i++) {
					if (contents.get(i).getUketsukeNo().equals(product.getUketsukeNo())) {
						currentProductIndex = i;
						break;
					}
				}
				if (currentProductIndex > -1) {
					// founded product
					String leftProduct = currentProductIndex == 0 ? null : contents.get(currentProductIndex - 1).getUketsukeNo();
					String rightProduct = currentProductIndex == contents.size() - 1 ? null : contents.get(currentProductIndex + 1).getUketsukeNo();
					Integer previousPage = productPage.getPageable().getPageNumber();
					Integer nextPage = productPage.getPageable().getPageNumber();
					// left 가 없는 경우
					if (leftProduct == null) {
						if (!productPage.getPageable().hasPrevious()) {
							// first page
						} else {
							Page<Product> previousProductPage = this.productService.findProductList(categoryId.get(), selectsMakerList, selectsTypeList, selectsKeijoList, pageable.previousOrFirst());
							Product previousProduct = previousProductPage.getContent().get(pageable.getPageSize() - 1);
							leftProduct = previousProduct.getUketsukeNo();
							previousPage = previousProductPage.getPageable().getPageNumber();
						}
					}
					// right 가 없는 경우
					if (rightProduct == null) {
						if (productPage.getTotalPages() <= productPage.getPageable().next().getPageNumber()) {
							// last page
						} else {
							Page<Product> nextProductPage = this.productService.findProductList(categoryId.get(), selectsMakerList, selectsTypeList, selectsKeijoList, productPage.getPageable().next());
							Product nextProduct = nextProductPage.getContent().get(0);
							rightProduct = nextProduct.getUketsukeNo();
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