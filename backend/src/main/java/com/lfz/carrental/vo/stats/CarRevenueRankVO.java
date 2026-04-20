package com.lfz.carrental.vo.stats;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarRevenueRankVO {
    private Long carId;
    private String brand;
    private String model;
    private String plateNo;
    private BigDecimal revenue;
    private Long orderCount;
}

