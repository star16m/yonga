package com.yonga.auc.data.category.detail;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class MakerKey implements Serializable {
    @Column(name = "category_no")
    private Integer categoryNo;
    @Column(name = "maker_cd")
    private Integer makerCd;
}
