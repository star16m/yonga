package com.yonga.auc.data.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageResponse implements Serializable {
    private Boolean first;
    private Boolean last;
    private Integer number;
    private Integer numberOfElements;
    private Integer size;
    private Object sort;
    private Integer totalElements;
    private Integer totalPages;
}
