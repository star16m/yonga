package com.yonga.auc.data.extract;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.product.Product;

public class DataExtractor {

	private String uid;
	private String pwd;
	private String targetProtocol;
	private String targetHost;
	private String workPath;
	public DataExtractor(Map<String, String> initializeInfoMap) {
		initialize(initializeInfoMap);
	}
	private String getTargetURL() {
		return getTargetURL("");
	}
	private String getTargetURL(String path) {
		return String.format("%s://%s/%s", this.targetProtocol, this.targetHost, path);
	}
	private void initialize(Map<String, String> initializeInfoMap) {
		if (!initializeInfoMap.containsKey("uid") || !initializeInfoMap.containsKey("pwd") || !initializeInfoMap.containsKey("targetProtocol") || !initializeInfoMap.containsKey("targetHost")) {
			throw new IllegalArgumentException("uid and pwd is required parameter");
		}
		this.uid = initializeInfoMap.get("uid");
		this.pwd = initializeInfoMap.get("pwd");
		this.targetProtocol = initializeInfoMap.get("targetProtocol");
		this.targetHost = initializeInfoMap.get("targetHost");
		this.workPath = initializeInfoMap.get("work.dir");
		String executor = initializeInfoMap.get("executor");
		System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, StringUtils.defaultString(executor, "driver/chromedriver.exe"));
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
		open(getTargetURL());
		$("input[name=uid]").setValue(uid);
		$("input[name=pwd]").setValue(pwd);
		$("input[name=imageField]").click();
		try {
			Alert alert = WebDriverRunner.getWebDriver().switchTo().alert();
			alert.accept();
			SelenideElement loginInfoElement = $("div#divUserData");
			loginInfoElement.shouldHave(text(this.uid));
			loginInfoElement.shouldHave(text("様"));
			$("div#navigation ul li a span").shouldHave(text("下見検索"));
		} catch (final NoAlertPresentException e) {
			return true;
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	private void loadMainMenu(Category category) {
		// select category
		open(getTargetURL("ba_menu.aspx?ud=0"));
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
	private void logout() {
		$("a#exitlink").click();
		WebDriverRunner.getWebDriver().close();
	}
	public void extractAll(List<Category> targetCategoryList, BiConsumer<Category, List<Product>> extractConsumer) {
		Objects.requireNonNull(targetCategoryList);
		Objects.requireNonNull(extractConsumer);
		boolean loginSuccess = login();
		if (loginSuccess) {
			targetCategoryList.stream().forEach(category -> {
				loadMainMenu(category);
				
				AtomicInteger currentPageNo = new AtomicInteger(1);
				AtomicInteger productNo = new AtomicInteger(1);
				$("input#list-view-count").click();
				SelenideElement pageSizeCombo = $$("div.x-combo-list-inner div.x-combo-list-item").stream().filter(d->d.getText().equals("100")).findFirst().orElse(null);
				if (pageSizeCombo != null) {
					pageSizeCombo.click();
				}
				// clean directory
				cleanHtmlFiles();
				List<Product> productList;
				do {
					long start = System.currentTimeMillis();
					// move page
					SelenideElement currentProductPage = $("input.x-tbar-page-number");
					String currentPage = String.valueOf(currentPageNo.getAndIncrement());
					currentProductPage.setValue(currentPage).pressEnter();
					$("div.ext-el-mask-msg.x-mask-loading").shouldNotBe(Condition.visible);
					
					// export html file
					File htmlFile = exportHtmlFiles(category, productNo, currentPage);
					productList = extractAll(category, productNo, htmlFile);
					extractConsumer.accept(category, productList);
					System.out.println(String.format("extract %s %s", category.getKorean(), (System.currentTimeMillis()-start)));
				} while(hasMoreProducts(category));
			});
		}
		logout();
	}
	
	public boolean hasMoreProducts(Category category) {
		String pageInfo = DataExtractorUtil.getText($("div.x-paging-info"));
		Objects.requireNonNull(pageInfo);
		if (pageInfo.contains("下見情報が見つかりませんでした")) {
			category.setTotalProductNum(0);
			return false;
		}
		if (pageInfo.contains("件中")) {
			Integer totalProductNum = Integer.valueOf(YongaUtil.getMatchedGroup(pageInfo, "(\\d+)件中"));
			Integer currentProductNum = Integer.valueOf(YongaUtil.getMatchedGroup(pageInfo, "(\\d+)件を表示"));
			category.setTotalProductNum(totalProductNum);
			category.setExtProductNum(currentProductNum);
			return totalProductNum > currentProductNum;
		}
		return false;
	}
	private boolean cleanHtmlFiles() {
		File htmlFile = Paths.get(this.workPath).toFile();
		try {
			if (htmlFile.exists()) {
				FileUtils.cleanDirectory(htmlFile);
			} else {
				FileUtils.forceMkdir(htmlFile);
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	private File exportHtmlFiles(Category category, AtomicInteger productNo, String pageNo) {
		File htmlFile = Paths.get(this.workPath, String.format("%s(%s)_%s.html", category.getKorean(), category.getId(), pageNo)).toFile();
		try {
			FileUtils.write(htmlFile, WebDriverRunner.getWebDriver().getPageSource(), Charset.forName("UTF-8"));
		} catch (IOException e) {
			return null;
		}
		return htmlFile;
	}
	private List<Product> extractAll(Category category, AtomicInteger productNo, File htmlFile) {
		List<Product> extractedProductList = new ArrayList<Product>();
		Document doc = null;
		try {
			doc = Jsoup.parse(htmlFile, "UTF-8");
		} catch (IOException e) {
			return null;
		}
		
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
		// 개최정보
		List<String> openInfoList =   doc.select("div.x-grid3-body div.x-grid3-row table.x-grid3-row-table tbody tr div.x-grid3-col-clmTrd").stream().map(e->e.text()).collect(Collectors.toList());
		// 평가
		List<String> ratingList =     doc.select("div.x-grid3-body div.x-grid3-row table.x-grid3-row-table tbody tr div.x-grid3-col-clmclass").stream().map(e->e.text()).collect(Collectors.toList());
		// 스타트
		List<String> startList =      doc.select("div.x-grid3-body div.x-grid3-row table.x-grid3-row-table tbody tr div.x-grid3-col-clmFlwNum").stream().map(e->e.text()).collect(Collectors.toList());
		// action 결과
		List<String> resultList =     doc.select("div.x-grid3-body div.x-grid3-row table.x-grid3-row-table tbody tr div.x-grid3-col-clmProdPlc").stream().map(e->e.text()).collect(Collectors.toList());
		for(int i = 0; i< uketsukeNoList.size(); i++) {
			Product product = new Product();
			product.setUketsukeNo(uketsukeNoList.get(i).replaceAll("[^\\d\\-]", ""));
			product.setName(uketsukeNoList.get(i));
			product.setProductNo(productNo.getAndIncrement());
			
			product.setMaker(makerList.get(i));
			product.setKeijo(keijoList.get(i));
			product.setType(typeList.get(i));
			product.setItemName(itemList.get(i));
			String openInfo = openInfoList.get(i);
			String openCount = YongaUtil.getMatchedGroup(openInfo, "(.+)\n");
			String openDate = YongaUtil.getMatchedGroup(openInfo, "\n(.+)");
			product.setOpenCount(openCount);
			product.setOpenDate(openDate);
			product.setRating(ratingList.get(i));
			product.setStart(startList.get(i));
			product.setResult(resultList.get(i));
			product.setCategory(category);
			product.setCreateDate(new Date());
			extractedProductList.add(product);
		}
		return extractedProductList;
	}
}