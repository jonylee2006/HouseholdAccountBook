package com.lazyledger.external;

import com.lazyledger.external.config.ExternalServicesProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificationServiceClientTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void shouldTriggerRemoteClassificationService() throws InterruptedException {
        ExternalServicesProperties properties = new ExternalServicesProperties();
        properties.getClassification().setUrl(server.url("/").toString());
        properties.getClassification().setApiKey("secret-key");
        ClassificationServiceClient client = new ClassificationServiceClient(WebClient.builder(), properties);
        server.enqueue(new MockResponse().setResponseCode(200));

        client.trigger(1L, 2L, List.of(3L, 4L));

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeader("X-API-Key")).isEqualTo("secret-key");
        assertThat(request.getPath()).isEqualTo("/imports");
        assertThat(request.getBody().readUtf8()).contains("\"ledgerId\":1", "\"jobId\":2");
    }
}
