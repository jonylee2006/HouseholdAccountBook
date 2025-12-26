package com.lazyledger.dashboard.controller;

import com.lazyledger.common.model.ApiResponse;
import com.lazyledger.dashboard.dto.DashboardSummaryResponse;
import com.lazyledger.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> summary(@RequestParam Long ledgerId) {
        return ApiResponse.ok(dashboardService.summary(ledgerId));
    }
}
