package com.yonga.auc.data.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Customer {
    @Id @NotNull @Size(min=4, max=30) @Pattern(regexp="^[\\w]+$")
    private String userId;
    @NotNull @Size(min=5, max=255)
    private String password;
    @JsonIgnore
    private String privilege;
    @NotNull @Size(min=2, max=30)
    private String name;
    @NotNull @Size(min=5, max=30) @Pattern(regexp="\\d{10,11}|\\d{2,3}-\\d{3,4}-\\d{4}|")
    private String tel;
    @NotNull @Size(min=3, max=255) @Pattern(regexp="^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$")
    private String email;
    @NotNull @Size(max=255)
    private String description;
    @JsonIgnore
    private Boolean enabled = false;
    @JsonIgnore
    private Boolean display = true;
    private LocalDateTime lastLogin;
}
