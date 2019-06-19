package com.yonga.auc.data.category.detail;

import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.common.CommonBaseData;
import com.yonga.auc.data.common.CommonBaseDataWithoutKey;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "keijo")
public class Keijo extends CommonBaseDataWithoutKey {
    @Id
    @Column(name = "keijo_cd")
    private Integer keijoCd;
    @Column(name = "name")
    private String name;
    @Column(name = "name_en")
    private String nameEn;
    @Column(name = "name_kr")
    private String nameKr;
    @Column(name = "category_no")
    private Integer categoryNo;

    @Data
    public static class KeijoResponse {
        private Integer keijoCd;
        private String keijo;
        private String keijoEn;
        private Integer count;
    }

    public static Keijo valueOf(KeijoResponse keijoResponse, Category category) {
        Objects.nonNull(keijoResponse);
        Keijo keijo = new Keijo();
        keijo.setKeijoCd(keijoResponse.getKeijoCd());
        keijo.setName(keijoResponse.getKeijo());
        keijo.setNameEn(keijoResponse.getKeijoEn());
        keijo.setNameKr(keijoResponse.getKeijoEn());
        keijo.setCategoryNo(category.getId());
        return keijo;
    }
}
