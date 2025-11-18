package com.lazyledger.storage;

import java.io.InputStream;

public interface StorageService {

    UploadSignatureResponse createUploadSignature(String directory, String contentType);

    InputStream openStream(String objectKey);
}
