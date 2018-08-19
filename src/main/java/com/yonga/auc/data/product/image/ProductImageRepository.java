package com.yonga.auc.data.product.image;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

	@Transactional
	@Query(value = "delete from product_image pi where exists (select 1 from product p where p.id = pi.product_id and p.category_no = :categoryId )", nativeQuery=true)
	@Modifying
	int deleteByCategoryId(@Param("categoryId") Integer categoryId);
}
