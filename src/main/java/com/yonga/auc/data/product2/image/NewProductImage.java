package com.yonga.auc.data.product2.image;

import com.yonga.auc.data.common.CommonBaseDataWithoutKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "product_image_new")
@Data
@NoArgsConstructor
@ToString(exclude="product_new")
public class NewProductImage extends CommonBaseDataWithoutKey {
    @Id
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "product_uketsuke_bng")
    private String uketsukeBng;
    @Column(name = "thumbnail_image_url")
    private String thumbnailImageUrl;
    @Column(name = "display_order")
    private Integer displayOrder;
    @Column(name = "genre_cd")
    private Integer genreCd;
}
