package com.yonga.auc.data.product;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.product.image.ProductImage;
import com.yonga.auc.data.product.image.ProductImageRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ProductImageRepository productImageRepository;

	public Integer findProductNum(Integer categoryId) {
		return this.productRepository.countProductByCategoryId(categoryId);
	}
	public Integer findAllProductNum() {
		return this.productRepository.findAll().size();
	}
	public List<Product> findCategoryIdNull() {
		return this.productRepository.findProductByCategoryIdIsNull();
	}
	@Cacheable("maker")
	public List<Map<String, Object>> findProductMaker(Integer categoryId) {
		return this.productRepository.findProductMaker(categoryId);
	}
	@Cacheable("productType")
	public List<Map<String, Object>> findProductType(Integer categoryId) {
		return this.productRepository.findProductType(categoryId);
	}
	@Cacheable("keijo")
	public List<Map<String, Object>> findProductKeijo(Integer categoryId) {
		return this.productRepository.findProductKeijo(categoryId);
	}
	
	public Page<Product> findProductList(Integer categoryId, List<String> selectsMakerList, List<String> selectsTypeList, List<String> selectsKeijoList, Pageable pageable) {
		return this.productRepository.findProductByCategoryIdAndMakerInAndTypeInAndKeijoInOrderByMakerAscKeijoAscRatingAscProductNoAsc(categoryId, selectsMakerList, selectsTypeList, selectsKeijoList, pageable);
	}

	public Product findProductByUketsukeNo(String uketsukeNo) {
		return this.productRepository.findProductByUketsukeNo(uketsukeNo);
	}
	public Product save(Product product) {
		return this.productRepository.save(product);
	}
	public ProductImage save(ProductImage productImage) {
		return this.productImageRepository.save(productImage);
	}
	public int deleteAll(Category category) {
		log.info("try delte all with category [{}]", category);
		int result = -1;
		try {
			result = this.productImageRepository.deleteByCategoryId(category.getId());
			result = this.productRepository.deleteByCategoryId(category.getId());
			log.info("success full delete product items [{}]", result);
			return result;
		} catch (Throwable e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	public void deleteAll() {
		this.productImageRepository.deleteAllInBatch();
		this.productRepository.deleteAllInBatch();
	}
}
