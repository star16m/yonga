package com.yonga.auc.data.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
	
	Product findProductByUketsukeNo(String uketsukeNo);
	List<Product> findProductByCategoryIdOrderByProductNo(Integer categoryId);
	Page<Product> findProductByCategoryIdOrderByMakerAscTypeAscRatingAscProductNoAsc(Integer categoryId, Pageable page);
}
