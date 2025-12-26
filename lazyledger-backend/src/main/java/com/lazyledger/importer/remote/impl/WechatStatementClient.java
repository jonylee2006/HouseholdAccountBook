package com.lazyledger.importer.remote.impl;

import com.lazyledger.importer.remote.RemoteStatementClient;
import com.lazyledger.importer.remote.config.RemoteStatementProperties;
import com.lazyledger.ledger.domain.ImportAuthorization;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.net.URI;

public class WechatStatementClient implements RemoteStatementClient {

    private final WebClient webClient;
    private final RemoteStatementProperties.Provider properties;

    public WechatStatementClient(WebClient webClient, RemoteStatementProperties.Provider properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    @Override
    public InputStream fetch(ImportAuthorization authorization, LocalDate billDate) {
        URI uri = UriComponentsBuilder.fromHttpUrl(properties.getBillUrl())
                .queryParam("bill_date", billDate)
                .build(true)
                .toUri();
        byte[] content = webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .headers(headers -> headers.setBearerAuth(authorization.getAccessToken()))
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
        return new ByteArrayInputStream(content == null ? new byte[0] : content);
    }
}
