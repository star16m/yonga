package com.yonga.auc.data.extract.worker;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.category.CategoryService;
import com.yonga.auc.data.extract.ExtractSiteInfo;
import com.yonga.auc.data.log.LogService;
import com.yonga.auc.data.product.ProductService;

public class DataCleanWorker implements Callable<Void> {
	private CategoryService categoryService;
	private ProductService productService;
	private ExtractSiteInfo siteInfo;
	private LogService logService;
	private List<Category> targetCategoryList;
	public DataCleanWorker(CategoryService categoryService, ProductService productService, ExtractSiteInfo siteInfo, LogService logService, List<Category> targetCategoryList) {
		this.categoryService = categoryService;
		this.productService = productService;
		this.siteInfo = siteInfo;
		this.logService = logService;
		this.targetCategoryList = targetCategoryList;
	}
	@Override
	public Void call() {
		this.targetCategoryList.parallelStream().forEach(category -> {
			this.categoryService.clean(category);
			this.productService.deleteAll(category);
		});
		YongaUtil.cleanDirectory(Paths.get(this.siteInfo.getWorkRoot(), siteInfo.getWorkPathList()));
		YongaUtil.cleanDirectory(Paths.get(this.siteInfo.getWorkRoot(), siteInfo.getWorkPathDetail()));
		this.logService.addLog("디렉토리를 초기화 하였습니다.");
		return null;
	}
}
