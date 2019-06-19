package com.yonga.auc.data.category.detail;

import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    @Query(value="select k.* from brand k where category_no = :categoryId order by brand_cd asc", nativeQuery=true)
    List<Brand> findBrand(@Param("categoryId") Integer categoryId) throws DataAccessException;

    @Transactional
    void deleteByCategoryNo(Integer categoryNo) throws DataAccessException;
}
