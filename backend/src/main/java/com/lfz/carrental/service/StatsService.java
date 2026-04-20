package com.lfz.carrental.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lfz.carrental.common.ApiException;
import com.lfz.carrental.entity.CarInfo;
import com.lfz.carrental.entity.RentalOrder;
import com.lfz.carrental.mapper.CarInfoMapper;
import com.lfz.carrental.mapper.RentalOrderMapper;
import com.lfz.carrental.vo.stats.CarRevenueRankVO;
import com.lfz.carrental.vo.stats.RevenueTrendPointVO;
import com.lfz.carrental.vo.stats.StatsOverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final RentalOrderMapper orderMapper;
    private final CarInfoMapper carInfoMapper;

    public StatsOverviewVO overview(LocalDate startDate, LocalDate endDate) {
        DateRange range = resolveRange(startDate, endDate);
        List<RentalOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getDeleted, 0)
                .ge(RentalOrder::getCreatedAt, range.start())
                .le(RentalOrder::getCreatedAt, range.end()));

        long totalOrders = orders.size();
        long finishedOrders = 0L;
        long canceledOrders = 0L;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExtraAmount = BigDecimal.ZERO;

        for (RentalOrder order : orders) {
            if (order.getOrderStatus() != null && order.getOrderStatus() == OrderService.ORDER_FINISHED) {
                finishedOrders++;
                totalRevenue = totalRevenue.add(nullToZero(order.getTotalAmount()));
                totalExtraAmount = totalExtraAmount.add(nullToZero(order.getExtraAmount()));
            } else if (order.getOrderStatus() != null && order.getOrderStatus() == OrderService.ORDER_CANCELED) {
                canceledOrders++;
            }
        }

        StatsOverviewVO vo = new StatsOverviewVO();
        vo.setTotalOrders(totalOrders);
        vo.setFinishedOrders(finishedOrders);
        vo.setCanceledOrders(canceledOrders);
        vo.setTotalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP));
        vo.setTotalExtraAmount(totalExtraAmount.setScale(2, RoundingMode.HALF_UP));
        if (totalOrders == 0) {
            vo.setFinishRate(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        } else {
            BigDecimal rate = BigDecimal.valueOf(finishedOrders)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
            vo.setFinishRate(rate);
        }
        return vo;
    }

    public List<RevenueTrendPointVO> revenueTrend(LocalDate startDate, LocalDate endDate) {
        DateRange range = resolveRange(startDate, endDate);
        List<RentalOrder> finishedOrders = orderMapper.selectList(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getDeleted, 0)
                .eq(RentalOrder::getOrderStatus, OrderService.ORDER_FINISHED)
                .ge(RentalOrder::getCreatedAt, range.start())
                .le(RentalOrder::getCreatedAt, range.end()));

        Map<LocalDate, BigDecimal> dayRevenueMap = new HashMap<>();
        LocalDate day = range.start().toLocalDate();
        while (!day.isAfter(range.end().toLocalDate())) {
            dayRevenueMap.put(day, BigDecimal.ZERO);
            day = day.plusDays(1);
        }

        for (RentalOrder order : finishedOrders) {
            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            dayRevenueMap.compute(orderDate, (k, v) -> nullToZero(v).add(nullToZero(order.getTotalAmount())));
        }

        List<RevenueTrendPointVO> result = new ArrayList<>();
        dayRevenueMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> result.add(new RevenueTrendPointVO(
                        entry.getKey().toString(),
                        entry.getValue().setScale(2, RoundingMode.HALF_UP)
                )));
        return result;
    }

    public List<CarRevenueRankVO> carRank(LocalDate startDate, LocalDate endDate, Integer limit) {
        int finalLimit = (limit == null || limit <= 0) ? 10 : limit;
        DateRange range = resolveRange(startDate, endDate);
        List<RentalOrder> finishedOrders = orderMapper.selectList(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getDeleted, 0)
                .eq(RentalOrder::getOrderStatus, OrderService.ORDER_FINISHED)
                .ge(RentalOrder::getCreatedAt, range.start())
                .le(RentalOrder::getCreatedAt, range.end()));

        Map<Long, BigDecimal> carRevenueMap = new HashMap<>();
        Map<Long, Long> carOrderCountMap = new HashMap<>();

        for (RentalOrder order : finishedOrders) {
            Long carId = order.getCarId();
            carRevenueMap.compute(carId, (k, v) -> nullToZero(v).add(nullToZero(order.getTotalAmount())));
            carOrderCountMap.compute(carId, (k, v) -> v == null ? 1L : v + 1L);
        }

        if (carRevenueMap.isEmpty()) {
            return List.of();
        }

        List<CarInfo> cars = carInfoMapper.selectBatchIds(carRevenueMap.keySet());
        Map<Long, CarInfo> carMap = new HashMap<>();
        for (CarInfo car : cars) {
            carMap.put(car.getId(), car);
        }

        List<CarRevenueRankVO> rankList = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : carRevenueMap.entrySet()) {
            CarInfo car = carMap.get(entry.getKey());
            if (car == null || car.getDeleted() != 0) {
                continue;
            }
            CarRevenueRankVO vo = new CarRevenueRankVO();
            vo.setCarId(car.getId());
            vo.setBrand(car.getBrand());
            vo.setModel(car.getModel());
            vo.setPlateNo(car.getPlateNo());
            vo.setRevenue(entry.getValue().setScale(2, RoundingMode.HALF_UP));
            vo.setOrderCount(carOrderCountMap.getOrDefault(car.getId(), 0L));
            rankList.add(vo);
        }

        rankList.sort(Comparator.comparing(CarRevenueRankVO::getRevenue).reversed());
        if (rankList.size() > finalLimit) {
            return rankList.subList(0, finalLimit);
        }
        return rankList;
    }

    private DateRange resolveRange(LocalDate startDate, LocalDate endDate) {
        LocalDate realStartDate = startDate == null ? LocalDate.now().minusDays(29) : startDate;
        LocalDate realEndDate = endDate == null ? LocalDate.now() : endDate;
        if (realStartDate.isAfter(realEndDate)) {
            throw new ApiException("开始日期不能晚于结束日期");
        }
        return new DateRange(realStartDate.atStartOfDay(), LocalDateTime.of(realEndDate, LocalTime.MAX));
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {
    }
}

