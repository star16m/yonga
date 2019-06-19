package com.yonga.auc.data.product;

import com.yonga.auc.data.common.PageResponse;
import lombok.Data;

import java.util.List;

@Data
public class ProductList extends PageResponse {
    private List<ProductDto> content;
}
