package com.yonga.auc.data.product2;

import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.category.detail.*;
import com.yonga.auc.data.product2.image.NewProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewProductService {
    @Autowired
    private MakerRepository makerRepository;
    @Autowired
    private KeijoRepository keijoRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private NewProductRepository newProductRepository;
    @Autowired
    private NewProductImageRepository productImageRepository;
    public Page<NewProduct> findProductList(Integer categoryId, List<Integer> selectsMakerList, List<Integer> selectsTypeList, List<Integer> selectsKeijoList, Pageable pageable) {
        return this.newProductRepository.findNewProductByGenreCdAndMakerCdInAndBrandTypeCdInAndKeijoCdInOrderByMakerCdAscKeijoCdAscUketsukeBngAsc(categoryId, selectsMakerList, selectsTypeList, selectsKeijoList, pageable);
    }

    public NewProduct findNewProductByUketsukeNo(String uketsukeNo) {
        NewProduct product = this.newProductRepository.findNewProductByUketsukeBng(uketsukeNo);
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
}
