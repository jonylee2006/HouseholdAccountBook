package com.lazyledger.storage.controller;

import com.lazyledger.common.model.ApiResponse;
import com.lazyledger.storage.StorageService;
import com.lazyledger.storage.UploadSignatureResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/storage")
public class UploadSignatureController {

    private final StorageService storageService;

    public UploadSignatureController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/signatures")
    public ApiResponse<UploadSignatureResponse> create(@Valid @RequestBody UploadSignatureRequest request) {
        UploadSignatureResponse signature = storageService.createUploadSignature(request.directory(), request.contentType());
        return ApiResponse.ok(signature);
    }

    public record UploadSignatureRequest(String directory,
                                         String contentType) {
    }
}
