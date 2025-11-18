package com.lazyledger.storage.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import com.lazyledger.storage.StorageService;
import com.lazyledger.storage.impl.AliyunOssStorageService;
import com.lazyledger.storage.impl.TencentCosStorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfiguration {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "lazyledger.storage.type", havingValue = "OSS", matchIfMissing = true)
    public StorageService ossStorageService(StorageProperties properties) {
        StorageProperties.Oss oss = properties.getOss();
        OSS client = new OSSClientBuilder().build(oss.getEndpoint(), oss.getAccessKeyId(), oss.getAccessKeySecret());
        return new AliyunOssStorageService(client, properties);
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "lazyledger.storage.type", havingValue = "COS")
    public StorageService cosStorageService(StorageProperties properties) {
        StorageProperties.Cos cos = properties.getCos();
        COSCredentials cred = new BasicCOSCredentials(cos.getSecretId(), cos.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cos.getRegion()));
        clientConfig.setHttpProtocol("https".equalsIgnoreCase(cos.getScheme()) ? com.qcloud.cos.http.HttpProtocol.https : com.qcloud.cos.http.HttpProtocol.http);
        COSClient cosClient = new COSClient(cred, clientConfig);
        return new TencentCosStorageService(cosClient, properties);
    }
}
