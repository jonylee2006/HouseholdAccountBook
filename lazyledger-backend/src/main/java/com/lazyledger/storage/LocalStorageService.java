package com.lazyledger.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class LocalStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    private final Path basePath;

    public LocalStorageService(@Value("${lazyledger.storage.local-base-path}") String basePath) {
        this.basePath = Paths.get(basePath);
        try {
            Files.createDirectories(this.basePath);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建本地存储目录", e);
        }
    }

    @Override
    public String store(MultipartFile file, String objectKey) {
        String key = objectKey != null ? objectKey : UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path target = basePath.resolve(key);
        try {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            } else {
                Files.createDirectories(basePath);
            }
            file.transferTo(target);
        } catch (IOException e) {
            throw new IllegalStateException("保存文件失败", e);
        }
        log.info("Stored file {} to {}", file.getOriginalFilename(), target);
        return key;
    }

    @Override
    public InputStream openStream(String objectKey) {
        Path target = basePath.resolve(objectKey);
        try {
            return Files.newInputStream(target);
        } catch (IOException e) {
            throw new IllegalStateException("读取存储对象失败: " + objectKey, e);
        }
    }
}
