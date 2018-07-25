package com.yonga.auc.data.extract;

import static com.codeborne.selenide.Condition.text;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

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
		    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
		    	return true;
		    }
		} catch (IOException e) {
			return false;
		}
	    return false;
	}
}
