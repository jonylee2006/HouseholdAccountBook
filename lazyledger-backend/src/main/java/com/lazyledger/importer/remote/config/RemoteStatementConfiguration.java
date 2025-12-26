package com.lazyledger.importer.remote.config;

import com.lazyledger.importer.remote.RemoteStatementClient;
import com.lazyledger.importer.remote.impl.CompositeRemoteStatementClient;
import com.lazyledger.importer.remote.impl.WechatStatementClient;
import com.lazyledger.importer.remote.impl.AlipayStatementClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(RemoteStatementProperties.class)
public class RemoteStatementConfiguration {

    @Bean
    public WebClient remoteStatementWebClient(WebClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public RemoteStatementClient remoteStatementClient(RemoteStatementProperties properties,
                                                       WebClient webClient) {
        return new CompositeRemoteStatementClient(
                new WechatStatementClient(webClient, properties.getWechat()),
                new AlipayStatementClient(webClient, properties.getAlipay())
        );
    }
}
