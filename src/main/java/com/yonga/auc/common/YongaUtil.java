package com.yonga.auc.common;

import com.yonga.auc.data.product2.ProductDto;
import com.yonga.auc.data.product2.image.NewProductImage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class YongaUtil {

    private static DateTimeFormatter COMMON_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM월 dd일 (EEEE) HH:mm", Locale.KOREAN);
    private YongaUtil() {
    }
    public static String getString(LocalDateTime localDateTime) {
        if (isNull(localDateTime)) {
            return "";
        }
        return localDateTime.format(COMMON_DATE_TIME_FORMATTER);
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
        if (!targetDirectory.isDirectory()) {
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

    public static List<NewProductImage> getAllProductImageList(final ProductDto product) {
        List<NewProductImage> normalProductImageList = convert(product, product.getFileZoomList(), product.getFileList(), 0);
        List<NewProductImage> adminProductImageList = convert(product, product.getFileZoomListAdmin(), product.getFileListAdmin(), 100);
        List<NewProductImage> allProductImageList = new ArrayList<>();
        allProductImageList.addAll(normalProductImageList);
        allProductImageList.addAll(adminProductImageList);
        return allProductImageList;
    }
    private static List<NewProductImage> convert(final ProductDto product, final List<String> fileZoomList, final List<String> fileList, final Integer displayOrderPrefix) {
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
                    NewProductImage newProductImage = new NewProductImage();
                    newProductImage.setGenreCd(product.getGenreCd());
                    newProductImage.setUketsukeBng(product.getUketsukeBng());
                    newProductImage.setImageUrl(imageUrl);
                    newProductImage.setThumbnailImageUrl(fileMap.get(fileName));
                    newProductImage.setDisplayOrder(displayOrderPrefix + Integer.valueOf(YongaUtil.getMatchedGroup(imageUrl, "(\\d\\d)L?\\.jpg")));
                    return newProductImage;
                })
                .filter(productImage -> YongaUtil.isNotNull(productImage) && YongaUtil.isNotNull(productImage.getImageUrl()) && YongaUtil.isNotNull(productImage.getThumbnailImageUrl()))
                .collect(Collectors.toList());
    }
}
