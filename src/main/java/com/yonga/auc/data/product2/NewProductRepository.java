package com.yonga.auc.data.product2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface NewProductRepository extends JpaRepository<NewProduct, String> {
    Boolean existsByUketsukeBng(String uketsukeBng);

    Page<NewProduct> findNewProductByGenreCdAndMakerCdInAndBrandTypeCdInAndKeijoCdInOrderByMakerCdAscKeijoCdAscUketsukeBngAsc(Integer genreCd, List<Integer> selectsMakerCdList, List<Integer> selectsBrandTypeList, List<Integer> selectsKeijoCdList, Pageable pageable);

    NewProduct findNewProductByUketsukeBng(String uketsukeBng);

    @Transactional
    void deleteByGenreCd(Integer genreCd);
}
