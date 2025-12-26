package com.lazyledger.external.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ExternalServicesProperties.class)
public class ExternalServiceConfiguration {
}
