package com.lazyledger.importer.remote;

import com.lazyledger.common.enums.ImportSourceType;
import com.lazyledger.ledger.domain.ImportAuthorization;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class MockRemoteStatementClient implements RemoteStatementClient {

    @Override
    public InputStream fetch(ImportAuthorization authorization) {
        try {
            String sample = authorization.getSourceType() == ImportSourceType.WECHAT
                    ? "samples/wechat.csv"
                    : "samples/alipay.csv";
            return new ClassPathResource(sample).getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException("远程拉取账单失败", e);
        }
    }
}
