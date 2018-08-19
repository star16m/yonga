package com.yonga.auc.data.extract.worker;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.category.CategoryService;
import com.yonga.auc.data.extract.DataExtractException;
import com.yonga.auc.data.extract.DataExtractor;
import com.yonga.auc.data.extract.DataExtractorUtil;
import com.yonga.auc.data.extract.ExtractSiteInfo;
import com.yonga.auc.data.log.LogService;
import com.yonga.auc.data.product.Product;
import com.yonga.auc.data.product.ProductService;
import com.yonga.auc.data.product.image.ProductImage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataExtractWorker implements Callable<Boolean> {

	private CategoryService categoryService;
	private ProductService productService;
	private ExtractSiteInfo siteInfo;
	private DataExtractor dataExtractor;
	private LogService logService;
	private List<Category> targetCategoryList;

	public DataExtractWorker(CategoryService categoryService, ProductService productService, ExtractSiteInfo siteInfo, LogService logService, List<Category> targetCategoryList) {
		this.categoryService = categoryService;
		this.productService = productService;
		this.siteInfo = siteInfo;
		this.logService = logService;
		this.targetCategoryList = targetCategoryList;
	}
	
	@Override
	public Boolean call() {
		this.dataExtractor = new DataExtractor(siteInfo);
		boolean extractedProductList = true;
		try {
			// extract
			extractedProductList &= this.dataExtractor.extractProductList(this.targetCategoryList.stream().collect(Collectors.toList()), category -> {
				// save category total product num
				category.setStatus("PROGRESS_LIST");
				this.categoryService.save(category);
				this.logService.addLog("카테고리[" + category.getKorean() + "] 리스트 추출을 시작합니다.");
			}, (category, productList) -> {
				productList.parallelStream().forEach(product -> {
					this.productService.save(product);
					List<String> imagePathList = findImage(product);
					imagePathList.parallelStream().forEach(image -> {
						ProductImage productImage = new ProductImage();
						productImage.setName(image);
						productImage.setProduct(product);
						productImage.setCreateDate(new Date());
						this.productService.save(productImage);
					});
				});
				category.setModifiedDate(new Date());
				category.setExtProductNum(category.getExtProductNum() + productList.size());
				this.categoryService.save(category);
			}, category -> {
				category.setStatus("COMPLETE_LIST");
				category.setModifiedDate(new Date());
				this.categoryService.save(category);
				this.logService.addLog("카테고리 (" + category.getKorean() + ") 를 리스트 추출하였습니다.");
			});
			this.logService.addLog("데이터 상세 추출을 시작합니다.");
			AtomicInteger detailNum = new AtomicInteger(0);
			// extract detail
			extractedProductList &= this.dataExtractor.extractProductDetail(this.targetCategoryList.stream().collect(Collectors.toList()), (category, product) -> {
				Product prod = this.productService.findProductByUketsukeNo(product.getUketsukeNo());
				if (prod != null) {
					prod.setSalesPoint(product.getSalesPoint());
					prod.setSalesPoint2(product.getSalesPoint2());
					prod.setAccessories(product.getAccessories());
					prod.setNote(product.getNote());
					prod.setModifiedDate(new Date());
					this.productService.save(prod);
	
					category.setExtProductDetailNum(category.getExtProductDetailNum() + 1);
					category.setStatus(category.getTotalProductNum() <= category.getExtProductDetailNum() ? "COMPLETE_DETAIL" : category.getStatus());
					categoryService.save(category);
					
					if (detailNum.incrementAndGet() % 500 == 0) {
						this.logService.addLog("데이터 상세 추출 진행중입니다.(500건 씩 로그)");
					}
				}
			});
			this.logService.addLog("데이터 상세 추출을 완료하였습니다. Total[" + detailNum.get() + "]");
		} catch (DataExtractException e) {
			log.warn(e.getMessage());
			this.logService.addLog(e.getMessage());
		}
		return extractedProductList;
	}
	
	private List<String> findImage(final Product product) {
		Objects.requireNonNull(product);
		final String uketsukeNo = product.getUketsukeNo();
		final AtomicBoolean available = new AtomicBoolean(true);
		final String[] uketsukeInfo = uketsukeNo.split("-");
		final String uketsukePrefix = uketsukeInfo[0];
		final String uketsukeMiddle = uketsukeInfo[1].substring(0, 3);
		final List<String> imageList = IntStream.rangeClosed(1, 10)
		.boxed()
		.map(i -> String.format(this.siteInfo.getTargetPhotoFormat(), uketsukePrefix, uketsukeMiddle, uketsukePrefix, uketsukeNo, i))
		.filter(i -> {
			if (available.get()) {
				available.set(DataExtractorUtil.availableURL(i));
			}
			return available.get();
		})
		.collect(Collectors.toList());
		return imageList;
	}
}
