package com.lazyledger.storage.impl;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObject;
import com.lazyledger.storage.StorageService;
import com.lazyledger.storage.UploadSignatureResponse;
import com.lazyledger.storage.config.StorageProperties;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class AliyunOssStorageService implements StorageService {

    private final OSS ossClient;
    private final StorageProperties properties;

    public AliyunOssStorageService(OSS ossClient, StorageProperties properties) {
        this.ossClient = ossClient;
        this.properties = properties;
    }

    @Override
    public UploadSignatureResponse createUploadSignature(String directory, String contentType) {
        StorageProperties.Upload upload = properties.getUpload();
        String dir = directory == null || directory.isBlank() ? upload.getDirectory() : directory;
        String objectKey = dir + "/" + UUID.randomUUID() + ".csv";
        long expireMillis = upload.getExpireSeconds() * 1000L;
        Instant expireAt = Instant.ofEpochMilli(System.currentTimeMillis() + expireMillis);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(properties.getBucket(), objectKey, HttpMethod.PUT);
        request.setExpiration(java.util.Date.from(expireAt));
        request.setContentType(contentType == null || contentType.isBlank() ? upload.getContentType() : contentType);
        URL url = ossClient.generatePresignedUrl(request);
        return new UploadSignatureResponse(
                "OSS",
                "PUT",
                url.toString(),
                objectKey,
                expireAt,
                Map.of("Content-Type", request.getContentType())
        );
    }

    @Override
    public InputStream openStream(String objectKey) {
        OSSObject object = ossClient.getObject(properties.getBucket(), objectKey);
        return object.getObjectContent();
    }
}
