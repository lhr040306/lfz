package com.lfz.carrental.vo.stats;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatsOverviewVO {
    private BigDecimal totalRevenue;
    private BigDecimal totalExtraAmount;
    private Long totalOrders;
    private Long finishedOrders;
    private Long canceledOrders;
    private BigDecimal finishRate;
}

