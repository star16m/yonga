package com.yonga.auc.data.extract;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.category.CategoryRepository;
import com.yonga.auc.data.product.Product;
import com.yonga.auc.data.product.ProductRepository;
import com.yonga.auc.data.product.image.ProductImage;
import com.yonga.auc.data.product.image.ProductImageRepository;

public class DataExtractWorker implements Runnable {

	private CategoryRepository categoryRepository;
	private ProductRepository productRepository;
	private ProductImageRepository imageRepository;
	private String targetPhotoFormat;
	private String extractMode;
	private Map<String, String> initializeInfoMap;

	public DataExtractWorker(CategoryRepository categoryRepository, ProductRepository productRepository, ProductImageRepository imageRepository, 
			Map<String, String> initializeInfoMap) {
		this.categoryRepository = categoryRepository;
		this.productRepository = productRepository;
		this.imageRepository = imageRepository;
		this.initializeInfoMap = initializeInfoMap;
		this.targetPhotoFormat = String.format("%s://%s", this.initializeInfoMap.get("targetProtocol"), this.initializeInfoMap.get("targetHost")) + "/ext_photos/SP/%s/%s/J%s_%s_0%dTN.jpg";
		this.extractMode = StringUtils.defaultString(this.initializeInfoMap.get("extract_mode"), "init");
	}

	private DataExtractor getDataExtractor() {
		return new DataExtractor(initializeInfoMap);
	}

	@Override
	public void run() {
		if (this.extractMode.equals("init")) {
			initializeAllProduct();
		}
	}
	private void initializeAllProduct() {
		// clean porduct
		this.imageRepository.deleteAllInBatch();
		this.productRepository.deleteAllInBatch();
		
		// get all category
		final List<Category> allCategoryList = this.categoryRepository.findAll();
		// set status to PROGRESS
		allCategoryList.parallelStream().forEach(category -> {
			category.setModifiedDate(new Date());
			category.setTotalProductNum(0);
			category.setExtProductNum(0);
			category.setStatus("PROGRESS");
			this.categoryRepository.save(category);
		});
		// extract
		getDataExtractor().extractAll(allCategoryList.stream().collect(Collectors.toList()), (category, productList) -> {
			productList.parallelStream().forEach(product -> {
				this.productRepository.save(product);
				List<String> imagePathList = findImage(product);
				imagePathList.parallelStream().forEach(image -> {
					ProductImage productImage = new ProductImage();
					productImage.setName(image);
					productImage.setProduct(product);
					productImage.setCreateDate(new Date());
					this.imageRepository.save(productImage);
				});
			});
			category.setModifiedDate(new Date());
			this.categoryRepository.save(category);
		});
		
		allCategoryList.parallelStream().forEach(category -> {
			category.setModifiedDate(new Date());
			category.setStatus("COMPLETE");
			this.categoryRepository.save(category);
		});
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
		.map(i -> String.format(this.targetPhotoFormat, uketsukePrefix, uketsukeMiddle, uketsukePrefix, uketsukeNo, i))
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
