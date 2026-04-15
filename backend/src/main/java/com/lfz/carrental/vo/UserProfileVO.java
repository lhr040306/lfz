package com.lfz.carrental.vo;

import lombok.Data;

@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String roleCode;
}

