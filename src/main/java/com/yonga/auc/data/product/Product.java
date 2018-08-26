package com.yonga.auc.data.product;

import com.yonga.auc.data.category.Category;
import com.yonga.auc.data.common.CommonBaseData;
import com.yonga.auc.data.product.image.ProductImage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
public class Product extends CommonBaseData {
	private static final long serialVersionUID = -6139945733305473828L;

	@OneToOne
	@JoinColumn(name = "category_no")
	private Category category;
	@Column(name = "product_no")
	private Integer productNo;
	// 접수번호
	@Column(name = "desc_uketsuke_no")
	private String uketsukeNo;
	// 형상
	@Column(name = "desc_keijo")
	private String keijo;
	// 개최회수
	@Column(name = "desc_open_count")
	private String openCount;
	// 개최일
	@Column(name = "desc_open_date")
	private String openDate;
	// 세리순(?)
	@Column(name = "desc_seri_bng")
	private String seriBng;
	// 메이커
	@Column(name = "desc_maker")
	private String maker;
	// 타입
	@Column(name = "desc_type")
	private String type;
	// 상품명
	@Column(name = "desc_item_name")
	private String itemName;
	// 평가
	@Column(name = "desc_rating")
	private String rating;
	// 외장
	@Column(name = "desc_outer")
	private String outer;
	// 내장
	@Column(name = "desc_interior")
	private String interior;
	// 스타트(금액)
	@Column(name = "desc_start")
	private String start;
	// 결과(세리?)
	@Column(name = "desc_result")
	private String result;
	// 모델번호
	@Column(name = "desc_model_no")
	private String modelNo;
	// 참고가격
	@Column(name = "desc_reference_price")
	private String referencePrice;
	// 세일스 포인트
	@Column(name = "desc_sales_point")
	private String salesPoint;
	// 세일스 포인트2
	@Column(name = "desc_sales_point2")
	private String salesPoint2;
	// 부속품
	@Column(name = "desc_accessories")
	private String accessories;
	// 특기사항
	@Column(name = "desc_note")
	private String note;
	// 개정
	@Column(name = "desc_correction")
	private String correction;
	// 이미지 썸네일
	// @OneToMany(cascade = CascadeType.ALL, mappedBy = "product", fetch = FetchType.EAGER, orphanRemoval = true)
	@OneToMany(mappedBy = "product")
	private List<ProductImage> imageList = new ArrayList<>();
}
