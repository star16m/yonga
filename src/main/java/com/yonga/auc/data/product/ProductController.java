package com.yonga.auc.data.product;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yonga.auc.common.PageWrapper;
import com.yonga.auc.data.category.CategoryRepository;
import com.yonga.auc.data.log.LogService;
import com.yonga.auc.data.product.image.ProductImage;

import lombok.extern.slf4j.Slf4j;

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
		log.info("selects maker {}, type {}", optionMap.get("selectsMaker"), optionMap.get("selectsType"));
		session.setAttribute("selectsMaker", optionMap.get("selectsMaker"));
		session.setAttribute("selectsType", optionMap.get("selectsType"));
		return "success";
	}
	@GetMapping("/product")
	public String showProductList(HttpSession session, Map<String, Object> model) {
		// remove maker
		session.removeAttribute("selectsMaker");
		session.removeAttribute("selectsType");
		return showProductList(session, Optional.empty(), model, PageRequest.of(0, 10));
	}
    @GetMapping("/product/{categoryId}")
    public String showProductList(HttpSession session, @PathVariable(value="categoryId", required=false) Optional<Integer> pathCategoryId, Map<String, Object> model, Pageable pageable) {
    	// find model value
    	findModelValue(session, model, pathCategoryId, pageable);
        return "product/product";
    }
    @GetMapping("/product/{categoryId}/{uketsukeNo}")
    public String showProductList(HttpSession session, @PathVariable("categoryId") Integer categoryId, @PathVariable(value="uketsukeNo", required = false) String uketsukeNo, Map<String, Object> model, Pageable pageable) {
    	// find model value
    	findModelValue(session, model, Optional.of(categoryId), pageable);
    	// find product value
    	if (uketsukeNo != null) {
    		Product product = this.productService.findProductByUketsukeNo(uketsukeNo);
    		product.setImageList(product.getImageList().stream().sorted(Comparator.comparing(ProductImage::getName)).collect(Collectors.toList()));
    		model.put("product", product);
    	}
        return "product/product";
    }
    
    @SuppressWarnings("unchecked")
	private void findModelValue(HttpSession session, Map<String, Object> model, Optional<Integer> categoryId, Pageable pageable) {
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
    		typeInfo.stream().forEach(e-> {
    			log.info("Type for Category[{}], [{}]", categoryId.get(), e.get("type"));
    		});
    		if (!session.isNew() && session.getAttribute("selectsType") != null) {
    			log.debug("maker is {}", session.getAttribute("selectsType"));
    			selectsTypeList = (List<String>) session.getAttribute("selectsType");
    		} else {
    			selectsTypeList = typeInfo.stream().map(i -> i.get("type").toString()).collect(Collectors.toList());
    		}
    		// product list
    		Page<Product> productList = this.productService.findProductList(categoryId.get(), selectsMakerList, selectsTypeList, pageable);
    		PageWrapper<Product> page = new PageWrapper<Product> (productList, "/product/" + categoryId.get());
    		model.put("page", page);
    	}
    }
}