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

    @Query(value =
            "select p.* from product p " +
            " where genre_cd = :genreCd " +
            "   and maker_cd in :selectsMakerCdList " +
            "   and brand_type_cd in :selectsBrandTypeList " +
            "   and keijo_cd in :selectsKeijoCdList " +
            // rt, low_price, mall 제품 구분
            "   and product_type in :productTypeList " +
            " order by maker_cd asc, brand_type_cd asc, keijo_cd asc, product_type asc, uketsuke_bng asc",
                    nativeQuery = true)
    Page<Product> findProductByGenreCdAndMakerCdInAndBrandTypeCdInAndKeijoCdInOrderByMakerCdAscKeijoCdAscUketsukeBngAsc(Integer genreCd, List<Integer> selectsMakerCdList, List<Integer> selectsBrandTypeList, List<Integer> selectsKeijoCdList, List<String> productTypeList, Pageable pageable);

    Product findProductByUketsukeBng(String uketsukeBng);

    @Transactional
    void deleteByGenreCd(Integer genreCd);

    @Transactional
    @Query(value = "delete from Product p where p.genreCd = :categoryId")
    @Modifying
    int deleteByCategoryId(@Param("categoryId") Integer categoryId);

    @Query(value = "select * from product p where p.genre_cd = :categoryId and p.extract_result = 'INITIALIZE'", nativeQuery = true)
    List<Product> findProductByNotCollected(@Param("categoryId") Integer categoryId);

}
