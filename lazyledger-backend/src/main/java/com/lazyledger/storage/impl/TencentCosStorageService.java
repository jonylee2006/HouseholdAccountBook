package com.lazyledger.storage.impl;

import com.lazyledger.storage.StorageService;
import com.lazyledger.storage.UploadSignatureResponse;
import com.lazyledger.storage.config.StorageProperties;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;

import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class TencentCosStorageService implements StorageService {

    private final COSClient cosClient;
    private final StorageProperties properties;

    public TencentCosStorageService(COSClient cosClient, StorageProperties properties) {
        this.cosClient = cosClient;
        this.properties = properties;
    }

    @Override
    public UploadSignatureResponse createUploadSignature(String directory, String contentType) {
        StorageProperties.Upload upload = properties.getUpload();
        String dir = directory == null || directory.isBlank() ? upload.getDirectory() : directory;
        String objectKey = dir + "/" + UUID.randomUUID() + ".csv";
        long expireMillis = upload.getExpireSeconds() * 1000L;
        Instant expireAt = Instant.ofEpochMilli(System.currentTimeMillis() + expireMillis);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(properties.getBucket(), objectKey, HttpMethodName.PUT);
        request.setExpiration(java.util.Date.from(expireAt));
        request.setContentType(contentType == null || contentType.isBlank() ? upload.getContentType() : contentType);
        URL url = cosClient.generatePresignedUrl(request);
        return new UploadSignatureResponse(
                "COS",
                "PUT",
                url.toString(),
                objectKey,
                expireAt,
                Map.of("Content-Type", request.getContentType())
        );
    }

    @Override
    public InputStream openStream(String objectKey) {
        COSObject cosObject = cosClient.getObject(properties.getBucket(), objectKey);
        return cosObject.getObjectContent();
    }
}
