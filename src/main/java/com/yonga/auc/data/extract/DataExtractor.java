package com.yonga.auc.data.extract;

import com.codeborne.selenide.*;
import com.codeborne.selenide.ex.ElementNotFound;
import com.codeborne.selenide.ex.UIAssertionError;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.category.AuctionInfo;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.category.CategoryDetailInfo;
import com.yonga.auc.data.category.CategoryInfo;
import com.yonga.auc.data.common.PageResponse;
import com.yonga.auc.data.extract.DataExtractException.ExtractExceptionMessage;
import com.yonga.auc.data.product.Product;
import com.yonga.auc.data.product.ProductDto;
import com.yonga.auc.data.product.ProductList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

@Slf4j
public class DataExtractor {

    private ExtractSiteInfo siteInfo;

    public DataExtractor(ExtractSiteInfo siteInfo, boolean showExtractView) {
        this.siteInfo = siteInfo;
        System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, StringUtils.defaultString(this.siteInfo.getExecutor(), "driver/chromedriver.exe"));
        Configuration.browser = WebDriverRunner.CHROME;
        Configuration.savePageSource = false;
        Configuration.screenshots = false;
        Configuration.timeout = 10000;
        ChromeOptions options = new ChromeOptions();
		options.setHeadless(!showExtractView);
        options.addArguments("disable-infobars");
        options.addArguments("—start-maximized");
        options.addArguments("—disable-application-cache");
        WebDriver driver = new ChromeDriver(options);
        WebDriverRunner.setWebDriver(driver);
    }

    public void login() throws DataExtractException {
        open(this.siteInfo.getTargetURL());
        try {
            $("input[name=username]").setValue(this.siteInfo.getUid());
            $("input[name=password]").setValue(this.siteInfo.getPwd());
            $("button.c-btn--login").click();
            SelenideElement errorDetail = $("li#error_detail");
            if (errorDetail != null && errorDetail.exists()) {
                log.error("Error displayed [{}]", errorDetail.text());
                throw new DataExtractException(ExtractExceptionMessage.LOGIN_FAIL);
            }
            SelenideElement loginInfoElement = $("div.kaiinInfo");
            loginInfoElement.shouldHave(text(this.siteInfo.getUid()));
            loginInfoElement.shouldHave(text("様"));
        } catch (DataExtractException e) {
            throw e;
        } catch (UIAssertionError e) {
            ExtractExceptionMessage extractExceptionMessage = null;
            try {
                $("body").shouldBe(Condition.exist, text("メンテナンス中"));
                extractExceptionMessage = ExtractExceptionMessage.MAINTENANCE;
            } catch (Throwable e1) {
                // ignorred.
            }
            log.error("error occurrred while extract.", e);
            if (YongaUtil.isNull(extractExceptionMessage)) {
                extractExceptionMessage = ExtractExceptionMessage.LOGIN_FAIL;
            }
            throw new DataExtractException(extractExceptionMessage);
        }
    }

    private <T> T extract(Class<T> classType, String url, Path path) {
        open(url);
        File categoryFile = extractTargetElement(path, $("body"));
        Gson gson = new Gson();
        try (JsonReader reader = new JsonReader(new FileReader(categoryFile))) {
            return gson.fromJson(reader, classType);
        } catch (IOException e) {
            log.error("Error occurred while extracting data from [{}]. [{}]", url, e.getMessage(), e);
        }
        return null;
    }

    private void logout() {
        SelenideElement exitLink = $("#pc_header > div > div.info > ul > li.button");
        if (exitLink.exists()) {
            exitLink.click();
        } else {
            open("https://u.brand-auc.com");
            exitLink = $("#pc_header > div > div.info > ul > li.button");
            if (exitLink.exists()) {
                exitLink.click();
                exitLink = $("div.modal-position > div.modal > div.button_area > button.common_btn.deep > div");
                if (exitLink.exists()) {
                    exitLink.click();
                }
            }
        }
        WebDriverRunner.getWebDriver().close();
    }

    public AuctionInfo extractAuctionInfo() {
        // 옥션 정보 추출
        AuctionInfo auctionInfo = extract(AuctionInfo.class,
                "https://u.brand-auc.com/api/v1/auction/auctionInfos/auctionOpenInfo",
                Paths.get(this.siteInfo.getWorkRoot(), "auctioninfo.json"));
        auctionInfo.setExtractDate(LocalDateTime.now());
        return auctionInfo;
    }
    public CategoryDetailInfo extractCategoryDetail(Category category) {
        CategoryDetailInfo categoryDetailInfo = extract(CategoryDetailInfo.class,
                String.format("https://u.brand-auc.com/api/v1/auction/searchHeader/options/previewSearch?genreCds=%s&gamenId=B02-01", category.getId()),
                Paths.get(this.siteInfo.getWorkRoot(),String.format("category_detail_info_%s_%s.json", category.getId(), category.getKorean())));
        Objects.requireNonNull(categoryDetailInfo);
        // 5.2. 전체 메이커 지정하여 다시 한번 추출.
        categoryDetailInfo = extract(CategoryDetailInfo.class,
                String.format("https://u.brand-auc.com/api/v1/auction/searchHeader/options/previewSearch?genreCds=%s&makerCds=%s&gamenId=B02-01", category.getId(), categoryDetailInfo.getMakerListInfo().stream().map(maker -> String.valueOf(maker.getMakerCd())).collect(Collectors.joining(","))),
                Paths.get(this.siteInfo.getWorkRoot(),String.format("category_detail_info_%s_%s.json", category.getId(), category.getKorean())));
        return categoryDetailInfo;
    }
    public boolean extractProductDetail(
            List<Category> targetCategoryList,
            BiFunction<Category, ProductList, List<Product>> extractProductListFunction,
            BiConsumer<Category, Product> extractProductDetailConsumer,
            BiConsumer<Category, Product> failedExtractProductConsumer,
            BiConsumer<Category, Integer> completedCategoryProductConsumer
    ) throws DataExtractException {
        Objects.requireNonNull(targetCategoryList);
        if (!YongaUtil.isNotEmpty(targetCategoryList)) {
            log.error("대상 카테고리가 지정되어 있지 않습니다.");
            throw new DataExtractException(ExtractExceptionMessage.NOT_FOUND_CATEGORY);
        }
        Objects.requireNonNull(extractProductDetailConsumer);
        try {
            // 1. 로그인
            login();



            // 3. 카테고리 정보
            CategoryInfo[] categoryInfoArray = extract(CategoryInfo[].class,
                    "https://u.brand-auc.com/api/v1/com/allGenre",
                    Paths.get(this.siteInfo.getWorkRoot(), "allCategory.json"));
            if (YongaUtil.isNullOrEmpty(categoryInfoArray)) {
                throw new DataExtractException(ExtractExceptionMessage.FAIL_EXTRACT_ALL_CATEGORY);
            }

            // 4. 카테고리별로 데이터 추출
            targetCategoryList.stream().forEach(category -> {
                AtomicInteger currentPageIndex = new AtomicInteger(0);
                AtomicInteger currentProductDetailNum = new AtomicInteger(0);
                ProductList productList = null;
                while ((productList = extract(ProductList.class,
                        String.format("https://u.brand-auc.com/api/v1/auction/previewItems/list?genreCds=%s&gamenId=B02-01&page=%s&pageNumber=0&viewType=1&albumTorokuSort=0&size=100", category.getId(), currentPageIndex.get()),
                        Paths.get(this.siteInfo.getWorkRoot(), this.siteInfo.getWorkPathDetail(), String.format("category_%s", category.getId()), String.format("list_%s.json", currentPageIndex.getAndIncrement())))) != null) {
                    log.debug("extract product list [{}]", productList);

                    List<Product> initializedProductList = extractProductListFunction.apply(category, productList);
                    if (YongaUtil.isNotEmpty(initializedProductList)) {
                        // extract product detail
                        initializedProductList.stream().forEach(product -> {
                            ProductDto productDto = extract(ProductDto.class, product.getDetailUrl(),
                                    Paths.get(this.siteInfo.getWorkRoot(),
                                            this.siteInfo.getWorkPathDetail(),
                                            String.format("category_%s", category.getId()),
                                            String.format("product_%s.json", product.getUketsukeBng())));
                            if (YongaUtil.isNull(productDto) || YongaUtil.isNull(productDto.getUketsukeBng())) {
                                String code = YongaUtil.isNull(productDto) ? "" : productDto.getCode();
                                String message = YongaUtil.isNull(productDto) ? "" : productDto.getMessage();
                                log.info("제품 추출에 실패하였습니다. code [{}], message [{}], product [{}]", code, message, productDto);
                                failedExtractProductConsumer.accept(category, product);
                                return;
                            }
                            productDto.setKaijoCd(product.getKaijoCd());
                            productDto.setHyoka(product.getHyoka());
                            productDto.setHyokaGaiso(product.getHyokaGaiso());
                            productDto.setHyokaNaiso(product.getHyokaNaiso());

                            Product productDetail = new Product(productDto);

                            extractProductDetailConsumer.accept(category, productDetail);
                            currentProductDetailNum.incrementAndGet();
                        });
                    }
                    if (productList.getLast()) {
                        completedCategoryProductConsumer.accept(category, currentProductDetailNum.get());
                        break;
                    }
                }
//                extractedProductConsumer.accept(category, foundProductNums);
            });
            return true;
        } catch (DataExtractException e) {
            throw e;
        } catch (Throwable e) {
            log.error("에러가 발생하였습니다. [{}]", e.getMessage(), e);
            Arrays.asList(e.getStackTrace()).stream().forEach(s -> log.warn(s.toString()));
            throw new DataExtractException(ExtractExceptionMessage.UNKNOWN, e);
        } finally {
            logout();
        }
    }

    private File extractTargetElement(Path targetFilePath, SelenideElement targetElement) {
        File targetFile = targetFilePath.toFile();
        try {
            FileUtils.write(targetFile, targetElement.getText(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return targetFile;
    }

    public Integer extractProductNum(Category category) {
        PageResponse pageResponse = extract(PageResponse.class,
                String.format("https://u.brand-auc.com/api/v1/auction/previewItems/list?genreCds=%s&gamenId=B02-01&page=1&pageNumber=0&viewType=1&albumTorokuSort=0&size=10",
                        category.getId()),
                Paths.get(this.siteInfo.getWorkRoot(),
                        this.siteInfo.getWorkPathDetail(),
                        String.format("category_%s", category.getId()),
                        String.format("product_num.json"))
        );
        return pageResponse.getTotalElements();
    }

    public ProductList extractProductList(Category category, Integer page) {
        ProductList productList = extract(ProductList.class,
                String.format("https://u.brand-auc.com/api/v1/auction/previewItems/list?genreCds=%s&gamenId=B02-01&page=%s&pageNumber=0&viewType=1&albumTorokuSort=0&size=100",
                        category.getId(), page),
                Paths.get(this.siteInfo.getWorkRoot(), this.siteInfo.getWorkPathDetail(), String.format("category_%s", category.getId()), String.format("list_%s.json", page)));
        return productList;
    }

    public Product extractProduct(Category category, Product product) {
        ProductDto productDto = extract(ProductDto.class, product.getDetailUrl(),
                Paths.get(this.siteInfo.getWorkRoot(),
                        this.siteInfo.getWorkPathDetail(),
                        String.format("category_%s", category.getId()),
                        String.format("product_%s.json", product.getUketsukeBng())));
        if (YongaUtil.isNull(productDto) || YongaUtil.isNull(productDto.getUketsukeBng())) {
            String code = YongaUtil.isNull(productDto) ? "" : productDto.getCode();
            String message = YongaUtil.isNull(productDto) ? "" : productDto.getMessage();
            log.info("제품 추출에 실패하였습니다. code [{}], message [{}], product [{}]", code, message, productDto);
            return null;
        }
        productDto.setKaijoCd(product.getKaijoCd());
        productDto.setHyoka(product.getHyoka());
        productDto.setHyokaGaiso(product.getHyokaGaiso());
        productDto.setHyokaNaiso(product.getHyokaNaiso());

        Product productDetail = new Product(productDto);
        return productDetail;
    }

    public void close() {
        logout();
    }
}