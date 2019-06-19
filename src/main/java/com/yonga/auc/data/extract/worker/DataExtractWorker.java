package com.yonga.auc.data.extract.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.config.ConfigConstants;
import com.yonga.auc.config.ConfigService;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.category.CategoryService;
import com.yonga.auc.data.category.ExtractMode;
import com.yonga.auc.data.category.detail.*;
import com.yonga.auc.data.extract.DataExtractException;
import com.yonga.auc.data.extract.DataExtractor;
import com.yonga.auc.data.extract.ExtractSiteInfo;
import com.yonga.auc.data.log.LogService;
import com.yonga.auc.data.product.ExtractResult;
import com.yonga.auc.data.product.ProductService;
import com.yonga.auc.data.product2.NewProduct;
import com.yonga.auc.data.product2.NewProductRepository;
import com.yonga.auc.data.product2.image.NewProductImageRepository;
import com.yonga.auc.mail.MailContents;
import com.yonga.auc.mail.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class DataExtractWorker implements Callable<Boolean> {

    private CategoryService categoryService;
    private ProductService productService;

    private NewProductRepository newProductRepository;
    private NewProductImageRepository newProductImageRepository;
    private MakerRepository makerRepository;
    private BrandRepository brandRepository;
    private KeijoRepository keijoRepository;


    private ExtractSiteInfo siteInfo;
    private DataExtractor dataExtractor;
    private LogService logService;
    private ConfigService configService;
    private List<Category> targetCategoryList;
    private ExtractMode extractMode;
    private MailService mailService;
    private ObjectMapper objectMapper;

    public DataExtractWorker(CategoryService categoryService, ProductService productService, ExtractSiteInfo siteInfo, LogService logService, ConfigService configService, List<Category> targetCategoryList, ExtractMode extractMode, MailService mailService,
                             NewProductRepository newProductRepository,
                             NewProductImageRepository newProductImageRepository,
                             MakerRepository makerRepository,
                             BrandRepository brandRepository,
                             KeijoRepository keijoRepository,
                             ObjectMapper objectMapper) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.siteInfo = siteInfo;
        this.logService = logService;
        this.configService = configService;
        this.targetCategoryList = targetCategoryList;
        this.extractMode = extractMode;
        this.mailService = mailService;
        this.newProductRepository = newProductRepository;
        this.newProductImageRepository = newProductImageRepository;
        this.makerRepository = makerRepository;
        this.brandRepository = brandRepository;
        this.keijoRepository = keijoRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Boolean call() {
        this.dataExtractor = new DataExtractor(siteInfo);
        boolean extractedProductList = true;
        Date startDate = new Date();
        AtomicInteger totalExtractNum = new AtomicInteger(0);
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            // extract
            this.logService.addLog("데이터 상세 추출을 시작합니다.");
//            AtomicInteger detailNum = new AtomicInteger(0);
            // extract detail
            extractedProductList &= this.dataExtractor.extractProductDetail(
                    this.targetCategoryList.stream().collect(Collectors.toList()),
                    (auctionInfo) -> {
                        log.info("수집된 auction 정보 [{}]", auctionInfo);
                        try {
                            String auctionInfoString = this.objectMapper.writeValueAsString(auctionInfo);
                            if (YongaUtil.isNotEmpty(auctionInfoString)) {
                                this.configService.setConfigValue("AUCTION", "INFO", auctionInfoString);
                            }
                            ConfigConstants.AUCTION_INFO = auctionInfo;
                        } catch (IOException e) {
                            // do nothing.
                        }
                    },
                    (category, categoryDetailInfo) -> {
                        // 초기화 할 경우, 각 카테고리 상세 정보를 초기화 한다.
                        if (this.extractMode.isRequiredInitialize()) {
                            this.newProductImageRepository.deleteByGenreCd(category.getId());
                            this.newProductRepository.deleteByGenreCd(category.getId());
                            this.makerRepository.deleteByCategoryNo(category.getId());
                            this.brandRepository.deleteByCategoryNo(category.getId());
                            this.keijoRepository.deleteByCategoryNo(category.getId());
                            if (YongaUtil.isNotEmpty(categoryDetailInfo.getMakerListInfo())) {
                                categoryDetailInfo.getMakerListInfo().stream().map(maker -> Maker.valueOf(maker, category)).forEach(maker -> this.makerRepository.save(maker));
                            }
                            if (YongaUtil.isNotEmpty(categoryDetailInfo.getBrandTypeListInfo())) {
                                categoryDetailInfo.getBrandTypeListInfo().stream().map(brand -> Brand.valueOf(brand, category)).forEach(brand -> this.brandRepository.save(brand));
                            }
                            if (YongaUtil.isNotEmpty(categoryDetailInfo.getKeijoListInfo())) {
                                categoryDetailInfo.getKeijoListInfo().stream().map(keijo -> Keijo.valueOf(keijo, category)).forEach(keijo -> this.keijoRepository.save(keijo));
                            }
                        }
                    },
                    (category, foundCategoryProductTotalNum) -> {
                        // 기존 데이터와 전체 건수가 상이한 경우 데이터를 초기화 한다.
                        if (this.extractMode.isRequiredInitialize() || foundCategoryProductTotalNum != category.getTotalProductNum()) {
                            this.logService.addLog(
                                    String.format("카테고리[%s]를 초기화 합니다. 추출된 전체 건수 [%s], 기존 전체 건수 [%s], 전달된 초기화 여부 [%s]",
                                            category.getKorean(),
                                            foundCategoryProductTotalNum,
                                            category.getTotalProductNum(),
                                            this.extractMode.isRequiredInitialize()));
                            category.setStatus("PROGRESS");
                            category.setExtProductNum(0);
                            category.setTotalProductNum(foundCategoryProductTotalNum);
                            category.setModifiedDate(new Date());
                            this.categoryService.save(category);
                        }
                        this.logService.addLog(
                                String.format("카테고리[%s] 추출을 시작합니다. 전체 건수 [%s]", category.getKorean(), foundCategoryProductTotalNum)
                        );
                    },
                    (category, productList) -> {
                        log.info("extract category [{}], elements [{}]", category.getKorean(), productList.getSize());
                        // 그 외의 경우, 모든 제품에 대해서 상세 추출을 진행한다.
                        AtomicInteger extractProduct = new AtomicInteger(0);
                        List<NewProduct> initializedProductList = productList.getContent().stream().map(p -> {
                            NewProduct product = new NewProduct(p);
                            product.setExtractResult(ExtractResult.INITIALIZE);
                            product = this.newProductRepository.save(product);
                            extractProduct.incrementAndGet();
                            totalExtractNum.incrementAndGet();
                            return product;
                        }).collect(Collectors.toList());
                        category.setExtProductNum(category.getExtProductNum() + extractProduct.get());
                        this.categoryService.save(category);
                        return initializedProductList;
                    }, (category, product) -> {

                        log.info("extracted product [{}]", product);
                        this.newProductImageRepository.saveAll(product.getProductImage());
                        product.setExtractResult(ExtractResult.COMPLETE);
                        if (!this.brandRepository.existsById(product.getBrandTypeCd())) {
                            Brand brand = new Brand();
                            brand.setBrandCd(product.getBrandTypeCd());
                            brand.setCategoryNo(product.getGenreCd());
                            brand.setName(product.getBrandType());
                            brand.setNameEn(product.getBrandType());
                            brand.setNameKr(product.getBrandType());
                            this.brandRepository.save(brand);
                        }
                        this.newProductRepository.save(product);
                    }, (category, product) -> {
                        this.logService.addLog(String.format("[%s] 제품 상세 추출에 실패하였습니다. 카테고리:[%s], 접수번호:[%s]", category.getKorean(), product.getUketsukeBng()));
                    }, (category, extractedProductNum) -> {
                        category.setStatus("COMPLETE");
                        category.setModifiedDate(new Date());
                        categoryService.save(category);
                        this.logService.addLog(String.format("[%s] 데이터 상세 추출을 완료 하였습니다.(%d)", category.getKorean(), extractedProductNum));
                    });
            this.logService.addLog("데이터 상세 추출을 완료하였습니다. Total[" + totalExtractNum.get() + "]");
            this.configService.setConfigValue("EXECUTOR", "STATUS", "COMPLETE");
            this.configService.setConfigValue("EXECUTOR", "MESSAGE", "완료");
        } catch (DataExtractException e) {
            log.warn(e.getMessage());
            this.logService.addLog(e.getMessage());
            if (YongaUtil.isNotNull(e.getCause())) {
                this.logService.addLog("error [" + e.getCause() + "]");
            }
            this.configService.setConfigValue("EXECUTOR", "STATUS", "FAIL");
            this.configService.setConfigValue("EXECUTOR", "MESSAGE", e.getMessage());
        } finally {
            watch.stop();
            String adminEmail = this.configService.getConfigValue("CONFIG", "ADMIN_EMAIL");
            String mailId = this.configService.getConfigValue("CONFIG", "MAIL_ID");
            if (YongaUtil.isNotEmpty(mailId)) {
                try {
                    this.mailService.sendEmail(new MailContents("제품 추출 완료", "데이터 추출을 완료 하였습니다.",
                            Arrays.asList("시작시간 : " + startDate, "종료시간 : " + new Date()),
                            Arrays.asList("추출 시간 : [" + watch.getTotalTimeSeconds() + "] 초"),
                            Arrays.asList("카테고리 : " + this.targetCategoryList.size() + "개",
                                    "상태 : " + this.configService.getConfigValue("EXECUTOR", "STATUS"),
                                    "메세지 : " + this.configService.getConfigValue("EXECUTOR", "MESSAGE"),
                                    "추출 제품 수 : " + totalExtractNum)
                    ), adminEmail);
                } catch (Exception e) {
                    this.logService.addLog("메일 발송 중 에러가 발생하였습니다. error [" + e.getMessage() + "]");
                }
            }
        }
        return extractedProductList;
    }
}
