package com.yonga.auc.data.product2.image;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface NewProductImageRepository extends JpaRepository<NewProductImage, String> {
    List<NewProductImage> findByUketsukeBngOrderByDisplayOrderAsc(String uketsukeBng);
    @Transactional
    void deleteByGenreCd(Integer genreCd);
}
