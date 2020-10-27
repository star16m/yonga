package com.yonga.auc.common;

import com.yonga.auc.data.category.CategoryInfo;
import com.yonga.auc.data.product.ProductDto;
import com.yonga.auc.data.product.image.ProductImage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class YongaUtil {

    public static DateTimeFormatter COMMON_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM월 dd일 (EEEE) HH:mm", Locale.KOREAN);
    public static DateTimeFormatter COMMON_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM월 dd일 (EEEE)", Locale.KOREAN);
    private YongaUtil() {
    }

    public static String getString(LocalDateTime localDateTime) {
        return getString(localDateTime, COMMON_DATE_TIME_FORMATTER);
    }
    public static String getString(LocalDate localDate, DateTimeFormatter dateTimeFormatter) {
        if (isNull(localDate)) {
            return "";
        }
        return localDate.format(dateTimeFormatter);
    }
    public static String getString(LocalDateTime localDateTime, DateTimeFormatter dateTimeFormatter) {
        if (isNull(localDateTime)) {
            return "";
        }
        return localDateTime.format(dateTimeFormatter);
    }
    public static String getString(String string) {
        return getString(string, "");
    }
    public static String getString(String string, int maxLength) {
        return getString(string).substring(0, maxLength);
    }

    public static String getString(String string, String defaultString) {
        return isNotEmpty(string) ? string : defaultString;
    }
    public static Boolean getBoolean(String string) {
        if (isEmpty(string)) {
            return false;
        }
        return Boolean.getBoolean(string);
    }
    public static String getDateString(String formattedString) {
        if (isEmpty(formattedString)) {
            return "";
        }
        if (formattedString.length() == 8 && formattedString.startsWith("20") && isNumeric(formattedString)) {
            String yyyy = StringUtils.substring(formattedString, 0, 4);
            String mm = StringUtils.substring(formattedString, 4, 6);
            String dd = StringUtils.substring(formattedString, 6, 8);
            if (isNumeric(yyyy) && isNumeric(mm) && isNumeric(dd)) {
                getString(LocalDate.of(getNumeric(yyyy), getNumeric(mm), getNumeric(dd)), COMMON_DATE_FORMATTER);
            }
        }
        return formattedString;
    }
    public static boolean isNumeric(String string) {
        return isNotEmpty(string) && StringUtils.isNumeric(string);
    }
    public static Integer getNumeric(String string) {
        return Integer.valueOf(string);
    }

    public static boolean isNotEmpty(String string) {
        return StringUtils.isNotEmpty(string);
    }

    public static boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    public static boolean isNull(Object object) {
        return object == null;
    }

    public static boolean isNotNull(Object object) {
        return object != null;
    }

    public static boolean isEmpty(Object object) {
        return object == null || isEmpty(object.toString());
    }

    public static boolean isEmpty(String string) {
        return StringUtils.isEmpty(string);
    }

    public static String trim(String string) {
        if (isNull(string)) {
            return null;
        }
        return StringUtils.removeAll(string, "^[ 　]|[ 　]$");
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

    public static void cleanDirectory(Path path) {
        File targetDirectory = path.toFile();
        if (targetDirectory != null && targetDirectory.exists() && !targetDirectory.isDirectory()) {
            throw new IllegalArgumentException();
        }
        try {
            if (targetDirectory.exists()) {
                FileUtils.cleanDirectory(targetDirectory);
            } else {
                FileUtils.forceMkdir(targetDirectory);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static LocalDateTime parseLocalDateTime(Long epochMilli) {
        if (epochMilli == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.of("Asia/Tokyo"));
    }

    public static List<ProductImage> getAllProductImageList(final ProductDto product) {
        List<ProductImage> normalProductImageList = convert(product, product.getFileZoomList(), product.getFileList(), 0);
        List<ProductImage> adminProductImageList = convert(product, product.getFileZoomListAdmin(), product.getFileListAdmin(), 100);
        List<ProductImage> allProductImageList = new ArrayList<>();
        allProductImageList.addAll(normalProductImageList);
        allProductImageList.addAll(adminProductImageList);
        return allProductImageList;
    }
    private static List<ProductImage> convert(final ProductDto product, final List<String> fileZoomList, final List<String> fileList, final Integer displayOrderPrefix) {
        Objects.requireNonNull(product);
        if (fileZoomList == null || fileZoomList.isEmpty() || fileList == null || fileList.isEmpty() || fileZoomList.size() != fileList.size()) {
            return Collections.emptyList();
        }
        Map<String, String> fileZoomMap = new HashMap<>();
        Map<String, String> fileMap = new HashMap<>();
        fileZoomList.stream().filter(Objects::nonNull).forEach(imageUrl -> {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("?"));
            fileZoomMap.put(fileName, imageUrl);
        });

        fileList.stream().filter(Objects::nonNull).forEach(imageUrl -> {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("?"));
            fileMap.put(fileName, imageUrl);
        });
        return fileMap.keySet().stream()
                .map(fileName -> {
                    String imageUrl = fileZoomMap.get(fileName);
                    ProductImage productImage = new ProductImage();
                    productImage.setGenreCd(product.getGenreCd());
                    productImage.setUketsukeBng(product.getUketsukeBng());
                    productImage.setImageUrl(imageUrl);
                    productImage.setThumbnailImageUrl(fileMap.get(fileName));
                    productImage.setDisplayOrder(displayOrderPrefix + Integer.valueOf(YongaUtil.getMatchedGroup(imageUrl, "(\\d\\d)L?\\.jpg")));
                    return productImage;
                })
                .filter(productImage -> YongaUtil.isNotNull(productImage) && YongaUtil.isNotNull(productImage.getImageUrl()) && YongaUtil.isNotNull(productImage.getThumbnailImageUrl()))
                .collect(Collectors.toList());
    }

    public static boolean isNullOrEmpty(Object[] objectArray) {
        return objectArray == null || objectArray.length == 0;
    }
    public static boolean isNullOrEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public static String substring(String message) {
        if (message == null) {
            return null;
        }
        return StringUtils.substring(message, 100);
    }
}
