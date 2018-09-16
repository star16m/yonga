package com.yonga.auc.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@Table(name = "config")
public class Config {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "config_group")
    private String group;
    @Column(name = "config_key")
    private String key;
    @Column(name = "config_value")
    private String value;
}
