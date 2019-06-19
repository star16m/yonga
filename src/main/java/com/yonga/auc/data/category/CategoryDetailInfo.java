package com.yonga.auc.data.category;

import com.yonga.auc.data.category.detail.Brand;
import com.yonga.auc.data.category.detail.Keijo;
import com.yonga.auc.data.category.detail.Maker;
import lombok.Data;

import java.util.List;

@Data
public class CategoryDetailInfo {
    List<Maker.MakerResponse> makerListInfo;
    List<Brand.BrandResponse> brandTypeListInfo;
    List<Keijo.KeijoResponse> keijoListInfo;
}
