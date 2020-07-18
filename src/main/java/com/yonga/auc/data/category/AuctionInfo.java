package com.yonga.auc.data.category;

import com.yonga.auc.common.YongaUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuctionInfo implements Serializable {
    private Integer channelA;
    private Integer channelB;
    private Integer channelC;
    private Integer channelD;
    private Integer channelE;
    private Integer channelF;
    private Integer channelG;
    // 옥션 개최 횟수
    private Integer kaisaiKaisu;
    // 옥션 개최 일자
    private LocalDateTime kaisaiYmd;
    // 옥션 최저 가격 정보
    private String lowPrice;
    private Integer lowPriceCount;
    private String lowPriceJokyo;
    private String lowPriceJokyoEn;
    private String lowPriceKaisaiFrom;
    private String lowPriceKaisaiTo;

    // 옥션 정보
    // 세리 상태
    private String seriJokyo;
    private String seriJokyoEn;
    // 출품수
    private Integer shuppinCount;

    // 추출 정보
    private LocalDateTime extractDate;

    // 총 제품 개수
    private Integer totalProductNum;
    public Integer getTotalProductNum() {
        if (YongaUtil.isNull(this.totalProductNum)) {
            return shuppinCount;
        }
        return totalProductNum;
    }
    public String getKaisaiYmdFormatted() {
        return YongaUtil.getString(kaisaiYmd, YongaUtil.COMMON_DATE_FORMATTER);
    }
    public String getExtractDateString() {
        return YongaUtil.getString(extractDate);
    }
}
