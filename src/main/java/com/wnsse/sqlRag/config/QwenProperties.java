package com.wnsse.sqlRag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "qwen.api")
public class QwenProperties {
    private String key;
    private String host;
}
