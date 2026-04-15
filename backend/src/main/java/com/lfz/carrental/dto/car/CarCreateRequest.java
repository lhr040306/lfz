package com.lfz.carrental.dto.car;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarCreateRequest {
    @NotBlank(message = "品牌不能为空")
    private String brand;

    private String series;

    @NotBlank(message = "车型不能为空")
    private String model;

    @NotBlank(message = "车牌号不能为空")
    private String plateNo;

    @NotNull(message = "座位数不能为空")
    @Min(value = 2, message = "座位数至少为2")
    private Integer seatCount;

    private String gearbox;
    private String fuelType;

    @NotNull(message = "日租金不能为空")
    @DecimalMin(value = "0.01", message = "日租金必须大于0")
    private BigDecimal dayRent;

    @NotNull(message = "押金不能为空")
    @DecimalMin(value = "0.00", message = "押金不能小于0")
    private BigDecimal deposit;

    private Integer status;
    private Integer mileage;
    private String coverUrl;
}

