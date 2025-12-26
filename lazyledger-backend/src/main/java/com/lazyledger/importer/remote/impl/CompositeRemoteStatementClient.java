package com.lazyledger.importer.remote.impl;

import com.lazyledger.common.enums.ImportSourceType;
import com.lazyledger.common.exception.BusinessException;
import com.lazyledger.importer.remote.RemoteStatementClient;
import com.lazyledger.ledger.domain.ImportAuthorization;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

public class CompositeRemoteStatementClient implements RemoteStatementClient {

    private final Map<ImportSourceType, RemoteStatementClient> delegates = new EnumMap<>(ImportSourceType.class);

    public CompositeRemoteStatementClient(RemoteStatementClient wechatClient,
                                          RemoteStatementClient alipayClient) {
        delegates.put(ImportSourceType.WECHAT, wechatClient);
        delegates.put(ImportSourceType.ALIPAY, alipayClient);
    }

    @Override
    public InputStream fetch(ImportAuthorization authorization, LocalDate billDate) {
        RemoteStatementClient client = delegates.get(authorization.getSourceType());
        if (client == null) {
            throw new BusinessException("REMOTE_SOURCE_UNSUPPORTED", "暂不支持的授权来源: " + authorization.getSourceType());
        }
        if (billDate == null) {
            throw new BusinessException("BILL_DATE_REQUIRED", "请指定账单日期");
        }
        return client.fetch(authorization, billDate);
    }
}
