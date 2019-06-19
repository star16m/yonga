package com.yonga.auc.data.product;

import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.category.detail.*;
import com.yonga.auc.data.product.image.ProductImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ProductService {
    @Autowired
    private MakerRepository makerRepository;
    @Autowired
    private KeijoRepository keijoRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductImageRepository productImageRepository;
    public Page<Product> findProductList(Integer categoryId, List<Integer> selectsMakerList, List<Integer> selectsTypeList, List<Integer> selectsKeijoList, Pageable pageable) {
        return this.productRepository.findNewProductByGenreCdAndMakerCdInAndBrandTypeCdInAndKeijoCdInOrderByMakerCdAscKeijoCdAscUketsukeBngAsc(categoryId, selectsMakerList, selectsTypeList, selectsKeijoList, pageable);
    }

    public Product findNewProductByUketsukeNo(String uketsukeNo) {
        Product product = this.productRepository.findNewProductByUketsukeBng(uketsukeNo);
//        if (YongaUtil.isNotNull(product)) {
//            product.setProductImageList(this.productImageRepository.findByUketsukeBngOrderByDisplayOrderAsc(uketsukeNo));
//        }
        return product;
    }

    public List<Maker> findMaker(Category category) {
        return this.makerRepository.findMaker(category.getId());
    }
    public List<Keijo> findKeijo(Category category) {
        return this.keijoRepository.findKeijo(category.getId());
    }
    public List<Brand> findBrand(Category category) {
        return this.brandRepository.findBrand(category.getId());
    }
    public int deleteAll(Category category) {
        log.info("try delete all with category [{}]", category);
        int result = -1;
        try {
            result = this.productImageRepository.deleteByCategoryId(category.getId());
            result = this.productRepository.deleteByCategoryId(category.getId());
            log.info("success full delete product items [{}]", result);
            return result;
        } catch (Throwable e) {
            log.error(e.getMessage());
            throw e;
        }
    }
}
