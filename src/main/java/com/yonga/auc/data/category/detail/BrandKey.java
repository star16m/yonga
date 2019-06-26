package com.yonga.auc.data.category.detail;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class BrandKey implements Serializable {
    @Column(name = "category_no")
    private Integer categoryNo;
    @Column(name = "brand_cd")
    private Integer brandCd;
}
