package com.yonga.auc.data.category.detail;

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
@Table(name = "brand")
public class Brand extends CommonBaseDataWithoutKey {
    @EmbeddedId
    private BrandKey brandKey;
    @Column(name = "name")
    private String name;
    @Column(name = "name_en")
    private String nameEn;
    @Column(name = "name_kr")
    private String nameKr;

    public Integer getBrandCd() {
        return this.brandKey.getBrandCd();
    }
    public Integer getCategoryNo() {
        return this.brandKey.getCategoryNo();
    }
    @Data
    public static class BrandResponse {
        private Integer brandTypeCd;
        private String brandType;
        private String brandTypeEn;
        private Integer count;
    }

    public static Brand valueOf(BrandResponse brandResponse, Category category) {
        Objects.requireNonNull(brandResponse);
        Brand brand = new Brand();
        BrandKey key = new BrandKey();
        key.setBrandCd(brandResponse.getBrandTypeCd());
        key.setCategoryNo(category.getId());
        brand.setBrandKey(key);
        brand.setName(brandResponse.getBrandType());
        brand.setNameEn(brandResponse.getBrandTypeEn());
        brand.setNameKr(brandResponse.getBrandTypeEn());
        return brand;
    }
}
