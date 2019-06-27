package com.yonga.auc.data.category;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    @Cacheable("category")
	@Query(value=
            "select c.* " +
            "from category c " +
            "where ext_product_num > 0 " +
              "and total_product_num > 0", nativeQuery=true)
	List<Category> findCategory() throws DataAccessException;

    @SuppressWarnings("unchecked")
    @CacheEvict(value = {"category", "maker", "productType", "keijo"}, allEntries = true)
    @Override
    Category save(Category entity);

    @Query(value=
            "select c.* " +
                    "from category c " +
                    "where ext_product_num > 0 " +
                    "and total_product_num > 0 " +
                    "and kaisai_kaisu = :kaisaiKaisu", nativeQuery=true)
    List<Category> findAllByKaisaiKaisu(@Param("kaisaiKaisu") Integer kaisaiKaisu);

    @Query(value = "SELECT sum(EXT_PRODUCT_NUM) FROM category WHERE KAISAI_KAISU = :kaisaiKaisu", nativeQuery = true)
    Integer countExtractedProductNum(@Param("kaisaiKaisu") Integer kaisaiKaisu);

}
