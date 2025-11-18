package com.lazyledger.report.controller;

import com.lazyledger.common.model.ApiResponse;
import com.lazyledger.report.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ApiResponse<Map<String, Object>> monthly(@RequestParam Long ledgerId,
                                                    @RequestParam int year,
                                                    @RequestParam int month) {
        return ApiResponse.ok(reportService.monthly(ledgerId, year, month));
    }
}
