package com.yonga.auc.data.customer;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class CustomerEnabled {
    @NotNull
    @Size(min=4, max=30)
    private String customerId;
    @NotNull
    private Boolean enabled;
}
