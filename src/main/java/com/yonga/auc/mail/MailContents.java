package com.yonga.auc.mail;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MailContents {
    private String title;
    private String subTitle;
    private List<String> contentsPrefix;
    private List<String> contentsPostfix;
    private List<String> contents;
}
