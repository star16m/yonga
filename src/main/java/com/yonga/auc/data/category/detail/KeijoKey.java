package com.yonga.auc.data.category.detail;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class KeijoKey implements Serializable {
    @Column(name = "category_no")
    private Integer categoryNo;
    @Column(name = "keijo_cd")
    private Integer keijoCd;
}
