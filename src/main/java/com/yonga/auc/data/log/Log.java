package com.yonga.auc.data.log;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedDate;

import lombok.Data;

@Data
@Entity
@Table(name = "work_log")
public class Log {

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
    private Integer id;
	
	@Column(name = "message")
    private String message;

	@Column(name = "create_date", updatable = false, insertable = true)
	@CreatedDate
	@Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
}
