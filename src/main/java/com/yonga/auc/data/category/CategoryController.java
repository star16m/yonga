package com.yonga.auc.data.category;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yonga.auc.data.extract.ExtractSiteInfo;
import com.yonga.auc.data.extract.worker.DataCleanWorker;
import com.yonga.auc.data.extract.worker.DataExtractWorker;
import com.yonga.auc.data.log.LogService;
import com.yonga.auc.data.product.ProductService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
class CategoryController {
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
	@Autowired
    private CategoryService categoryService;
	@Autowired
	private ProductService productService;
	@Autowired
	private ExtractSiteInfo siteInfo;
	@Autowired
	private LogService logService;
    @GetMapping("/category")
    public String getCategoryList(Map<String, Object> model) {
        model.put("categoryList", this.categoryService.findAll());
        return "category/category";
    }
    
    @PatchMapping("/category/init")
    public @ResponseBody String extract(@RequestBody Map<String, Object> optionMap) {
    	if (!optionMap.containsKey("categoryIdList")) {
    		return "categoryId is empty";
    	}
    	if (!optionMap.containsKey("extractPassword")) {
    		return "export password is empty";
    	}
    	@SuppressWarnings("unchecked")
		List<String> categoryIdList = (List<String>)optionMap.get("categoryIdList");
    	String extractPassword = optionMap.get("extractPassword").toString();
    	
		// check export password
		if (!extractPassword.equals(this.siteInfo.getExportPassword())) {
			return "different export password.";
		}
		log.info("try data execute");
		List<Category> targetCategoryList = this.categoryService.findAll().stream().filter(category -> categoryIdList.contains(String.valueOf(category.getId()))).collect(Collectors.toList());
		EXECUTOR.submit(new DataCleanWorker(this.categoryService, this.productService, this.siteInfo, this.logService, targetCategoryList));
		EXECUTOR.submit(new DataExtractWorker(this.categoryService, this.productService, this.siteInfo, this.logService, targetCategoryList));
		
    	return "success";
    }
}
