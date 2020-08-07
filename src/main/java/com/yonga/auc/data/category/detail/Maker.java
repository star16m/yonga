package com.yonga.auc.data.category.detail;

import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.common.CommonBaseDataWithoutKey;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "maker")
public class Maker extends CommonBaseDataWithoutKey {

    @EmbeddedId
    private MakerKey makerKey;
    @Column(name = "name")
    private String name;
    @Column(name = "name_en")
    private String nameEn;
    @Column(name = "name_kr")
    private String nameKr;
    @Column(name = "orders")
    private Integer order;

    public Integer getMakerCd() {
        return this.makerKey.getMakerCd();
    }
    public Integer getCategoryNo() {
        return this.makerKey.getCategoryNo();
    }
    @Data
    public static class MakerResponse {
        private Integer makerCd;
        private String maker;
        private String makerEn;
        private Integer count;
    }

    public static Maker valueOf(MakerResponse makerResponse, Category category) {
        Objects.requireNonNull(makerResponse);
        Maker maker = new Maker();
        MakerKey key = new MakerKey();
        key.setCategoryNo(category.getId());
        key.setMakerCd(makerResponse.getMakerCd());
        maker.setMakerKey(key);
        maker.setName(YongaUtil.trim(makerResponse.getMaker()));
        maker.setNameEn(YongaUtil.trim(makerResponse.getMakerEn()));
        maker.setNameKr(YongaUtil.trim(makerResponse.getMakerEn()));
        return maker;
    }
}
