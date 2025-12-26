package com.lazyledger.external;

import com.lazyledger.external.config.ExternalServicesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ClassificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ClassificationServiceClient.class);

    private final WebClient webClient;
    private final ExternalServicesProperties properties;
    private final boolean enabled;

    public ClassificationServiceClient(WebClient.Builder builder, ExternalServicesProperties properties) {
        this.properties = properties;
        String baseUrl = properties.getClassification().getUrl();
        this.enabled = baseUrl != null && !baseUrl.isBlank();
        this.webClient = this.enabled ? builder.baseUrl(baseUrl).build() : builder.build();
    }

    public void trigger(Long ledgerId, Long jobId, List<Long> transactionIds) {
        if (!enabled) {
            log.debug("Classification service not configured, skip remote call");
            return;
        }
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
