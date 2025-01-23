package in.clear.tax_harvester.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "Basic c2F2ZWFkbWluOk5ORHJuQ1BLQ1dnUQ=="); // Consider getting this value securely
            // Add other headers if necessary
        };
    }
}