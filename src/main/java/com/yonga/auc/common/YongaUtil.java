package com.yonga.auc.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class YongaUtil {

	private YongaUtil() {
	}

	public static String getString(String string) {
		return getString(string, "");
	}

	public static String getString(String string, String defaultString) {
		return isNotEmpty(string) ? string : defaultString;
	}

	public static boolean isNotEmpty(String string) {
		return StringUtils.isNotEmpty(string);
	}

	public static boolean isEmpty(String string) {
		return StringUtils.isEmpty(string);
	}

	public static String getMatchedGroup(String orgString, String patternString) {
		String replaceString = new String(orgString);
		if (!isEmpty(patternString)) {
			Pattern p = Pattern.compile(patternString);
			Matcher m = p.matcher(replaceString);
			if (m.find() && m.groupCount() > 0) {
				replaceString = m.group(1);
			}
		}
		return replaceString;
	}
}
