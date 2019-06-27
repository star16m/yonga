package com.yonga.auc.data.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.config.ConfigConstants;
import com.yonga.auc.config.ConfigService;
import com.yonga.auc.data.category.detail.BrandRepository;
import com.yonga.auc.data.category.detail.KeijoRepository;
import com.yonga.auc.data.category.detail.MakerRepository;
import com.yonga.auc.data.extract.ExtractSiteInfo;
import com.yonga.auc.data.extract.worker.DataCleanWorker;
import com.yonga.auc.data.extract.worker.DataExtractWorker;
import com.yonga.auc.data.log.LogService;
import com.yonga.auc.data.product.ProductRepository;
import com.yonga.auc.data.product.ProductService;
import com.yonga.auc.data.product.image.ProductImageRepository;
import com.yonga.auc.mail.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Controller
@Slf4j
class CategoryController {
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
	@Autowired
    private CategoryService categoryService;
	@Autowired
	private ExtractSiteInfo siteInfo;
	@Autowired
	private LogService logService;
	@Autowired
	private ConfigService configService;
	@Autowired
	private MailService mailService;

	@Autowired
	private ProductService productService;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ProductImageRepository productImageRepository;
	@Autowired
	private MakerRepository makerRepository;
	@Autowired
	private BrandRepository brandRepository;
	@Autowired
	private KeijoRepository keijoRepository;
	@Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/category")
    public String getCategoryList(Map<String, Object> model) {
        model.put("categoryList", this.categoryService.findAll(null));
        return "category/category";
    }

    @PostConstruct
    private void initExtract() {
    	this.configService.setConfigValue("EXECUTOR", "STATUS", "NONE");
	}

	// 기동 이후 3분 이후에 실행
	// 이후 15분 마다 확인
	// TODO 확인할 것.
//	@Scheduled(initialDelay = 3 * 60 * 1000, fixedDelay = 15 * 60 * 1000)
    public void findNotCompleteCategory() {
		String executorStatus = this.configService.getConfigValue("EXECUTOR", "STATUS");
		if ("RUNNING".equals(executorStatus)) {
			// 실행 중인 경우 skip
			return;
		}
		List<Category> categoryList = this.categoryService.findNotCompleteCategory();
		if (categoryList == null || categoryList.isEmpty()) {
			// 대상 없음
			return;
		}
		log.info("완료되지 않은 {} 개의 카테고리에 대해서 추출을 재시도 합니다.", categoryList.size());
		extract(categoryList, ExtractMode.EXTRACT);
	}
    @PatchMapping("/category/init")
    public @ResponseBody String extract(@RequestBody Map<String, Object> optionMap) {
    	if (!optionMap.containsKey("categoryIdList")) {
    		return "categoryId is empty";
    	}
		if (!optionMap.containsKey("extractMode") || optionMap.get("extractMode").toString().isEmpty()) {
			return "extract mode is empty";
		}

		@SuppressWarnings("unchecked")
		List<String> categoryIdList = (List<String>)optionMap.get("categoryIdList");
		String extractModeString = optionMap.get("extractMode").toString();
		ExtractMode extractMode = ExtractMode.valueOf(extractModeString);

		log.info("try data execute");
		String executorStatus = this.configService.getConfigValue("EXECUTOR", "STATUS");
		if ("RUNNING".equals(executorStatus)) {
			return "실행 중입니다.";
		}
		List<Category> targetCategoryList = this.categoryService.findAll(null).stream().filter(category -> categoryIdList.contains(String.valueOf(category.getId()))).collect(Collectors.toList());
		extract(targetCategoryList, extractMode);
		return "success";
    }
    private void extract(List<Category> categoryList, ExtractMode extractMode) {
		categoryList.forEach(c -> {
			c.setStatus("EXTRACT_INIT");
			this.categoryService.save(c);
		});
		this.configService.setConfigValue("EXECUTOR", "STATUS", "RUNNING");
		if (extractMode.isRequiredInitialize()) EXECUTOR.submit(new DataCleanWorker(this.categoryService, this.productService, this.siteInfo, this.logService, categoryList));
		EXECUTOR.submit(new DataExtractWorker(this.categoryService, this.siteInfo, this.logService, this.configService, categoryList, extractMode, this.mailService,
				productRepository, productImageRepository, makerRepository, brandRepository, keijoRepository, objectMapper));
	}
}
