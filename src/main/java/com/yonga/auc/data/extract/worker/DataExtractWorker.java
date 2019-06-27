package com.yonga.auc.data.extract.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.config.Config;
import com.yonga.auc.config.ConfigConstants;
import com.yonga.auc.config.ConfigService;
import com.yonga.auc.data.category.*;
import com.yonga.auc.data.category.detail.*;
import com.yonga.auc.data.extract.DataExtractor;
import com.yonga.auc.data.extract.ExtractSiteInfo;
import com.yonga.auc.data.log.LogService;
import com.yonga.auc.data.product.ExtractResult;
import com.yonga.auc.data.product.Product;
import com.yonga.auc.data.product.ProductList;
import com.yonga.auc.data.product.ProductRepository;
import com.yonga.auc.data.product.image.ProductImageRepository;
import com.yonga.auc.mail.MailContents;
import com.yonga.auc.mail.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DataExtractWorker implements Callable<Boolean> {

    private CategoryService categoryService;

    private ProductRepository productRepository;
    private ProductImageRepository productImageRepository;
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

    public DataExtractWorker(CategoryService categoryService, ExtractSiteInfo siteInfo, LogService logService, ConfigService configService, List<Category> targetCategoryList, ExtractMode extractMode, MailService mailService,
                             ProductRepository productRepository,
                             ProductImageRepository productImageRepository,
                             MakerRepository makerRepository,
                             BrandRepository brandRepository,
                             KeijoRepository keijoRepository,
                             ObjectMapper objectMapper) {
        this.categoryService = categoryService;
        this.siteInfo = siteInfo;
        this.logService = logService;
        this.configService = configService;
        this.targetCategoryList = targetCategoryList;
        this.extractMode = extractMode;
        this.mailService = mailService;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.makerRepository = makerRepository;
        this.brandRepository = brandRepository;
        this.keijoRepository = keijoRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Boolean call() {
        Boolean showExtractView = YongaUtil.getBoolean(this.configService.getConfigValue("CONFIG", "EXTRACT_VIEW"));
        this.dataExtractor = new DataExtractor(siteInfo, showExtractView);
        boolean extractedProductList = true;
        Date startDate = new Date();
        AtomicInteger totalExtractNum = new AtomicInteger(0);
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            // extract
            this.logService.addLog("데이터 상세 추출을 시작합니다.");
            // 1. 로그인
            this.dataExtractor.login();
            // 2. AuctionInfo 수집
            AuctionInfo auctionInfo = this.dataExtractor.extractAuctionInfo();
            log.info("수집된 auction 정보 [{}]", auctionInfo);
            try {
                String auctionInfoString = this.objectMapper.writeValueAsString(auctionInfo);
                if (YongaUtil.isNotEmpty(auctionInfoString)) {
                    this.configService.setConfigValue("AUCTION", "INFO", auctionInfoString);
                }
                if (YongaUtil.isNull(ConfigConstants.AUCTION_INFO)
                        && YongaUtil.isNull(ConfigConstants.AUCTION_INFO.getKaisaiKaisu())
                        && !ConfigConstants.AUCTION_INFO.getKaisaiKaisu().equals(auctionInfo.getKaisaiKaisu())) {
                    // 저장되어 있는 회차가 다른 경우, 무조건 초기화 한다.
                    this.extractMode = ExtractMode.INITIALIZE;
                    this.logService.addLog(String.format("새로운 Auction 회차[%s]가 확인 되어 초기화를 시도합니다.", auctionInfo.getKaisaiKaisu()));
                }
                ConfigConstants.AUCTION_INFO = auctionInfo;
            } catch (IOException e) {
                // do nothing.
                this.logService.addLog(String.format("Auction 정보를 변환 중에 에러가 발생하였습니다. [%s]", YongaUtil.substring(e.getMessage())));
            }
            // 3. 카테고리 별 상세 조건(maker, brand, keijo) 추출
            if (!this.extractMode.isRequiredInitialize()) {
                // 초기화를 하지 않는 경우에도 카테고리 별 제품 수를 추출하여 다른 경우 초기화를 시도한다.
                this.targetCategoryList.stream().forEach(category -> {
                    Integer productNum = this.dataExtractor.extractProductNum(category);
                    if (!category.getTotalProductNum().equals(productNum)) {
                        this.extractMode = ExtractMode.INITIALIZE;
                        this.logService.addLog(String.format("카테고리 [%s] 에 저장되어 있는 제품 수가 달라서 초기화를 시도합니다. 기존 제품 수 [%s], 신규 제품 수 [%s]", category.getKorean(), category.getTotalProductNum(), productNum));
                    }
                });
            }
            // 4. 카테고리 별 초기화
            Map<Integer, List<Maker>> makerMap = new HashMap<>();
            Map<Integer, List<Brand>> brandMap = new HashMap<>();
            Map<Integer, List<Keijo>> keijoMap = new HashMap<>();
            if (this.extractMode.isRequiredInitialize()) {
                this.targetCategoryList.stream().forEach(category -> {
                    this.logService.addLog(String.format("카테고리 [%s] 를 초기화합니다.", category.getKorean()));
                    CategoryDetailInfo categoryDetailInfo = this.dataExtractor.extractCategoryDetail(category);
                    // 초기화 할 경우, 각 카테고리 상세 정보를 초기화 한다.
                    this.productImageRepository.deleteByGenreCd(category.getId());
                    this.productRepository.deleteByGenreCd(category.getId());
                    this.makerRepository.deleteByCategoryNo(category.getId());
                    this.brandRepository.deleteByCategoryNo(category.getId());
                    this.keijoRepository.deleteByCategoryNo(category.getId());
                    makerMap.put(category.getId(), new ArrayList<>());
                    brandMap.put(category.getId(), new ArrayList<>());
                    keijoMap.put(category.getId(), new ArrayList<>());
                    if (YongaUtil.isNotEmpty(categoryDetailInfo.getMakerListInfo())) {
                        categoryDetailInfo.getMakerListInfo().stream()
                                .map(maker -> Maker.valueOf(maker, category))
                                .forEach(maker -> {
                            makerMap.get(category.getId()).add(maker);
                            this.makerRepository.save(maker);
                        });
                    }
                    if (YongaUtil.isNotEmpty(categoryDetailInfo.getBrandTypeListInfo())) {
                        categoryDetailInfo.getBrandTypeListInfo().stream().map(brand -> Brand.valueOf(brand, category)).forEach(brand -> {
                            brandMap.get(category.getId()).add(brand);
                            this.brandRepository.save(brand);
                        });
                    }
                    if (YongaUtil.isNotEmpty(categoryDetailInfo.getKeijoListInfo())) {
                        categoryDetailInfo.getKeijoListInfo().stream().map(keijo -> Keijo.valueOf(keijo, category)).forEach(keijo -> {
                            keijoMap.get(category.getId()).add(keijo);
                            this.keijoRepository.save(keijo);
                        });
                    }
                    Integer productNum = this.dataExtractor.extractProductNum(category);
                    category.setTotalProductNum(productNum);
                    category.setKaisaiKaisu(ConfigConstants.AUCTION_INFO.getKaisaiKaisu());
                    category.setStatus("INIT");
                    category.setExtProductNum(0);
                    category.setModifiedDate(new Date());
                    this.categoryService.save(category);
                });
            }
            // 5. 카테고리 별 기본 정보 조회
            if (this.extractMode.isRequiredInitialize()) {
                this.targetCategoryList.stream().forEach(category -> {
                    List<Maker> makerList = makerMap.get(category.getId());
                    List<Brand> brandList = brandMap.get(category.getId());
                    List<Keijo> keijoList = keijoMap.get(category.getId());
                    ProductList productList = null;
                    AtomicInteger page = new AtomicInteger(0);
                    while ((productList = this.dataExtractor.extractProductList(category, page.getAndIncrement())) != null) {
                        if (YongaUtil.isNullOrEmpty(productList.getContent())) {
                            break;
                        }
                        log.info("extract category [{}], elements [{}]", category.getKorean(), productList.getSize());
                        AtomicInteger extractProduct = new AtomicInteger(0);
                        productList.getContent().stream().forEach(p -> {
                            // maker / brand / keijo 가 없는 경우, 미리 설정
                            boolean makerExists = makerList.stream().anyMatch(m -> m.getMakerKey().getMakerCd().equals(p.getMakerCd()));
                            boolean brandExists = brandList.stream().anyMatch(b -> b.getBrandCd().equals(p.getBrandTypeCd()));
                            boolean keijoExists = keijoList.stream().anyMatch(k -> k.getKeijoCd().equals(p.getKeijoCd()));
                            if (makerExists && brandExists && keijoExists) {
                            } else {
                                log.info("제품 : maker [{}] exists [{}], brand [{}] exists [{}], keijo [{}] exists [{}]", p.getMakerCd(), makerExists, p.getBrandTypeCd(), brandExists, p.getKeijoCd(), keijoExists);
                                if (!makerExists) {
                                    Maker maker = new Maker();
                                    MakerKey key = new MakerKey();
                                    key.setCategoryNo(category.getId());
                                    key.setMakerCd(p.getMakerCd());
                                    maker.setMakerKey(key);
                                    maker.setName(p.getMaker());
                                    maker.setNameKr(p.getMaker());
                                    maker.setNameEn(p.getMaker());
                                    this.makerRepository.save(maker);
                                }
                                if (!brandExists) {
                                    Brand brand = new Brand();
                                    BrandKey key = new BrandKey();
                                    key.setBrandCd(p.getBrandTypeCd());
                                    key.setCategoryNo(category.getId());
                                    brand.setBrandKey(key);
                                    brand.setName(p.getBrandType());
                                    brand.setNameEn(p.getBrandTypeEn());
                                    brand.setNameKr(p.getBrandType());
                                    this.brandRepository.save(brand);
                                }
                                if (!keijoExists) {
                                    Keijo keijo = new Keijo();
                                    KeijoKey key = new KeijoKey();
                                    key.setKeijoCd(p.getKeijoCd());
                                    key.setCategoryNo(category.getId());
                                    keijo.setKeijoKey(key);
                                    keijo.setName(p.getKeijo());
                                    keijo.setNameEn(p.getKeijoEn());
                                    keijo.setNameKr(p.getKeijo());
                                    this.keijoRepository.save(keijo);
                                }
                            }
                            Product product = new Product(p);
                            product.setExtractResult(ExtractResult.INITIALIZE);
                            log.info("제품 : maker [{}], brand [{}], keijo [{}]", p.getMakerCd(), p.getBrandTypeCd(), p.getKeijoCd());
                            this.productRepository.save(product);
                            extractProduct.incrementAndGet();
                        });
                    }
                });
            }
            // 6. 카테고리 별 상세 정보 조회
            this.targetCategoryList.stream().forEach(category -> {
                category.setStatus("PROGRESS");
                this.categoryService.save(category);
                List<Product> notCollectedProductList = this.productRepository.findProductByNotCollected(category.getId());
                category.setExtProductNum(category.getTotalProductNum() - notCollectedProductList.size());
                this.categoryService.save(category);
                log.info("not collected product list [{}]", notCollectedProductList == null ? 0 : notCollectedProductList.size());
                AtomicInteger extractProductNum = new AtomicInteger(0);
                if (!YongaUtil.isNullOrEmpty(notCollectedProductList)) {
                    this.logService.addLog(String.format("카테고리 [%s] 의 제품 [%s] 개를 상세 추출 합니다. ", category.getKorean(), notCollectedProductList.size()));
                    notCollectedProductList.stream().forEach(product -> {
                        Product productDetail = this.dataExtractor.extractProduct(category, product);
                        if (YongaUtil.isNotNull(productDetail)) {
                            if (!YongaUtil.isNullOrEmpty(productDetail.getProductImage())) {
                                this.productImageRepository.saveAll(productDetail.getProductImage());
                            }
                            productDetail.setExtractResult(ExtractResult.COMPLETE);
                            this.productRepository.save(productDetail);
                            extractProductNum.incrementAndGet();
                            if (extractProductNum.get() % 10 == 0) {
                                // 10 번 추출 시마다, 현재 추출한 데이터를 update
                                category.setExtProductNum(category.getExtProductNum() + 10);
                                this.categoryService.save(category);
                            }
                        }
                    });
                    notCollectedProductList = this.productRepository.findProductByNotCollected(category.getId());
                }
                category.setExtProductNum(category.getTotalProductNum() - (notCollectedProductList == null || notCollectedProductList.isEmpty() ? 0 : notCollectedProductList.size()));
                category.setModifiedDate(new Date());
                category.setStatus("COMPLETE");
                this.categoryService.save(category);
            });
            this.logService.addLog("데이터 상세 추출을 완료하였습니다. Total[" + totalExtractNum.get() + "]");
            this.configService.setConfigValue("EXECUTOR", "STATUS", "COMPLETE");
            this.configService.setConfigValue("EXECUTOR", "MESSAGE", "완료");
        } catch (Throwable e) {
            log.error(e.getMessage());
            this.logService.addLog(e.getMessage());
            if (YongaUtil.isNotNull(e.getCause())) {
                this.logService.addLog("error [" + YongaUtil.getString(e.getCause().toString(), 230) + "]");
            }
            this.configService.setConfigValue("EXECUTOR", "STATUS", "FAIL");
            this.configService.setConfigValue("EXECUTOR", "MESSAGE", e.getMessage());
        } finally {
            if (dataExtractor != null) {
                this.dataExtractor.close();
            }
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
