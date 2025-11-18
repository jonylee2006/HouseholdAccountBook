package com.lazyledger.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {

    String store(MultipartFile file, String objectKey);

    InputStream openStream(String objectKey);
}
