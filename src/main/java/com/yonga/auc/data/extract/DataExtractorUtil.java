package com.yonga.auc.data.extract;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataExtractorUtil {

	private DataExtractorUtil() {
	}

	public static void shouldBePresentText(SelenideElement element, String text) {
		element.shouldBe(Condition.exist, text(text));
	}

	public static String getText(SelenideElement element) {
		Objects.requireNonNull(element);
		element.shouldBe(Condition.exist);
		log.debug("element=[{}], text=[{}]", element, element.getText());
		return StringUtils.defaultString(element.getText());
	}

	public static String getText(SelenideElement element, String cssString) {
		return getText(element.find(cssString).shouldBe(Condition.exist));
	}

	public static boolean availableURL(String urlString) {
		try {
			HttpURLConnection.setFollowRedirects(false);
		    HttpURLConnection con = (HttpURLConnection) new URL(urlString).openConnection();
		    con.setRequestMethod("HEAD");
		    return con.getResponseCode() == HttpURLConnection.HTTP_OK;
		} catch (IOException e) {
			return false;
		}
	}
	public static void waitLoading() {
		$("#page_content > app-preview-search > main > app-loading div").shouldNotBe(Condition.visible);
	}
	public static Document getJsoupDocument(File file) {
		try {
			return Jsoup.parse(file, "UTF-8");
		} catch (IOException e) {
			log.warn("error occurred while parse jsoup document using file [{}]", file);
			return null;
		}
	}
}