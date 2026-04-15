package com.lfz.carrental.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lfz.carrental.common.ApiException;
import com.lfz.carrental.dto.order.OrderCreateRequest;
import com.lfz.carrental.dto.order.OrderReturnRequest;
import com.lfz.carrental.entity.CarInfo;
import com.lfz.carrental.entity.RentalOrder;
import com.lfz.carrental.mapper.CarInfoMapper;
import com.lfz.carrental.mapper.RentalOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderService {

    // 订单状态定义
    public static final int ORDER_WAIT_PAY = 10;
    public static final int ORDER_WAIT_PICKUP = 20;
    public static final int ORDER_RENTING = 30;
    public static final int ORDER_FINISHED = 50;
    public static final int ORDER_CANCELED = 90;

    private final RentalOrderMapper orderMapper;
    private final CarInfoMapper carInfoMapper;

    // 用户创建订单：校验时间冲突并计算费用
    public RentalOrder createOrder(Long userId, OrderCreateRequest request) {
        LocalDateTime start = request.getRentStartTime();
        LocalDateTime end = request.getRentEndTime();
        if (start == null || end == null || !end.isAfter(start)) {
            throw new ApiException("租赁结束时间必须晚于开始时间");
        }

        CarInfo car = carInfoMapper.selectById(request.getCarId());
        if (car == null || car.getDeleted() != 0 || car.getStatus() != 1) {
            throw new ApiException("车辆不存在或不可租");
        }

        Long conflictCount = orderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getCarId, request.getCarId())
                .eq(RentalOrder::getDeleted, 0)
                .in(RentalOrder::getOrderStatus, List.of(ORDER_WAIT_PAY, ORDER_WAIT_PICKUP, ORDER_RENTING))
                .lt(RentalOrder::getRentStartTime, end)
                .gt(RentalOrder::getRentEndTime, start));
        if (conflictCount != null && conflictCount > 0) {
            throw new ApiException("当前时间段车辆已被预订");
        }

        long minutes = Duration.between(start, end).toMinutes();
        int rentDays = (int) Math.ceil(minutes / (60.0 * 24));
        if (rentDays <= 0) {
            throw new ApiException("租赁时长必须至少1天");
        }

        BigDecimal baseAmount = car.getDayRent().multiply(BigDecimal.valueOf(rentDays));
        BigDecimal totalAmount = baseAmount.add(car.getDeposit());

        RentalOrder order = new RentalOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setCarId(car.getId());
        order.setRentStartTime(start);
        order.setRentEndTime(end);
        order.setRentDays(rentDays);
        order.setDayRent(car.getDayRent());
        order.setDeposit(car.getDeposit());
        order.setBaseAmount(baseAmount);
        order.setExtraAmount(BigDecimal.ZERO);
        order.setTotalAmount(totalAmount);
        order.setOrderStatus(ORDER_WAIT_PAY);
        order.setRemark(request.getRemark());
        orderMapper.insert(order);
        return order;
    }

    // 用户查询自己的订单
    public IPage<RentalOrder> myOrders(Long userId, Integer page, Integer size) {
        return orderMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<RentalOrder>()
                        .eq(RentalOrder::getUserId, userId)
                        .eq(RentalOrder::getDeleted, 0)
                        .orderByDesc(RentalOrder::getCreatedAt));
    }

    // 用户查看单个订单详情
    public RentalOrder getOrderByIdForUser(Long userId, Long orderId) {
        RentalOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() != 0) {
            throw new ApiException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new ApiException("无权查看该订单");
        }
        return order;
    }

    // 仅待支付订单可取消
    public void cancelOrder(Long userId, Long orderId) {
        RentalOrder order = getOrderByIdForUser(userId, orderId);
        if (order.getOrderStatus() != ORDER_WAIT_PAY) {
            throw new ApiException("当前订单状态不允许取消");
        }
        order.setOrderStatus(ORDER_CANCELED);
        orderMapper.updateById(order);
    }

    // 支付成功后进入待取车
    public void payOrder(Long userId, Long orderId) {
        RentalOrder order = getOrderByIdForUser(userId, orderId);
        if (order.getOrderStatus() != ORDER_WAIT_PAY) {
            throw new ApiException("当前订单状态不允许支付");
        }
        order.setOrderStatus(ORDER_WAIT_PICKUP);
        orderMapper.updateById(order);
    }

    // 管理员分页查询订单
    public IPage<RentalOrder> listAdminOrders(Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<RentalOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentalOrder::getDeleted, 0);
        wrapper.eq(status != null, RentalOrder::getOrderStatus, status);
        wrapper.orderByDesc(RentalOrder::getCreatedAt);
        return orderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // 确认出车：待取车 -> 租赁中
    public void confirmPickup(Long orderId) {
        RentalOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() != 0) {
            throw new ApiException("订单不存在");
        }
        if (order.getOrderStatus() != ORDER_WAIT_PICKUP) {
            throw new ApiException("当前订单状态不允许出车");
        }
        order.setOrderStatus(ORDER_RENTING);
        orderMapper.updateById(order);
    }

    // 确认还车：计算额外费用并结算
    public void confirmReturn(Long orderId, OrderReturnRequest request) {
        RentalOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() != 0) {
            throw new ApiException("订单不存在");
        }
        if (order.getOrderStatus() != ORDER_RENTING) {
            throw new ApiException("当前订单状态不允许还车结算");
        }
        BigDecimal extraAmount = request.getExtraAmount() == null ? BigDecimal.ZERO : request.getExtraAmount();
        order.setExtraAmount(extraAmount);
        order.setTotalAmount(order.getBaseAmount().add(order.getDeposit()).add(extraAmount));
        order.setOrderStatus(ORDER_FINISHED);
        orderMapper.updateById(order);
    }

    private String generateOrderNo() {
        String timePart = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "ORD" + timePart + random;
    }
}
