package com.lazyledger.importer.controller;

import com.lazyledger.common.model.ApiResponse;
import com.lazyledger.importer.dto.ImportJobResponse;
import com.lazyledger.importer.dto.ImportResultResponse;
import com.lazyledger.importer.dto.ImportedTransactionDto;
import com.lazyledger.importer.dto.StatementImportRequest;
import com.lazyledger.importer.service.StatementImportService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/import")
@Validated
public class StatementImportController {

    private final StatementImportService statementImportService;

    public StatementImportController(StatementImportService statementImportService) {
        this.statementImportService = statementImportService;
    }

    @PostMapping(value = "/jobs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImportResultResponse> importStatement(@Valid @RequestPart("metadata") StatementImportRequest request,
                                                             @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(statementImportService.importStatement(request, file));
    }

    @GetMapping("/jobs/{jobId}")
    public ApiResponse<ImportJobResponse> getJob(@PathVariable Long jobId) {
        return ApiResponse.ok(statementImportService.findJob(jobId));
    }

    @GetMapping("/jobs/{jobId}/transactions")
    public ApiResponse<List<ImportedTransactionDto>> getJobTransactions(@PathVariable Long jobId) {
        return ApiResponse.ok(statementImportService.findTransactionsByJob(jobId));
    }
}
