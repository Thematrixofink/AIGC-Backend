package com.ink.backend.config;

import com.zhipu.oapi.ClientV4;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "glm")
@Data
public class GLM4Config {

    private String apiKey;

    @Bean
    public ClientV4 getClient(){
        ClientV4 client = new ClientV4.Builder(apiKey)
                .build();
        return client;
    }
}
