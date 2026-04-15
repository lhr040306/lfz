package com.lfz.carrental.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lfz.carrental.common.ApiException;
import com.lfz.carrental.dto.car.CarCreateRequest;
import com.lfz.carrental.dto.car.CarUpdateRequest;
import com.lfz.carrental.entity.CarInfo;
import com.lfz.carrental.mapper.CarInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarInfoMapper carInfoMapper;

    // 用户侧车辆列表：仅返回可租车辆
    public IPage<CarInfo> listCars(Integer page, Integer size, String brand, BigDecimal minPrice, BigDecimal maxPrice) {
        LambdaQueryWrapper<CarInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarInfo::getStatus, 1);
        wrapper.eq(CarInfo::getDeleted, 0);
        wrapper.like(StringUtils.hasText(brand), CarInfo::getBrand, brand);
        wrapper.ge(minPrice != null, CarInfo::getDayRent, minPrice);
        wrapper.le(maxPrice != null, CarInfo::getDayRent, maxPrice);
        wrapper.orderByDesc(CarInfo::getCreatedAt);
        return carInfoMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // 管理员车辆列表：可按状态筛选
    public IPage<CarInfo> listAdminCars(Integer page, Integer size, String brand, Integer status) {
        LambdaQueryWrapper<CarInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarInfo::getDeleted, 0);
        wrapper.like(StringUtils.hasText(brand), CarInfo::getBrand, brand);
        wrapper.eq(status != null, CarInfo::getStatus, status);
        wrapper.orderByDesc(CarInfo::getCreatedAt);
        return carInfoMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // 用户侧车辆详情
    public CarInfo getPublicCarDetail(Long id) {
        CarInfo car = carInfoMapper.selectById(id);
        if (car == null || car.getDeleted() != 0 || car.getStatus() != 1) {
            throw new ApiException("车辆不存在或不可租");
        }
        return car;
    }

    // 管理员新增车辆
    public CarInfo create(CarCreateRequest request) {
        ensurePlateUnique(request.getPlateNo(), null);
        CarInfo car = new CarInfo();
        fillCarByRequest(car, request);
        car.setStatus(car.getStatus() == null ? 1 : car.getStatus());
        car.setMileage(car.getMileage() == null ? 0 : car.getMileage());
        carInfoMapper.insert(car);
        return car;
    }

    // 管理员编辑车辆
    public CarInfo update(Long id, CarUpdateRequest request) {
        CarInfo car = carInfoMapper.selectById(id);
        if (car == null || car.getDeleted() != 0) {
            throw new ApiException("车辆不存在");
        }
        ensurePlateUnique(request.getPlateNo(), id);
        fillCarByRequest(car, request);
        car.setStatus(car.getStatus() == null ? 1 : car.getStatus());
        car.setMileage(car.getMileage() == null ? 0 : car.getMileage());
        carInfoMapper.updateById(car);
        return car;
    }

    // 车辆状态调整（可租/维护/下架）
    public void updateStatus(Long id, Integer status) {
        CarInfo car = carInfoMapper.selectById(id);
        if (car == null || car.getDeleted() != 0) {
            throw new ApiException("车辆不存在");
        }
        car.setStatus(status);
        carInfoMapper.updateById(car);
    }

    private void ensurePlateUnique(String plateNo, Long excludeId) {
        LambdaQueryWrapper<CarInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarInfo::getPlateNo, plateNo);
        wrapper.eq(CarInfo::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(CarInfo::getId, excludeId);
        }
        Long count = carInfoMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new ApiException("车牌号已存在");
        }
    }

    private void fillCarByRequest(CarInfo car, CarCreateRequest request) {
        car.setBrand(request.getBrand());
        car.setSeries(request.getSeries());
        car.setModel(request.getModel());
        car.setPlateNo(request.getPlateNo());
        car.setSeatCount(request.getSeatCount());
        car.setGearbox(request.getGearbox());
        car.setFuelType(request.getFuelType());
        car.setDayRent(request.getDayRent());
        car.setDeposit(request.getDeposit());
        car.setStatus(request.getStatus());
        car.setMileage(request.getMileage());
        car.setCoverUrl(request.getCoverUrl());
    }

    private void fillCarByRequest(CarInfo car, CarUpdateRequest request) {
        car.setBrand(request.getBrand());
        car.setSeries(request.getSeries());
        car.setModel(request.getModel());
        car.setPlateNo(request.getPlateNo());
        car.setSeatCount(request.getSeatCount());
        car.setGearbox(request.getGearbox());
        car.setFuelType(request.getFuelType());
        car.setDayRent(request.getDayRent());
        car.setDeposit(request.getDeposit());
        car.setStatus(request.getStatus());
        car.setMileage(request.getMileage());
        car.setCoverUrl(request.getCoverUrl());
    }
}
