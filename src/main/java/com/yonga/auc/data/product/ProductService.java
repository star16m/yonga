package com.yonga.auc.data.product;

import java.util.List;
import java.util.Objects;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.product.image.ProductImageRepository;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ProductImageRepository productImageRepository;
	public List<Product> findProductByCategory(Category category) {
		Objects.requireNonNull(category);
		return this.productRepository.findProductByCategoryIdOrderByProductNo(category.getId());
	}
	@Transactional
	public void save(Product product) {
		Objects.requireNonNull(product);
		if (product.getId() == null) {
			productRepository.save(product);
		}
		productImageRepository.deleteByProductId(product.getId());
		if (!product.getImageList().isEmpty()) {
			product.getImageList().stream().forEach(productImage -> {
				productImage.setProduct(product);
				productImageRepository.save(productImage);
			});
		}
		productRepository.saveAndFlush(product);
	}
	@Transactional
	public void deleteByCategory(Category category) {
		Objects.requireNonNull(category);
		List<Product> productList = findProductByCategory(category);
		if (productList != null && !productList.isEmpty()) {
			productList.stream().forEach(product -> {
				this.productImageRepository.deleteByProductId(product.getId());
				this.productRepository.delete(product);
			});
		}
	}
	public void deleteAll() {
		this.productImageRepository.deleteAll();
		this.productRepository.deleteAll();
	}
}
