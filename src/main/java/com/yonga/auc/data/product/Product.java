package com.yonga.auc.data.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.category.detail.Brand;
import com.yonga.auc.data.category.detail.Keijo;
import com.yonga.auc.data.category.detail.Maker;
import com.yonga.auc.data.common.CommonBaseDataWithoutKey;
import com.yonga.auc.data.product.image.ProductImage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@ToString
public class Product extends CommonBaseDataWithoutKey {

    public Product(ProductDto productDto) {
        this.uketsukeBng = productDto.getUketsukeBng();
        this.genreCd = productDto.getGenreCd();
        this.kaijoCd = productDto.getKaijoCd();
        this.kaisaiKaisu = productDto.getKaisaiKaisu();
        this.seriBng = productDto.getSeriBng();

        this.productType = "L".equals(productDto.getChannelKbn()) ? "LOW" : productDto.getKaijoCd() == 0 ? "MALL" : "RT";
        this.seiyakuFlg = productDto.getSeiyakuFlg();
        this.makerCd = productDto.getMakerCd();
        this.brandTypeCd = productDto.getBrandTypeCd();
        this.keijoCd = productDto.getKeijoCd();
        this.seizoBng = productDto.getSeizoBng();
        this.channelKbn = productDto.getChannelKbn();
        this.shinpinKbn = productDto.getShinpinKbn();
        this.startKng = productDto.getStartKng();
        if ("MALL".equals(this.productType)) {
            this.startKng = productDto.getKingaku();
        }
        this.kiboKng = productDto.getKiboKng();
        this.kekkaKbn = productDto.getKekkaKbn();
        this.kekka = productDto.getKekka();
        this.kekkaKng = productDto.getKekkaKng();
        this.shohin = productDto.getShohin();
        String shuppinBiko2 = "";
        if (productDto.getShuppinBiko2() != null) {
            shuppinBiko2 = productDto.getShuppinBiko2().replaceAll(" {2,}", ", ");
            if (shuppinBiko2.length() > 255) {
                shuppinBiko2 = shuppinBiko2.substring(0, 255);
            }
        }
        this.shuppinBiko2 = shuppinBiko2;
        this.kata = productDto.getKata();
        String biko = "";
        if (productDto.getBiko() != null) {
            biko = productDto.getBiko().replaceAll(" {2,}", ", ");
            if (biko.length() > 255) {
                biko = biko.substring(0, 255);
            }
        }
        this.biko = biko;
        this.hyoka = productDto.getHyoka();
        this.hyokaGaiso = productDto.getHyokaGaiso();
        this.hyokaNaiso = productDto.getHyokaNaiso();
        this.invTorokuBng = productDto.getInvTorokuBng();
        this.inventoryNo = productDto.getInventoryNo();
        this.kaisaiYmd = productDto.getKaisaiYmd();
        this.kaisaiJknLocalDateTime = productDto.getKaisaiJknLocalDateTime();
        this.entryDateLocalDateTime = productDto.getEntryDateLocalDateTime();
        this.updateDateLocalDateTime = productDto.getUpdateDateLocalDateTime();
        this.productImage = YongaUtil.getAllProductImageList(productDto);
    }
    // 기본 정보
    @Id
    @Column(name = "uketsuke_bng")
    private String uketsukeBng;
    @OneToOne
    @JoinColumn(name = "genre_cd", updatable = false, insertable = false)
    private Category category;
    @Column(name = "genre_cd")
    private Integer genreCd;
    @Column(name = "kaijo_cd")
    private Integer kaijoCd;
    @Column(name = "kaisai_kaisu")
    private Integer kaisaiKaisu;
    @Column(name = "seri_bng")
    private Integer seriBng;
    @Transient
    private Boolean seiyakuFlg;
    @Column(name = "maker_cd")
    private Integer makerCd;
    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "genre_cd", referencedColumnName="category_no", updatable = false, insertable = false),
            @JoinColumn(name = "maker_cd", referencedColumnName = "maker_cd", updatable = false, insertable = false)
    })
    @JsonIgnore
    private Maker maker;
    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "genre_cd", referencedColumnName="category_no", updatable = false, insertable = false),
            @JoinColumn(name = "brand_type_cd", referencedColumnName = "brand_cd", updatable = false, insertable = false)
    })
    @JsonIgnore
    private Brand brand;
    @Column(name = "brand_type_cd")
    private Integer brandTypeCd;
    @Transient
    private String brandType;
    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "genre_cd", referencedColumnName="category_no", updatable = false, insertable = false),
            @JoinColumn(name = "keijo_cd", referencedColumnName = "keijo_cd", updatable = false, insertable = false)
    })
    @JsonIgnore
    private Keijo keijo;
    @Column(name = "keijo_cd")
    private Integer keijoCd;
    @Column(name = "product_type")
    private String productType;
    // 상세 정보
    @Column(name = "seizo_bng")
    private String seizoBng;
    @Column(name = "channel_kbn")
    private String channelKbn;
    @Column(name = "shinpin_kbn")
    private Integer shinpinKbn;
    @Column(name = "start_kng")
    private Integer startKng;
    @Column(name = "kibo_kng")
    private Integer kiboKng;
    @Column(name = "kekka_kbn")
    private Integer kekkaKbn;
    @Column(name = "kekka")
    private String kekka;
    @Column(name = "kekka_kng")
    private Integer kekkaKng;
    @Column(name = "shohin")
    private String shohin;
    @Column(name = "shuppin_biko2")
    private String shuppinBiko2;
    @Column(name = "kata")
    private String kata;
    @Column(name = "biko")
    private String biko;
    @Column(name = "hyoka")
    private String hyoka;
    @Column(name = "hyoka_gaiso")
    private String hyokaGaiso;
    @Column(name = "hyoka_naiso")
    private String hyokaNaiso;
    @Column(name = "inv_toroku_bng")
    private Integer invTorokuBng;
    @Column(name = "inventory_no")
    private String inventoryNo;
    @Column(name = "kaisai_ymd")
    private String kaisaiYmd;
    @Column(name = "kaisai_jkn")
    private LocalDateTime kaisaiJknLocalDateTime;
    @Column(name = "entry_date")
    private LocalDateTime entryDateLocalDateTime;
    @Column(name = "update_date")
    private LocalDateTime updateDateLocalDateTime;
    @Column(name = "extract_result")
    @Enumerated(EnumType.STRING)
    private ExtractResult extractResult;
    @OneToMany
    @JoinColumn(name = "product_uketsuke_bng", updatable = false, insertable = false)
    private List<ProductImage> productImage;

    public String getKaisaiYmdFormattedString() {
        return YongaUtil.getDateString(this.kaisaiYmd);
    }
    public void setKaisaiJkn(Long kaisaiJkn) {
        this.kaisaiJknLocalDateTime = YongaUtil.parseLocalDateTime(kaisaiJkn);
    }

    public void setEntryDate(Long entryDate) {
        this.entryDateLocalDateTime = YongaUtil.parseLocalDateTime(entryDate);
    }

    public void setUpdateDate(Long updateDate) {
        this.updateDateLocalDateTime = YongaUtil.parseLocalDateTime(updateDate);
    }
    public String getDetailUrl() {
        if (genreCd == 1 || genreCd == 2 || genreCd == 3) {
            // 백/시계/귀금속
            String genreUrlCd = genreCd == 1 ? "bag" : genreCd == 2 ? "watch" : genreCd == 3 ? "noble" : null;
            if (this.kaijoCd == 0) {
                return String.format("https://u.brand-auc.com/api/v1/shops/buyItems/%s/%s/false", genreUrlCd, this.invTorokuBng);
            }
            return String.format("https://u.brand-auc.com/api/v1/auction/auctionItems/%s/%s/%s/%s/B02-01", genreUrlCd, this.kaijoCd, this.kaisaiKaisu, this.uketsukeBng);
        } else {
            if (this.kaijoCd == 0) {
                return String.format("https://u.brand-auc.com/api/v1/shops/buyItems/other/%s/false", this.invTorokuBng);
            }
            return String.format("https://u.brand-auc.com/api/v1/auction/auctionItems/other/1/%s/%s/B02-01/%s", this.kaisaiKaisu, this.uketsukeBng, this.genreCd);
        }
    }
}
