package com.yonga.auc.data.category.detail;

import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.common.CommonBaseData;
import com.yonga.auc.data.common.CommonBaseDataWithoutKey;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Objects;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "keijo")
public class Keijo extends CommonBaseDataWithoutKey {
    @EmbeddedId
    private KeijoKey keijoKey;
    @Column(name = "name")
    private String name;
    @Column(name = "name_en")
    private String nameEn;
    @Column(name = "name_kr")
    private String nameKr;
    @Column(name = "orders")
    private Integer order;
    public Integer getKeijoCd() {
        return this.keijoKey.getKeijoCd();
    }
    public Integer getCategoryNo() {
        return this.keijoKey.getCategoryNo();
    }
    @Data
    public static class KeijoResponse {
        private Integer keijoCd;
        private String keijo;
        private String keijoEn;
        private Integer count;
    }

    public static Keijo valueOf(KeijoResponse keijoResponse, Category category) {
        Objects.requireNonNull(keijoResponse);
        Keijo keijo = new Keijo();
        KeijoKey key = new KeijoKey();
        key.setKeijoCd(keijoResponse.getKeijoCd());
        key.setCategoryNo(category.getId());
        keijo.setKeijoKey(key);
        keijo.setName(YongaUtil.trim(keijoResponse.getKeijo()));
        keijo.setNameEn(YongaUtil.trim(keijoResponse.getKeijoEn()));
        keijo.setNameKr(YongaUtil.trim(keijoResponse.getKeijoEn()));
        return keijo;
    }
}
