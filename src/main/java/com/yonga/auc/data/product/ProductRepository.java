package com.yonga.auc.data.product;

import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Integer> {

	Product findProductByUketsukeNo(String uketsukeNo);

	List<Product> findProductByCategoryId(Integer categoryId);

	Integer countProductByCategoryId(Integer categoryId);
	List<Product> findProductByCategoryIdIsNull();
	List<Product> findProductByCategoryIdOrderByProductNo(Integer categoryId);

	Page<Product> findProductByCategoryIdAndMakerInAndTypeInAndKeijoInOrderByMakerAscKeijoAscRatingAscProductNoAsc(
			Integer categoryId, List<String> makerList, List<String> typeList, List<String> keijoList, Pageable page);


	@Transactional
	@Query(value = "delete from Product p where p.category.id = :categoryId")
	@Modifying
	int deleteByCategoryId(@Param("categoryId") Integer categoryId);
}
