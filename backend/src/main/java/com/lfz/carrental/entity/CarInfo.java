package com.lfz.carrental.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("car_info")
public class CarInfo extends BaseEntity {
    private String brand;
    private String series;
    private String model;
    private String plateNo;
    private Integer seatCount;
    private String gearbox;
    private String fuelType;
    private BigDecimal dayRent;
    private BigDecimal deposit;
    private Integer status;
    private Integer mileage;
    private String coverUrl;
}

