package com.yonga.auc.data.product2;

import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.data.product2.image.NewProductImage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ProductDto {
    // 실패할 경우의 메세지
    private String code;
    private String message;

    // 기본 정보
    private String uketsukeBng;
    private Integer genreCd;
    private Integer kaijoCd;
    private Integer kaisaiKaisu;
    private Integer seriBng;
    private Boolean seiyakuFlg;
    private Integer makerCd;
    private Integer brandTypeCd;
    private String brandType;
    private Integer keijoCd;
    // 상세 정보
    private String seizoBng;
    private String channelKbn;
    private Integer shinpinKbn;
    private Integer startKng;
    private Integer kiboKng;
    private Integer kekkaKbn;
    private String kekka;
    private Integer kekkaKng;
    private String shohin;
    private String shuppinBiko2;
    private String kata;
    private String biko;
    private String hyoka;
    private String hyokaGaiso;
    private String hyokaNaiso;
    private Integer invTorokuBng;
    private String inventoryNo;
    private String kaisaiYmd;
    private LocalDateTime kaisaiJknLocalDateTime;
    private LocalDateTime entryDateLocalDateTime;
    private LocalDateTime updateDateLocalDateTime;
    private List<String> fileList = new ArrayList<>();
    private List<String> fileZoomList = new ArrayList<>();
    private List<String> fileListAdmin = new ArrayList<>();
    private List<String> fileZoomListAdmin = new ArrayList<>();
    private List<NewProductImage> productImage;

    public void setKaisaiJkn(Long kaisaiJkn) {
        this.kaisaiJknLocalDateTime = YongaUtil.parseLocalDateTime(kaisaiJkn);
    }

    public void setEntryDate(Long entryDate) {
        this.entryDateLocalDateTime = YongaUtil.parseLocalDateTime(entryDate);
    }

    public void setUpdateDate(Long updateDate) {
        this.updateDateLocalDateTime = YongaUtil.parseLocalDateTime(updateDate);
    }
}
