package com.yonga.auc.data.product;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.yonga.auc.common.PageWrapper;
import com.yonga.auc.data.category.CategoryRepository;

@Controller
class ProductController {

	@Autowired
    private ProductRepository productRepository;
	@Autowired
	private CategoryRepository categoryRepository;

	@GetMapping("/product")
	public String showProductList(Map<String, Object> model) {
		return showProductList(Optional.empty(), model, PageRequest.of(0, 10));
	}
    @GetMapping("/product/{categoryId}")
    public String showProductList(@PathVariable(value="categoryId", required=false) Optional<Integer> pathCategoryId, Map<String, Object> model, Pageable pageable) {
    	Page<Product> productList = null;
    	Integer categoryId = 0;
    	if (pathCategoryId.isPresent()) {
    		categoryId = pathCategoryId.get();
    	}
    	if (categoryId > 0) {
    		productList = this.productRepository.findProductByCategoryIdOrderByMakerAscTypeAscRatingAscProductNoAsc(categoryId, pageable);
    	}
    	if (productList != null) {
    		PageWrapper<Product> page = new PageWrapper<Product> (productList, "/product/" + categoryId);
    		model.put("page", page);
    	}
    	model.put("categoryList", this.categoryRepository.findCategory());
        return "product/product";
    }
    @GetMapping("/product/{categoryId}/{uketsukeNo}")
    public String showProductList(@PathVariable("categoryId") Integer categoryId, @PathVariable(value="uketsukeNo", required = false) String uketsukeNo, Map<String, Object> model, Pageable pageable) {
    	Page<Product> productList = null;
    	if (categoryId > 0) {
    		productList = this.productRepository.findProductByCategoryIdOrderByMakerAscTypeAscRatingAscProductNoAsc(categoryId, pageable);
    	}
    	if (productList != null) {
    		PageWrapper<Product> page = new PageWrapper<Product> (productList, "/product/" + categoryId);
    		model.put("page", page);
    	}
    	if (uketsukeNo != null) {
    		Product product = this.productRepository.findProductByUketsukeNo(uketsukeNo);
    		model.put("product", product);
    	}
    	model.put("categoryList", this.categoryRepository.findCategory());
        return "product/product";
    }
}
