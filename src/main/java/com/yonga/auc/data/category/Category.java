package com.yonga.auc.data.category;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.yonga.auc.data.common.CommonBaseData;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "category")
public class Category extends CommonBaseData {
	private static final long serialVersionUID = -6286748887841426805L;

	@Column(name = "korean")
	private String korean;

	@Column(name = "japanese")
	private String japanese;

	@Column(name = "status")
	private String status;
	
	@Column(name = "total_product_num")
	private Integer totalProductNum;
	
	@Column(name = "ext_product_num")
	private Integer extProductNum;
	
	@Column(name = "ext_product_detail_num")
	private Integer extProductDetailNum;
}
