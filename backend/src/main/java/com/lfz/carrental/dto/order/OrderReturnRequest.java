package com.lfz.carrental.dto.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderReturnRequest {
    @NotNull(message = "额外费用不能为空")
    @DecimalMin(value = "0.00", message = "额外费用不能小于0")
    private BigDecimal extraAmount;
}

