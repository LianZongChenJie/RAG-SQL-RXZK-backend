package com.wnsse.sqlRag.service;

import com.wnsse.sqlRag.constant.SqlProsConstant;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SurveyRulesLoader {

    private String rulesContent;

    /**
     * 初始化时从 classpath:object_rule.md 加载规则内容
     */
    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource(SqlProsConstant.SURVEY_FILE);
            try (InputStream is = resource.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                this.rulesContent = reader.lines().collect(Collectors.joining("\n"));
            }
            log.info("SQL生成规则加载成功，长度: {} 字符", rulesContent.length());
        } catch (IOException e) {
            log.warn("未找到 object_rule.md 文件，使用默认空规则", e);
            this.rulesContent = "";
        }
    }

    /**
     * 获取完整的 SQL 生成规则文本
     */
    public String getRules() {
        return rulesContent;
    }

    /**
     * 构建注入到 system prompt 中的规则段落
     */
    public String buildRulesPrompt() {
        if (rulesContent == null || rulesContent.isEmpty()) {
            return "";
        }
        return "\n\n## SQL生成规则\n请严格按照以下规则生成对应的SQL查询语句：\n\n" + rulesContent;
    }
}
