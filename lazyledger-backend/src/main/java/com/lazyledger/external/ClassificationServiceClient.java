package com.lazyledger.external;

import com.lazyledger.external.config.ExternalServicesProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ClassificationServiceClient {

    private final WebClient webClient;
    private final ExternalServicesProperties properties;

    public ClassificationServiceClient(WebClient.Builder builder, ExternalServicesProperties properties) {
        this.properties = properties;
        this.webClient = builder.baseUrl(properties.getClassification().getUrl()).build();
    }

    public void trigger(Long ledgerId, Long jobId, List<Long> transactionIds) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ledgerId", ledgerId);
        payload.put("jobId", jobId);
        payload.put("transactionIds", transactionIds);
        webClient.post()
                .uri("/imports")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.set("X-API-Key", properties.getClassification().getApiKey()))
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
