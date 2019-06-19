package com.yonga.auc.data.product.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, String> {
    List<ProductImage> findByUketsukeBngOrderByDisplayOrderAsc(String uketsukeBng);
    @Transactional
    void deleteByGenreCd(Integer genreCd);

    @Transactional
    @Query(value = "delete from ProductImage p where p.genreCd = :categoryId")
    @Modifying
    int deleteByCategoryId(@Param("categoryId") Integer categoryId);
}
