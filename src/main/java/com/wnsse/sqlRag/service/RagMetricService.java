package com.wnsse.sqlRag.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.wnsse.sqlRag.common.PageResult;
import com.wnsse.sqlRag.common.Result;
import com.wnsse.sqlRag.constant.RagMetricStatusConstant;
import com.wnsse.sqlRag.constant.SqlProsConstant;
import com.wnsse.sqlRag.entity.RagMetric;
import com.wnsse.sqlRag.mapper.RagMetricMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagMetricService {

    private final RagMetricMapper ragMetricMapper;
    private final QwenService qwenService;
    private final SqlExecutionService sqlExecutionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SqlRulesLoader sqlRulesLoader;

    private String sqlRules;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource(SqlProsConstant.OBJECTIVE_FILE);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                sqlRules = reader.lines().collect(Collectors.joining("\n"));
            }
            log.info("SQL规则文件加载成功，长度: {}", sqlRules.length());
        } catch (Exception e) {
            log.error("加载SQL规则文件失败", e);
            throw new RuntimeException("加载SQL规则文件失败: " + e.getMessage(), e);
        }
    }

    public PageResult<RagMetric> getPageList(Integer pageNum, Integer pageSize, String name, Integer status) {
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        pageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        Long offset = (long) (pageNum - 1) * pageSize;
        List<RagMetric> list = ragMetricMapper.selectPageList(offset, pageSize.longValue(), name, status);
        Long total = ragMetricMapper.count(name, status);

        return new PageResult<>(list, total, pageNum, pageSize);
    }

    public RagMetric getDetail(Integer id) {
        return ragMetricMapper.selectById(id);
    }

    public void add(RagMetric ragMetric) {
        ragMetric.setStatus(0);
        ragMetric.setCreatedTime(LocalDateTime.now());
        ragMetricMapper.insert(ragMetric);
    }

    public void update(RagMetric ragMetric) {
        ragMetric.setStatus(0);
        ragMetric.setComment(null);
        ragMetric.setUpdateTime(LocalDateTime.now());
        ragMetricMapper.updateById(ragMetric);
    }

    @DS("master")
    @Async
    public void generateSqlAsync() {
        log.info("开始批量生成SQL任务");
        try {
            List<RagMetric> pendingList = ragMetricMapper.selectPendingGenerate();
            log.info("查询到{}个待生成指标", pendingList.size());

            for (RagMetric metric : pendingList) {
                try {
                    log.info("正在处理指标: id={}, name={}", metric.getId(), metric.getName());

                    // -- 加载 SQL 生成规则 (object_rule.md)
                    String sqlRulesPrompt = sqlRulesLoader.buildRulesPrompt();

                    // -- 构造 System Prompt = 前端传入的 sqlStr(范式) + SQL生成规则
                    String systemPrompt = "你是一个SQL专家。请根据以下SQL生成规则，为每个指标生成正确的SQL范式查询语句和DSL描述。\n\n"
                            + "## 指标名称\n" + metric.getName() + "\n\n"
                            + "## 指标定义\n" + metric.getDesc() + "\n\n"
                            + "## 指标参考描述\n" + metric.getNode() + "\n\n"
                            + "## 查询表名称\n" + "tbobjectivedata" + "\n\n"
                            + "## SQL生成规则\n" + sqlRulesPrompt + "\n\n"
                            + "## 要求\n"
                            + "- 生成数据库名.数据库表名称形式的sql语句\n"
                            + "- 严格执行指标定义，明确定义范围，按照范围生成sql查询条件\n"
                            + "- 只返回SQL语句，不要包含任何解释、Markdown标记或额外内容\n"
                            + "- SQL语句必须是合法的MySQL查询语句\n"
                            + "- SQL语句中的字段别名必须使用指标参考描述中对应的中文名称作为别名\n"
                            + "- 如果用户问题与查询范式无关，返回空字符串";

                    String userPrompt = buildPrompt(metric.getName(), metric.getDesc());
                    String[][] sqlGenMessages = {
                            { "system", systemPrompt },
                            { "user", userPrompt }
                    };
                    // 使用千问大模型生成SQL语句和DSL描述
                    String response = qwenService.nonStreamChat(sqlGenMessages);
                    // String prompt = buildPrompt(metric.getName(), metric.getDesc());
                    // String response = qwenService.nonStreamChat(new String[][]{
                    // {"system", "你是一个SQL生成专家，请根据SQL生成规则，为每个指标生成正确的SQL查询语句和DSL描述"},
                    // {"user", prompt}
                    // });

                    SqlGenerationResult result = parseResponse(response);

                    // 兜底：如果AI生成了真实表名而非{tableName}占位符，强制替换
                    String normalizedSql = result.getSqlStr()
                            .replaceAll("(?i)tbobjectivedata\\d*[_a-zA-Z0-9]*", "{tableName}")
                            .replaceAll("(?i)tbobjectivedata", "{tableName}");

                    ragMetricMapper.updateSqlAndDsl(
                            metric.getId(),
                            normalizedSql,
                            result.getDsl(),
                            0,
                            null,
                            LocalDateTime.now());
                    log.info("指标生成成功: id={}", metric.getId());

                } catch (Exception e) {
                    log.error("指标生成失败: id={}, name={}, error={}", metric.getId(), metric.getName(), e.getMessage());
                    ragMetricMapper.updateSqlAndDsl(
                            metric.getId(),
                            null,
                            null,
                            -3,
                            "生成失败: " + e.getMessage(),
                            LocalDateTime.now());
                }
            }
        } catch (Exception e) {
            log.error("批量生成SQL任务异常", e);
        }
        log.info("批量生成SQL任务结束");
    }

    private String buildPrompt(String name, String desc) {
        return """
                请根据以下SQL生成规则，为指标"NAME_PLACEHOLDER"生成对应的SQL查询语句和DSL描述

                SQL生成规则:
                SQL_RULES_PLACEHOLDER

                指标描述: DESC_PLACEHOLDER

                要求:
                1. SQL中表名必须使用 {tableName} 作为占位符,例如:SELECT COUNT(*) FROM {tableName} WHERE ...
                2. 返回格式必须为JSON格式,包含sql_str和dsl两个字段
                3. sql_str字段为生成的SQL查询语句,表名用 {tableName} 占位
                4. dsl字段是对该指标的JSON结构化描述,包含指标名称、描述和统计类型等信息
                示例返回格式:
                {"sql_str":"SELECT COUNT(*) FROM {tableName} WHERE ...","dsl":"{\\"name\\":\\"指标名称\\",\\"description\\":\\"指标描述\\",\\"aggregationType\\":\\"COUNT\\"}"}
                """
                .replace("NAME_PLACEHOLDER", name)
                .replace("DESC_PLACEHOLDER", desc != null ? desc : "")
                .replace("SQL_RULES_PLACEHOLDER", sqlRules);
    }

    private SqlGenerationResult parseResponse(String response) {
        try {
            response = response.trim();

            if (response.startsWith("```")) {
                int firstNewline = response.indexOf("\n");
                if (firstNewline > 0) {
                    response = response.substring(firstNewline + 1);
                }
                if (response.endsWith("```")) {
                    response = response.substring(0, response.lastIndexOf("```")).trim();
                }
            }

            JsonNode jsonNode = objectMapper.readTree(response.trim());
            String sqlStr = jsonNode.has("sql_str") ? jsonNode.get("sql_str").asText() : "";
            String dsl = jsonNode.has("dsl") ? jsonNode.get("dsl").asText() : "";

            return new SqlGenerationResult(sqlStr, dsl);
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", response, e);
            throw new RuntimeException("解析响应失败: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> executeSqlByTableName(String tableName, Integer metricId, String metricCode) {
        List<RagMetric> approvedMetrics = ragMetricMapper.selectByStatus(1);
        if (approvedMetrics.isEmpty()) {
            throw new RuntimeException("没有通过审核的指标");
        }

        // 按metricId和metricCode过滤，传哪个按哪个过滤，都不传则查全部
        List<RagMetric> filteredMetrics = approvedMetrics.stream()
                .filter(m -> metricId == null || m.getId().equals(metricId))
                .filter(m -> metricCode == null || metricCode.isEmpty() || metricCode.equals(m.getCode()))
                .toList();

        List<Map<String, Object>> results = new ArrayList<>();
        for (RagMetric metric : filteredMetrics) {
            try {
                if (metric.getSqlStr() == null || metric.getSqlStr().isEmpty()) {
                    continue;
                }

                String sql = metric.getSqlStr().replace("{tableName}", tableName);
                List<Map<String, Object>> data = sqlExecutionService.executeQuery(sql);

                Map<String, Object> result = new HashMap<>();
                result.put("metricId", metric.getId());
                result.put("metricCode", metric.getCode());
                result.put("metricName", metric.getName());
                result.put("data", data);
                results.add(result);
            } catch (Exception e) {
                log.error("执行指标SQL失败: id={}, name={}, error={}", metric.getId(), metric.getName(), e.getMessage());
                Map<String, Object> result = new HashMap<>();
                result.put("metricId", metric.getId());
                result.put("metricCode", metric.getCode());
                result.put("metricName", metric.getName());
                result.put("error", "执行失败: " + e.getMessage());
                results.add(result);
            }
        }
        return results;
    }

    private static class SqlGenerationResult {
        private final String sqlStr;
        private final String dsl;

        public SqlGenerationResult(String sqlStr, String dsl) {
            this.sqlStr = sqlStr;
            this.dsl = dsl;
        }

        public String getSqlStr() {
            return sqlStr;
        }

        public String getDsl() {
            return dsl;
        }
    }

    /**
     * 更具数据库 status 状态更新 DSL
     * @return 执行状态消息
     */
    public Result<Map<String,String>> updateDsl() {
        Map<String,String> result = new HashMap<>();
        List<RagMetric> approvedMetrics = ragMetricMapper.selectByStatus(RagMetricStatusConstant.REJECTED);
        approvedMetrics.addAll(ragMetricMapper.selectByStatus(RagMetricStatusConstant.GENERATION_FAILED));
        if (approvedMetrics.isEmpty()) {
            return Result.success("没有通过审核的指标", result);
        }
        try {
            log.info("开始批量更新DSL任务");
            updateSqlAsync(approvedMetrics);
        }catch (Exception e){
            log.error("批量更新SQL任务发生异常{}", e.getMessage());
            Result.error(500, String.format("批量更新DSL任务异常%s", e) );
        }
        return Result.success("开始批量更新DSL任务", result);
    }

    @DS("master")
    public void updateSqlAsync(List<RagMetric> updateList) throws Exception{
        log.info("开始批量更新SQL任务");
        try {

            for (RagMetric metric : updateList) {
                try {
                    log.info("正在处理指标: id={}, name={}", metric.getId(), metric.getName());

                    // -- 加载 SQL 生成规则 (object_rule.md)
                    String sqlRulesPrompt = sqlRulesLoader.buildRulesPrompt();

                    // -- 构造 System Prompt = 前端传入的 sqlStr(范式) + SQL生成规则
                    String systemPrompt = "你是一个SQL专家。请根据以下SQL生成规则和已有的sql范式和需要改正的内容，重新生成查询语句和DSL描述。\n\n"
                            + "## 指标名称\n" + metric.getName() + "\n\n"
                            + "## 指标定义\n" + metric.getDesc() + "\n\n"
                            + "## 指标参考描述\n" + metric.getNode() + "\n\n"
                            + "## 查询表名称\n" + "tbobjectivedata" + "\n\n"
                            + "## SQL生成规则\n" + sqlRulesPrompt + "\n\n"
                            + "## 已有sql范式\n" + metric.getSqlStr() + "\n\n"
                            + "## 需要改正的地方\n" + metric.getComment() + "\n\n"
                            + "## 要求\n"
                            + "- 生成数据库名.数据库表名称形式的sql语句\n"
                            + "- 严格执行指标定义，明确定义范围，按照范围生成sql查询条件\n"
                            + "- 只返回SQL语句，不要包含任何解释、Markdown标记或额外内容\n"
                            + "- SQL语句必须是合法的MySQL查询语句\n"
                            + "- SQL语句中的字段别名必须使用指标参考描述中对应的中文名称作为别名\n"
                            + "- 如果用户问题与查询范式无关，返回空字符串";

                    String userPrompt = buildPrompt(metric.getName(), metric.getDesc());
                    String[][] sqlGenMessages = {
                            { "system", systemPrompt },
                            { "user", userPrompt }
                    };
                    // 使用千问大模型生成SQL语句和DSL描述
                    String response = qwenService.nonStreamChat(sqlGenMessages);

                    SqlGenerationResult result = parseResponse(response);

                    // 兜底：如果AI生成了真实表名而非{tableName}占位符，强制替换
                    String normalizedSql = result.getSqlStr()
                            .replaceAll("(?i)tbobjectivedata\\d*[_a-zA-Z0-9]*", "{tableName}")
                            .replaceAll("(?i)tbobjectivedata", "{tableName}");

                    ragMetricMapper.updateSqlAndDsl(
                            metric.getId(),
                            normalizedSql,
                            result.getDsl(),
                            0,
                            null,
                            LocalDateTime.now());
                    log.info("指标生成成功: id={}", metric.getId());

                } catch (Exception e) {
                    log.error("指标生成失败: id={}, name={}, error={}", metric.getId(), metric.getName(), e.getMessage());
                    ragMetricMapper.updateSqlAndDsl(
                            metric.getId(),
                            null,
                            null,
                            -3,
                            "生成失败: " + e.getMessage(),
                            LocalDateTime.now());
                }
            }
        } catch (Exception e) {
            log.error("批量更新SQL任务异常", e);
            throw new Exception("批量更新SQL任务异常", e);
        }
        log.info("批量SQL任务结束");
    }

    public RagMetric getRagMetric(String code){
        if (StringUtils.isEmpty(code)){
            log.info("code 为空请确认code值");
            return null;
        }
        return ragMetricMapper.selectByCode(code);
    }
}