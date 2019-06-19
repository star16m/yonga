package com.yonga.auc.data.category.detail;

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
@Table(name = "maker")
public class Maker extends CommonBaseDataWithoutKey {
    @Id
    @Column(name = "maker_cd")
    private Integer makerCd;
    @Column(name = "name")
    private String name;
    @Column(name = "name_en")
    private String nameEn;
    @Column(name = "name_kr")
    private String nameKr;
    @Column(name = "category_no")
    private Integer categoryNo;

    @Data
    public static class MakerResponse {
        private Integer makerCd;
        private String maker;
        private String makerEn;
        private Integer count;
    }

    public static Maker valueOf(MakerResponse makerResponse, Category category) {
        Objects.nonNull(makerResponse);
        Maker maker = new Maker();
        maker.setMakerCd(makerResponse.getMakerCd());
        maker.setName(makerResponse.getMaker());
        maker.setNameEn(makerResponse.getMakerEn());
        maker.setNameKr(makerResponse.getMakerEn());
        maker.setCategoryNo(category.getId());
        return maker;
    }
}
