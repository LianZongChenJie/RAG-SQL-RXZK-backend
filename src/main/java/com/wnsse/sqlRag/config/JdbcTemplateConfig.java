package com.wnsse.sqlRag.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class JdbcTemplateConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        // dynamic-datasource 会自动注入 DynamicRoutingDataSource
        // 这里直接使用即可
        return new JdbcTemplate(dataSource);
    }
}