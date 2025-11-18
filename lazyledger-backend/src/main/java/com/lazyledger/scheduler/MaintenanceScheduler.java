package com.lazyledger.scheduler;

import com.lazyledger.report.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

@Component
public class MaintenanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceScheduler.class);

    private final ReportService reportService;

    public MaintenanceScheduler(ReportService reportService) {
        this.reportService = reportService;
    }

    @Scheduled(cron = "0 30 3 * * *")
    public void nightlyReportWarmup() {
        // 真实场景下应遍历所有账本，此处示例日志
        log.debug("夜间任务：可在此扫描所有账本，重新生成月度报表或预算提醒");
    }
}
