package com.lfz.carrental.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderCreateRequest {
    @NotNull(message = "车辆ID不能为空")
    private Long carId;

    @NotNull(message = "租赁开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rentStartTime;

    @NotNull(message = "租赁结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rentEndTime;

    private String remark;
}

