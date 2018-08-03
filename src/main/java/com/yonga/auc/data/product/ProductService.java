package com.yonga.auc.data.product;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;
	@Cacheable("maker")
	public List<Map<String, Object>> findProductMaker(Integer categoryId) {
		return this.productRepository.findProductMaker(categoryId);
	}
	@Cacheable("productType")
	public List<Map<String, Object>> findProductType(Integer categoryId) {
		return this.productRepository.findProductType(categoryId);
	}
	
	public Page<Product> findProductList(Integer categoryId, List<String> selectsMakerList, List<String> selectsTypeList, Pageable pageable) {
		return this.productRepository.findProductByCategoryIdAndMakerInAndTypeInOrderByMakerAscTypeAscRatingAscProductNoAsc(categoryId, selectsMakerList, selectsTypeList, pageable);
	}

	public Product findProductByUketsukeNo(String uketsukeNo) {
		return this.productRepository.findProductByUketsukeNo(uketsukeNo);
	}
}
