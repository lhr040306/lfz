package com.lfz.carrental.vo.customer;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private Integer status;
    private LocalDateTime createdAt;
}

