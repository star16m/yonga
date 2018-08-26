package com.yonga.auc.data.extract;

import com.codeborne.selenide.*;
import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.extract.DataExtractException.ExtractExceptionMessage;
import com.yonga.auc.data.product.Product;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.data.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

@Slf4j
public class DataExtractor {

    private ExtractSiteInfo siteInfo;

    public DataExtractor(ExtractSiteInfo siteInfo) {
        this.siteInfo = siteInfo;

        System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, StringUtils.defaultString(this.siteInfo.getExecutor(), "driver/chromedriver.exe"));
        Configuration.browser = WebDriverRunner.CHROME;
        Configuration.savePageSource = false;
        Configuration.screenshots = false;
        Configuration.timeout = 10000;
        ChromeOptions options = new ChromeOptions();
//		options.setHeadless(true);
        options.addArguments("disable-infobars");
        options.addArguments("—start-maximized");
        options.addArguments("—disable-application-cache");
        WebDriver driver = new ChromeDriver(options);
        WebDriverRunner.setWebDriver(driver);
    }

    private boolean login() {
        open(this.siteInfo.getTargetURL());
        $("input[name=uid]").setValue(this.siteInfo.getUid());
        $("input[name=pwd]").setValue(this.siteInfo.getPwd());
        $("input[name=imageField]").click();
        try {
            Alert alert = WebDriverRunner.getWebDriver().switchTo().alert();
            String alertText = alert.getText();
            if (alertText.contains("すでにログインされているか")) {
                alert.accept();
                return false;
            }
            alert.accept();
            SelenideElement loginInfoElement = $("div#divUserData");
            loginInfoElement.shouldHave(text(this.siteInfo.getUid()));
            loginInfoElement.shouldHave(text("様"));
            $("div#navigation ul li a span").shouldHave(text("下見検索"));
        } catch (final NoAlertPresentException e) {
            return true;
        } catch (Exception e) {
            log.warn(e.getMessage());
            return false;
        }
        return true;
    }

    private void loadMainMenu(Category category) {
        // select category
        open(this.siteInfo.getTargetURL("ba_menu.aspx?ud=0"));
        // main menu check
        SelenideElement loginInfoElement = $("div#divUserData");
        SelenideElement menuElement = loginInfoElement.parent().parent();
        menuElement.shouldHave(text("②メイン画面（メニュー）"));

        // category click
        ElementsCollection atagCollection = $$("a.x-tree-node-anchor");
        atagCollection.shouldHaveSize(8);
        SelenideElement aTag = atagCollection.stream()
                .filter(a -> category.getJapanese().equals(a.getText()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Can't find category name. [%s]", category.getKorean())));
        aTag.click();
        // category selected check
        SelenideElement categoryNameElement = $("div.x-panel-tc span.x-panel-header-text");
        categoryNameElement.shouldHave(text(category.getJapanese()));
        // click search button
        $("a#seachlink").click();
        SelenideElement titleElement = $("div#mainPanel div span.x-panel-header-text");
        titleElement.shouldHave(text("出品一覧"));
    }

    private boolean hasProductDetail() {
        DataExtractorUtil.waitLoading();
        ElementsCollection productCollection = $$("div.x-grid3-cell-inner.x-grid3-col-clmUketsukeNo");
        productCollection.shouldBe(CollectionCondition.sizeGreaterThanOrEqual(0));
        if (productCollection != null && productCollection.size() > 0) {
            productCollection.get(0).click();
            DataExtractorUtil.waitLoading();
            SelenideElement titleElement = $("span.x-panel-header-text");
            titleElement.shouldHave(text("出品詳細"));
            return true;
        }
        return false;
    }

    private void logout() {
        SelenideElement exitLink = $("a#exitlink");
        if (exitLink.exists()) {
            exitLink.click();
        }
        WebDriverRunner.getWebDriver().close();
    }

    public boolean extractProductDetail(
            List<Category> targetCategoryList,
            BiConsumer<Category, Integer> foundTotalCategoryProductConsumer,
            BiConsumer<Category, Pair<Product, List<String>>> extractConsumer,
            BiConsumer<Category, Integer> extractedProductConsumer
    ) throws DataExtractException {
        Objects.requireNonNull(targetCategoryList);
        Objects.requireNonNull(extractConsumer);
        try {
            if (!login()) {
                throw new DataExtractException(ExtractExceptionMessage.LOGIN_FAIL);
            }
            targetCategoryList.stream().forEach(category -> {
                loadMainMenu(category);

                Integer foundProductNums = 0;
                if (hasProductDetail()) {
                    foundProductNums = foundTotalProductNum();
                    foundTotalCategoryProductConsumer.accept(category, foundProductNums);
                    IntStream.rangeClosed(1, foundProductNums).boxed().map(String::valueOf).forEach(productNo -> {
                        long start = System.currentTimeMillis();
                        // move product page
                        SelenideElement currentProductPage = $("input.x-tbar-page-number");
                        currentProductPage.setValue(productNo).pressEnter();
                        DataExtractorUtil.waitLoading();
                        // export html file
                        File htmlFile = exportHtmlFiles(Paths.get(this.siteInfo.getWorkRoot(), this.siteInfo.getWorkPathDetail(), String.format("%s_%s_%s.html", category.getId(), category.getKorean(), productNo)));
                        extractProduct(category, htmlFile, extractConsumer);
                        log.info(String.format("extract Product %s %s %s", category.getKorean(), productNo, (System.currentTimeMillis() - start)));
                    });
                }
                extractedProductConsumer.accept(category, foundProductNums);
            });
            return true;
        } catch (DataExtractException e) {
            throw e;
        } catch (Throwable e) {
            log.warn(e.getMessage());
            Arrays.asList(e.getStackTrace()).stream().forEach(s -> log.warn(s.toString()));
        } finally {
            logout();
        }
        return false;
    }

    public Integer foundTotalProductNum() {
        String pageInfo = DataExtractorUtil.getText($("div.x-paging-info"));
        Objects.requireNonNull(pageInfo);
        if (pageInfo.contains("下見情報が見つかりませんでした")) {
            return 0;
        }
        if (pageInfo.contains("件中")) {
            Integer totalProductNum = Integer.valueOf(YongaUtil.getMatchedGroup(pageInfo, "(\\d+)件中"));
            return totalProductNum;
        }
        return 0;
    }

    private File exportHtmlFiles(Path targetFilePath) {
        File htmlFile = targetFilePath.toFile();
        try {
            FileUtils.write(htmlFile, WebDriverRunner.getWebDriver().getPageSource(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return htmlFile;
    }

    private Product extractProduct(Category category, File htmlFile, BiConsumer<Category, Pair<Product, List<String>>> extractConsumer) {
        Document doc = DataExtractorUtil.getJsoupDocument(htmlFile);
        String uketsukeNo = doc.select("span#desc-uketsuke-no").first().text();
        String maker = doc.select("span#desc-maker").first().text();
        String keijo = doc.select("span#desc-keijo").first().text();
        String type = doc.select("span#desc-type").first().text();
        String itemName = doc.select("span#desc-item-name").first().text();
        String rating = doc.select("span#desc-rating").first().text();


        String salesPoint = doc.select("span#desc-sales-point").first().text().replaceAll("[\n　 ]+", ", ");
        String salesPoint2 = doc.select("span#desc-sales-point2").first().text().replaceAll("[\n　 ]+", ", ");
        String accessories = doc.select("span#desc-accessories").first().text().replaceAll("[\n　 ]+", ", ");
        String seriBng = doc.select("span#desc-seri-bng").first().text();
        String openCount = doc.select("span#desc-open-count").first().text();
        String openDate = doc.select("span#desc-open-date").first().text();
        String start = doc.select("span#desc-start").first().text();
        String result = doc.select("span#desc-result").first().text();
        String bikoInfo = doc.select("span#desc-note table tbody td.bikoTd").stream()
                .map(Element::text)
                .map(StringUtils::trim)
                .map(t -> StringUtils.replaceAll(t, "[ 　\r\n]", ""))
                .filter(t -> !t.isEmpty())
                .collect(Collectors.joining(", "));
        List<String> imageList = doc.select("img[id*=img]").stream()
                .filter(e -> e.hasAttr("src"))
                .map(e -> e.attr("src"))
                .filter(src -> src != null && !src.isEmpty() && src.contains("/ext_photos/") && src.contains("TN.jpg"))
                .map(src -> src.replaceAll("\\?.+", "").replaceAll(".+/ext_photos/", "ext_photos/"))
                .map(src -> this.siteInfo.getTargetURL() + src)
                .distinct()
                .collect(Collectors.toList());

        Product extractedProduct = new Product();
        extractedProduct.setUketsukeNo(uketsukeNo);
        extractedProduct.setKeijo(keijo);
        extractedProduct.setType(type);
        extractedProduct.setItemName(itemName);
        extractedProduct.setName(itemName);
        extractedProduct.setMaker(maker);
        extractedProduct.setRating(rating);
        extractedProduct.setSalesPoint(salesPoint);
        extractedProduct.setSalesPoint2(salesPoint2);
        extractedProduct.setAccessories(accessories);
        extractedProduct.setSeriBng(seriBng);
        extractedProduct.setOpenCount(openCount);
        extractedProduct.setOpenDate(openDate);
        extractedProduct.setStart(start);
        extractedProduct.setResult(result);
        extractedProduct.setNote(bikoInfo);

        extractedProduct.setCategory(category);
        extractedProduct.setCreateDate(new Date());
        extractConsumer.accept(category, Pair.of(extractedProduct, imageList));
        return extractedProduct;
    }
}