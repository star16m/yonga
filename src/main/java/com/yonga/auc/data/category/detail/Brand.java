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
@Table(name = "brand")
public class Brand extends CommonBaseDataWithoutKey {
    @Id
    @Column(name = "brand_cd")
    private Integer brandCd;
    @Column(name = "name")
    private String name;
    @Column(name = "name_en")
    private String nameEn;
    @Column(name = "name_kr")
    private String nameKr;
    @Column(name = "category_no")
    private Integer categoryNo;

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
        brand.setBrandCd(brandResponse.getBrandTypeCd());
        brand.setName(brandResponse.getBrandType());
        brand.setNameEn(brandResponse.getBrandTypeEn());
        brand.setNameKr(brandResponse.getBrandTypeEn());
        brand.setCategoryNo(category.getId());
        return brand;
    }
}
