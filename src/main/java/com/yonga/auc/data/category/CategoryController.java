package com.yonga.auc.data.category;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.extract.DataExtractWorker;
import com.yonga.auc.data.product.ProductRepository;
import com.yonga.auc.data.product.image.ProductImageRepository;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
class CategoryController {
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
	@Autowired
    private CategoryRepository categoryRepository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ProductImageRepository imageRepository;
	@Value("${app.brand-auc.uid}")
	private String uid;
	@Value("${app.brand-auc.pwd}")
	private String pwd;
	@Value("${app.brand-auc.executor}")
	private String executor;
	@Value("${app.brand-auc.targetProtocol}")
	private String targetProtocol;
	@Value("${app.brand-auc.targetHost}")
	private String targetHost;
	@Value("${app.brand-auc.work.path}")
	private String workPath;
	@Value("${app.brand-auc.exportPassword}")
	private String exportPassword;
	
    @GetMapping("/category")
    public String showVetList(Map<String, Object> model) {
        model.put("categoryList", this.categoryRepository.findAll());
        return "category/category";
    }
    
    @PatchMapping("/category/init")
    public @ResponseBody String extract(@RequestBody Category category) {
		System.out.println("try extract categoryId[" + category.getId() + "]");
		// check export password
		if (YongaUtil.isEmpty(category.getExportPassword())) {
			return "export password is empty";
		} else if (!category.getExportPassword().equals(this.exportPassword)) {
			return "different export password.";
		}
		Map<String, String> initializeInfoMap = new HashMap<>();
		initializeInfoMap.put("uid", this.uid);
		initializeInfoMap.put("pwd", this.pwd);
		initializeInfoMap.put("targetProtocol", targetProtocol);
		initializeInfoMap.put("targetHost", targetHost);
		initializeInfoMap.put("executor", this.executor);
		initializeInfoMap.put("extract_mode", "init");
		initializeInfoMap.put("work.dir", this.workPath);
		log.info("try data execute");
		EXECUTOR.submit(new DataExtractWorker(categoryRepository, productRepository, imageRepository, initializeInfoMap));
    	return "success";
    }
}
