package com.yonga.auc.data.product.image;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.yonga.auc.data.common.CommonBaseData;
import com.yonga.auc.data.product.Product;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "product_image")
@Data
@NoArgsConstructor
@ToString(exclude="product")
public class ProductImage extends CommonBaseData {
	private static final long serialVersionUID = -3149028832949010253L;
	public ProductImage(Integer id, String name) {
		super();
		super.setId(id);
		super.setName(name);
	}
	public ProductImage(String name) {
		super();
		super.setName(name);
	}
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    public String getLargeName() {
    	Objects.requireNonNull(this.getName());
    	String largeName = this.getName().replaceAll("TN.jpg", "L.jpg");
    	return largeName;
    }
}
