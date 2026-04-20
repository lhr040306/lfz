package com.lfz.carrental.controller.admin;

import com.lfz.carrental.common.ApiResponse;
import com.lfz.carrental.service.StatsService;
import com.lfz.carrental.vo.stats.CarRevenueRankVO;
import com.lfz.carrental.vo.stats.RevenueTrendPointVO;
import com.lfz.carrental.vo.stats.StatsOverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    public ApiResponse<StatsOverviewVO> overview(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return ApiResponse.success(statsService.overview(startDate, endDate));
    }

    @GetMapping("/revenue-trend")
    public ApiResponse<List<RevenueTrendPointVO>> revenueTrend(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return ApiResponse.success(statsService.revenueTrend(startDate, endDate));
    }

    @GetMapping("/car-rank")
    public ApiResponse<List<CarRevenueRankVO>> carRank(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.success(statsService.carRank(startDate, endDate, limit));
    }
}

