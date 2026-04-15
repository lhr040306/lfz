package com.lfz.carrental.dto.car;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarStatusRequest {
    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态不合法")
    @Max(value = 3, message = "状态不合法")
    private Integer status;
}

