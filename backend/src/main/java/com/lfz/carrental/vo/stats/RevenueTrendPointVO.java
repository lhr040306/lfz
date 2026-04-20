package com.lfz.carrental.vo.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RevenueTrendPointVO {
    private String date;
    private BigDecimal revenue;
}

