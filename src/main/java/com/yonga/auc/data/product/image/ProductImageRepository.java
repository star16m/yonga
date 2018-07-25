package com.yonga.auc.data.product.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

	@Query(value = "delete from ProductImage pi where pi.product.id = :productId")
	@Modifying
	int deleteByProductId(@Param("productId") Integer productId);
}
