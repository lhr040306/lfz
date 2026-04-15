package com.lfz.carrental.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rental_order")
public class RentalOrder extends BaseEntity {
    private String orderNo;
    private Long userId;
    private Long carId;
    private LocalDateTime rentStartTime;
    private LocalDateTime rentEndTime;
    private Integer rentDays;
    private BigDecimal dayRent;
    private BigDecimal deposit;
    private BigDecimal baseAmount;
    private BigDecimal extraAmount;
    private BigDecimal totalAmount;
    private Integer orderStatus;
    private String remark;
}

