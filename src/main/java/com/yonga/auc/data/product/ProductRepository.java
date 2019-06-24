package com.yonga.auc.data.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    Boolean existsByUketsukeBng(String uketsukeBng);

    Page<Product> findNewProductByGenreCdAndMakerCdInAndBrandTypeCdInAndKeijoCdInOrderByMakerCdAscKeijoCdAscUketsukeBngAsc(Integer genreCd, List<Integer> selectsMakerCdList, List<Integer> selectsBrandTypeList, List<Integer> selectsKeijoCdList, Pageable pageable);

    Product findNewProductByUketsukeBng(String uketsukeBng);

    @Transactional
    void deleteByGenreCd(Integer genreCd);

    @Transactional
    @Query(value = "delete from Product p where p.genreCd = :categoryId")
    @Modifying
    int deleteByCategoryId(@Param("categoryId") Integer categoryId);

    @Query(value = "select * from product p where p.genre_cd = :categoryId and p.extract_result = 'INITIALIZE'", nativeQuery = true)
    List<Product> findProductByNotCollected(@Param("categoryId") Integer categoryId);
}
