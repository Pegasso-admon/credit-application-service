package com.coopcredit.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${risk-central.base-url:http://localhost:8081}")
    private String riskCentralBaseUrl;

    @Value("${risk-central.connection-timeout:5000}")
    private int connectionTimeout;

    @Value("${risk-central.read-timeout:5000}")
    private int readTimeout;

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        /**
         * Create request factory with timeouts.
         */
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectionTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return builder
                .baseUrl(riskCentralBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
