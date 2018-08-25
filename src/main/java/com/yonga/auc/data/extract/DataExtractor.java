package com.yonga.auc.data.extract;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.extract.DataExtractException.ExtractExceptionMessage;
import com.yonga.auc.data.product.Product;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataExtractor {

	private static final int PAGE_SIZE = 100;
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
	public boolean extractProductList(List<Category> targetCategoryList, Consumer<Category> foundCategoryProductConsumer, BiConsumer<Category, List<Product>> extractConsumer, Consumer<Category> extractedCategoryConsumer) throws DataExtractException {
		Objects.requireNonNull(targetCategoryList);
		Objects.requireNonNull(extractConsumer);
		try {
			if (!login()) {
				throw new DataExtractException(ExtractExceptionMessage.LOGIN_FAIL);
			}
			targetCategoryList.stream().forEach(category -> {
				loadMainMenu(category);
				
				$("input#list-view-count").click();
				SelenideElement pageSizeCombo = $$("div.x-combo-list-inner div.x-combo-list-item").stream().filter(d->d.getText().equals(String.valueOf(PAGE_SIZE))).findFirst().orElse(null);
				if (pageSizeCombo != null) {
					pageSizeCombo.click();
				}
				DataExtractorUtil.waitLoading();
				AtomicInteger productNo = new AtomicInteger(1);
				int foundProductPageNums = foundTotalProductNum(category, foundCategoryProductConsumer);
				IntStream.rangeClosed(1, foundProductPageNums).boxed().map(String::valueOf).forEach(pageNo-> {
					long start = System.currentTimeMillis();
					// move page
					SelenideElement currentProductPage = $("input.x-tbar-page-number");
					currentProductPage.setValue(pageNo).pressEnter();
					DataExtractorUtil.waitLoading();
					log.info("1. move page [{}]", (System.currentTimeMillis() - start)); start = System.currentTimeMillis();
					
					// export html file
					File htmlFile = exportHtmlFiles(Paths.get(this.siteInfo.getWorkRoot(), this.siteInfo.getWorkPathList(), String.format("%s_%s_%s.html", category.getId(), category.getKorean(), pageNo)));
					log.info("2. export html file [{}]", (System.currentTimeMillis() - start)); start = System.currentTimeMillis();
					extractConsumer.accept(category, extractProductList(category, productNo, htmlFile));
					log.info("4. extraction consumer [{}]", (System.currentTimeMillis() - start)); start = System.currentTimeMillis();
				});
				extractedCategoryConsumer.accept(category);
			});
			return true;
		} finally {
			logout();
		}
	}
	public boolean extractProductDetail(List<Category> targetCategoryList, BiConsumer<Category, Product> extractConsumer) throws DataExtractException {
		Objects.requireNonNull(targetCategoryList);
		Objects.requireNonNull(extractConsumer);
		try {
			if (!login()) {
				throw new DataExtractException(ExtractExceptionMessage.LOGIN_FAIL);
			}
			targetCategoryList.stream().forEach(category -> {
				loadMainMenu(category);
				
				if (hasProductDetail()) {
					int foundProductPageNums = category.getTotalProductNum();
					IntStream.rangeClosed(1, foundProductPageNums).boxed().map(String::valueOf).forEach(productNo -> {
						long start = System.currentTimeMillis();
						// move product page
						SelenideElement currentProductPage = $("input.x-tbar-page-number");
						currentProductPage.setValue(productNo).pressEnter();
						DataExtractorUtil.waitLoading();
						// export html file
						File htmlFile = exportHtmlFiles(Paths.get(this.siteInfo.getWorkRoot(), this.siteInfo.getWorkPathDetail(), String.format("%s_%s_%s.html", category.getId(), category.getKorean(), productNo)));
						extractConsumer.accept(category, extractProduct(category, htmlFile));
						log.info(String.format("extract Product %s %s %s", category.getKorean(), productNo, (System.currentTimeMillis()-start)));
					});
				}
			});
			return true;
		} finally {
			logout();
		}
	}
	public int foundTotalProductNum(Category category, Consumer<Category> foundCategoryProductConsumer) {
		String pageInfo = DataExtractorUtil.getText($("div.x-paging-info"));
		Objects.requireNonNull(pageInfo);
		if (pageInfo.contains("下見情報が見つかりませんでした")) {
			category.setTotalProductNum(0);
		} else if (pageInfo.contains("件中")) {
			Integer totalProductNum = Integer.valueOf(YongaUtil.getMatchedGroup(pageInfo, "(\\d+)件中"));
			category.setTotalProductNum(totalProductNum);
		}
		foundCategoryProductConsumer.accept(category);
		return YongaUtil.calculateTotalPage(category.getTotalProductNum(), PAGE_SIZE);
	}

	public boolean hasProducts(Category category) {
		String pageInfo = DataExtractorUtil.getText($("div.x-paging-info"));
		Objects.requireNonNull(pageInfo);
		if (pageInfo.contains("下見情報が見つかりませんでした")) {
			return false;
		}
		if (pageInfo.contains("件中")) {
			Integer totalProductNum = Integer.valueOf(YongaUtil.getMatchedGroup(pageInfo, "(\\d+)件中"));
			Integer currentProductNum = Integer.valueOf(YongaUtil.getMatchedGroup(pageInfo, "(\\d+)件目?を表示"));
			return totalProductNum > currentProductNum;
		}
		return false;
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
	private Product extractProduct(Category category, File htmlFile) {
		Document doc = DataExtractorUtil.getJsoupDocument(htmlFile);
		String uketsukeNo = doc.select("span#desc-uketsuke-no").first().text();
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
				.map(t->StringUtils.replaceAll(t, "[ 　\r\n]", ""))
				.filter(t->!t.isEmpty())
				.collect(Collectors.joining(", "));

		Product extractedProduct = new Product();
		extractedProduct.setUketsukeNo(uketsukeNo);
		extractedProduct.setSalesPoint(salesPoint);
		extractedProduct.setSalesPoint2(salesPoint2);
		extractedProduct.setAccessories(accessories);
		extractedProduct.setSeriBng(seriBng);
		extractedProduct.setOpenCount(openCount);
		extractedProduct.setOpenDate(openDate);
		extractedProduct.setStart(start);
		extractedProduct.setResult(result);
		extractedProduct.setNote(bikoInfo);
		return extractedProduct;
	}
	private List<Product> extractProductList(Category category, AtomicInteger productNo, File htmlFile) {
		Document doc = DataExtractorUtil.getJsoupDocument(htmlFile);
		
		// 접수번호
		List<String> uketsukeNoList = doc.select("div.x-grid3-body div.x-grid3-row table.x-grid3-row-table tbody tr div.x-grid3-col-clmUketsukeNo").stream().map(e->e.text()).collect(Collectors.toList());
		// 메이커
		List<String> makerList =      doc.select("div.x-grid3-body div.x-grid3-row table.x-grid3-row-table tbody tr div.x-grid3-col-clmMakerNm").stream().map(e->e.text()).collect(Collectors.toList());
		// 타입
		List<String> keijoList =      doc.select("div.x-grid3-body div.x-grid3-row table.x-grid3-row-table tbody tr div.x-grid3-col-clmKindNm").stream().map(e->e.text()).collect(Collectors.toList());
		// 형상(type)
		List<String> typeList =       doc.select("div.x-grid3-body div.x-grid3-row table.x-grid3-row-table tbody tr div.x-grid3-col-clmTypeNm").stream().map(e->e.text()).collect(Collectors.toList());
		// 상품명
		List<String> itemList =       doc.select("div.x-grid3-body div.x-grid3-row table.x-grid3-row-table tbody tr div.x-grid3-col-clmItemNm").stream().map(e->e.text()).collect(Collectors.toList());
		// 평가
		List<String> ratingList =     doc.select("div.x-grid3-body div.x-grid3-row table.x-grid3-row-table tbody tr div.x-grid3-col-clmclass").stream().map(e->e.text()).collect(Collectors.toList());

		List<Product> extractedProductList = new ArrayList<Product>();
		for(int i = 0; i< uketsukeNoList.size(); i++) {
			Product product = new Product();
			product.setUketsukeNo(uketsukeNoList.get(i).replaceAll("[^\\d\\-]", ""));
			product.setName(uketsukeNoList.get(i));
			product.setProductNo(productNo.getAndIncrement());
			
			product.setMaker(makerList.get(i));
			product.setKeijo(keijoList.get(i));
			product.setType(typeList.get(i));
			product.setItemName(itemList.get(i));
			product.setRating(ratingList.get(i));
			product.setCategory(category);
			product.setCreateDate(new Date());
			extractedProductList.add(product);
		}
		return extractedProductList;
	}
}