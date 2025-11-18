package com.lazyledger.importer.controller;

import com.lazyledger.common.model.ApiResponse;
import com.lazyledger.importer.dto.ImportAuthorizationRequest;
import com.lazyledger.importer.dto.ImportAuthorizationResponse;
import com.lazyledger.importer.service.ImportAuthorizationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/import/authorizations")
public class ImportAuthorizationController {

    private final ImportAuthorizationService importAuthorizationService;

    public ImportAuthorizationController(ImportAuthorizationService importAuthorizationService) {
        this.importAuthorizationService = importAuthorizationService;
    }

    @PostMapping
    public ApiResponse<ImportAuthorizationResponse> create(@Valid @RequestBody ImportAuthorizationRequest request) {
        return ApiResponse.ok(importAuthorizationService.create(request));
    }

    @GetMapping
    public ApiResponse<List<ImportAuthorizationResponse>> list(@RequestParam Long ledgerId) {
        return ApiResponse.ok(importAuthorizationService.list(ledgerId));
    }
}
