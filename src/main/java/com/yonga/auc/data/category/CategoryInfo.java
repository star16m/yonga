package com.yonga.auc.data.category;

import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryInfo implements Serializable {

    private Integer genreCd;
    private String genre;
    private String genreEn;

}
