package com.yonga.auc.data.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchOption {
    List<String> selectsProductType;
    List<Integer> selectsMaker;
    List<Integer> selectsKeijo;
    List<Integer> selectsBrand;
    Boolean viewProductImage;
}
