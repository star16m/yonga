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

	List<Product> findProductByCategoryIdOrderByProductNo(Integer categoryId);

	Page<Product> findProductByCategoryIdAndMakerInAndTypeInOrderByMakerAscTypeAscRatingAscProductNoAsc(
			Integer categoryId, List<String> makerList, List<String> typeList, Pageable page);

	@Query(value = "select new map(p.category.id as category_no, p.maker as maker, count(p.maker) as makerCount) from Product p where p.category.id = :categoryId group by p.category.id, p.maker order by count(p.maker) desc")
	List<Map<String, Object>> findProductMaker(Integer categoryId);

	@Query(value = "select new map(p.category.id as category_no, p.type as type, count(p.type) as typeCount) from Product p where p.category.id = :categoryId group by p.category.id, p.type order by count(p.type) desc")
	List<Map<String, Object>> findProductType(Integer categoryId);

	@Transactional
	@Query(value = "delete from Product p where p.category.id = :categoryId")
	@Modifying
	int deleteByCategoryId(@Param("categoryId") Integer categoryId);
}
