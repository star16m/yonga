package com.yonga.auc.data.extract.worker;

import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.category.CategoryService;
import com.yonga.auc.data.extract.DataExtractException;
import com.yonga.auc.data.extract.DataExtractor;
import com.yonga.auc.data.extract.DataExtractorUtil;
import com.yonga.auc.data.extract.ExtractSiteInfo;
import com.yonga.auc.data.log.LogService;
import com.yonga.auc.data.product.Product;
import com.yonga.auc.data.product.ProductService;
import com.yonga.auc.data.product.image.ProductImage;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class DataExtractWorker implements Callable<Boolean> {

    private CategoryService categoryService;
    private ProductService productService;
    private ExtractSiteInfo siteInfo;
    private DataExtractor dataExtractor;
    private LogService logService;
    private List<Category> targetCategoryList;

    public DataExtractWorker(CategoryService categoryService, ProductService productService, ExtractSiteInfo siteInfo, LogService logService, List<Category> targetCategoryList) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.siteInfo = siteInfo;
        this.logService = logService;
        this.targetCategoryList = targetCategoryList;
    }

    @Override
    public Boolean call() {
        this.dataExtractor = new DataExtractor(siteInfo);
        boolean extractedProductList = true;
        try {
            // extract
            this.logService.addLog("데이터 상세 추출을 시작합니다.");
            AtomicInteger detailNum = new AtomicInteger(0);
            // extract detail
            extractedProductList &= this.dataExtractor.extractProductDetail(
                    this.targetCategoryList.stream().collect(Collectors.toList()),
                    (category, totalProduct) -> {
                        category.setStatus("PROGRESS_LIST");
                        category.setExtProductNum(0);
                        category.setTotalProductNum(totalProduct);
                        category.setModifiedDate(new Date());
                        this.categoryService.save(category);
                        this.logService.addLog("카테고리[" + category.getKorean() + "] 추출을 시작합니다. 전체 건수 [" + totalProduct + "]");
                    },
                    (category, productPair) -> {
                        int extractedProductNum = detailNum.getAndIncrement();
                        Product product = productPair.getFirst();
                        List<String> productImagePathList = productPair.getSecond();
                        product.setProductNo(extractedProductNum);
                        String thumbnailImage = productImagePathList.stream().sorted().findFirst().orElse(null);
                        product.setThumbnailImage(thumbnailImage);
                        this.productService.save(product);
                        productImagePathList.parallelStream().forEach(imagePath -> {
                            ProductImage productImage = new ProductImage();
                            productImage.setName(imagePath);
                            productImage.setProduct(product);
                            productImage.setCreateDate(new Date());
                            this.productService.save(productImage);
                        });
                        if (extractedProductNum % 100 == 0) {
                            category.setExtProductNum(extractedProductNum);
                            category.setStatus("PROGRESS_DETAIL");
                            category.setModifiedDate(new Date());
                            categoryService.save(category);
                            this.logService.addLog(String.format("[%s] 데이터 상세 추출 진행중입니다.(%d/%d)", category.getKorean(), extractedProductNum, category.getTotalProductNum()));
                        }
                    }, (category, extractedProductNum) -> {
                        category.setExtProductNum(extractedProductNum);
                        category.setStatus("COMPLETE");
                        category.setModifiedDate(new Date());
                        categoryService.save(category);
                        this.logService.addLog(String.format("[%s] 데이터 상세 추출을 완료 하였습니다.(%d)", category.getKorean(), extractedProductNum));
                    });
            this.logService.addLog("데이터 상세 추출을 완료하였습니다. Total[" + detailNum.get() + "]");
        } catch (DataExtractException e) {
            log.warn(e.getMessage());
            this.logService.addLog(e.getMessage());
        }
        return extractedProductList;
    }
}
