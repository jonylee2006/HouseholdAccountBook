package com.lazyledger.importer.controller;

import com.lazyledger.common.model.ApiResponse;
import com.lazyledger.importer.dto.ImportJobResponse;
import com.lazyledger.importer.dto.StatementImportRequest;
import com.lazyledger.importer.dto.AuthorizationImportRequest;
import com.lazyledger.importer.service.StatementImportService;
import com.lazyledger.storage.StorageService;
import com.lazyledger.storage.UploadSignatureResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/import")
public class ImportBridgeController {

    private final StorageService storageService;
    private final StatementImportService statementImportService;

    public ImportBridgeController(StorageService storageService,
                                  StatementImportService statementImportService) {
        this.storageService = storageService;
        this.statementImportService = statementImportService;
    }

    @PostMapping("/upload-url")
    public ApiResponse<UploadSignatureResponse> uploadUrl(@RequestBody UploadUrlRequest request) {
        return ApiResponse.ok(storageService.createUploadSignature(request.directory(), request.contentType()));
    }

    @PostMapping("/parse-csv")
    public ApiResponse<ImportJobResponse> parseCsv(@Valid @RequestBody StatementImportRequest request) {
        return ApiResponse.ok(statementImportService.importStatement(request));
    }

    @PostMapping("/parse-authorization")
    public ApiResponse<ImportJobResponse> parseAuthorization(@Valid @RequestBody AuthorizationImportRequest request) {
        return ApiResponse.ok(statementImportService.importByAuthorization(request));
    }

    public record UploadUrlRequest(String directory, String contentType) {
    }
}
