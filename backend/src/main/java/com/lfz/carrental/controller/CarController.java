package com.lfz.carrental.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lfz.carrental.common.ApiResponse;
import com.lfz.carrental.entity.CarInfo;
import com.lfz.carrental.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping
    public ApiResponse<IPage<CarInfo>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return ApiResponse.success(carService.listCars(page, size, brand, minPrice, maxPrice));
    }

    @GetMapping("/{id}")
    public ApiResponse<CarInfo> detail(@PathVariable Long id) {
        return ApiResponse.success(carService.getPublicCarDetail(id));
    }
}

