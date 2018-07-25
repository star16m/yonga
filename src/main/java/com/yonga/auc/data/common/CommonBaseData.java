package com.yonga.auc.data.common;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

@Data
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
public class CommonBaseData implements Serializable {
	private static final long serialVersionUID = 449027036578512049L;

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
    private Integer id;
	
	@Column(name = "name")
    private String name;

	@Column(name = "create_date", updatable = false, insertable = true)
	@CreatedDate
	@Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

	@Column(name = "modified_date", updatable = true)
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDate;
}
