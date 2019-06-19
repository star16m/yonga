package com.yonga.auc.data.common;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
public class CommonBaseDataWithoutKey implements Serializable {
	private static final long serialVersionUID = 449027036578512049L;

	@Column(name = "create_date", updatable = false, insertable = true)
	@CreatedDate
	@Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

	@Column(name = "modified_date", updatable = true)
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDate;
}
