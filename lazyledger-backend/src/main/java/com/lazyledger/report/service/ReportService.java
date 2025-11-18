package com.lazyledger.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lazyledger.ledger.repository.TransactionRepository;
import com.lazyledger.report.domain.ReportSnapshot;
import com.lazyledger.report.repository.ReportSnapshotRepository;
import com.lazyledger.security.CurrentUserService;
import com.lazyledger.security.UserPrincipal;
import com.lazyledger.ledger.service.LedgerAccessService;
import com.lazyledger.transaction.dto.CategorySummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final ReportSnapshotRepository reportSnapshotRepository;
    private final LedgerAccessService ledgerAccessService;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    public ReportService(TransactionRepository transactionRepository,
                         ReportSnapshotRepository reportSnapshotRepository,
                         LedgerAccessService ledgerAccessService,
                         CurrentUserService currentUserService,
                         ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.reportSnapshotRepository = reportSnapshotRepository;
        this.ledgerAccessService = ledgerAccessService;
        this.currentUserService = currentUserService;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> monthly(Long ledgerId, int year, int month) {
        UserPrincipal user = currentUserService.currentUser();
        ledgerAccessService.ensureMember(ledgerId, user.userId());
        YearMonth ym = YearMonth.of(year, month);
        String period = ym.toString();
        return reportSnapshotRepository.findByLedgerIdAndPeriod(ledgerId, period)
                .map(snapshot -> readPayload(snapshot.getPayload()))
                .orElseGet(() -> generateReport(ledgerId, ym));
    }

    private Map<String, Object> generateReport(Long ledgerId, YearMonth ym) {
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay().minusNanos(1);
        BigDecimal totalExpense = transactionRepository.sumExpenseBetween(ledgerId, start, end);
        List<CategorySummary> topCategories = transactionRepository.topCategories(ledgerId, start, end);
        Map<String, Object> payload = new HashMap<>();
        payload.put("period", ym.toString());
        payload.put("totalExpense", totalExpense);
        payload.put("topCategories", topCategories);
        ReportSnapshot snapshot = new ReportSnapshot();
        snapshot.setLedgerId(ledgerId);
        snapshot.setPeriod(ym.toString());
        snapshot.setPayload(writePayload(payload));
        reportSnapshotRepository.save(snapshot);
        return payload;
    }

    private Map<String, Object> readPayload(String payload) {
        try {
            return objectMapper.readValue(payload, Map.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("无法读取报表缓存", e);
        }
    }

    private String writePayload(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("无法写入报表缓存", e);
        }
    }
}
