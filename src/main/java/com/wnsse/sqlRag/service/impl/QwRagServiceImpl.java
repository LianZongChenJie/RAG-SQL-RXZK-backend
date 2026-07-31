package com.wnsse.sqlRag.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wnsse.sqlRag.common.PageResult;
import com.wnsse.sqlRag.constant.SqlProsConstant;
import com.wnsse.sqlRag.entity.PageQuestion;
import com.wnsse.sqlRag.entity.QwRagStreamRequest;
import com.wnsse.sqlRag.entity.RagMetric;
import com.wnsse.sqlRag.entity.UserQAHistory;
import com.wnsse.sqlRag.enumeration.QuestionTypeEnum;
import com.wnsse.sqlRag.mapper.UserQAHistoryMapper;
import com.wnsse.sqlRag.service.*;
import com.wnsse.sqlRag.util.SseEmitterUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@Slf4j
@Service
@RequiredArgsConstructor
public class QwRagServiceImpl implements QwRagService {

    private final QwenService qwenService;
    private final SqlExecutionService sqlExecutionService;
    private final SqlRulesLoader sqlRulesLoader;
    private final SurveyRulesLoader surveyRulesLoader;
    private final ChartRulesLoader chartRulesLoader;
    private final ObjectMapper objectMapper;

    /**
     * 异步任务线程池
     */
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "qwrag-async-");
        t.setDaemon(true);
        return t;
    });
    private final UserQAHistoryService userQAHistoryService;
    private final UserQAHistoryMapper userQAHistoryMapper;
    private final RagMetricService ragMetricService;

    @Override
    public String validateRequest(QwRagStreamRequest request) {
        // ... 保持原有校验逻辑不变 ...
        String message = request.getMessage();
        String dbName = request.getDbName();
        String tableName = request.getTableName();
        String dbType = request.getDbType();
        String lId = request.getLId();
        String code = request.getCode();
        String sqlStr = request.getSqlStr();
        String lQuestionnaireId = request.getLQuestionnaireId();
        String strAnswerColumn = request.getStrAnswerColumn();
        String strType = request.getStrType();
        String strDesc = request.getStrDesc();

        if (StringUtils.isBlank(message)) {
            log.error("message 不能为空");
            return "message 不能为空";
        }
        if (StringUtils.isBlank(dbName)) {
            log.error("dbName 不能为空");
            return "dbName 不能为空";
        }
        if (StringUtils.isBlank(tableName)) {
            log.error("tableName 不能为空");
            return "tableName 不能为空";
        }
        if (StringUtils.isBlank(dbType)) {
            log.error("dbType 不能为空");
            return "dbType 不能为空";
        }

        if (SqlProsConstant.OBJECTIVE_DB_TYPE.equals(dbType)) {
            log.info("客观数据类型，需要校验 lId, code, sqlStr 参数");
            if (StringUtils.isBlank(lId)) {
                log.error("lId 不能为空");
                return "lId 不能为空";
            }
            if (StringUtils.isBlank(code)) {
                log.error("code 不能为空");
                return "code 不能为空";
            }
            if (StringUtils.isBlank(sqlStr)) {
                log.error("sqlStr 不能为空");
                return "sqlStr 不能为空";
            }
            log.info("客观数据校验完成！");
        } else {
            log.info("调研数据类型，需要校验 lQuestionnaireId, strAnswerColumn strType strDesc 参数");
            if (StringUtils.isBlank(lQuestionnaireId)) {
                log.error("lQuestionnaireId 不能为空");
                return "lQuestionnaireId 不能为空";
            }
            if (StringUtils.isBlank(strAnswerColumn)) {
                log.error("strAnswerColumn 不能为空");
                return "strAnswerColumn 不能为空";
            }
            if (StringUtils.isBlank(strType)) {
                log.error("strType 不能为空");
                return "strType 不能为空";
            }
            if (StringUtils.isBlank(strDesc)) {
                log.error("strDesc 不能为空");
                return "strDesc 不能为空";
            }
            log.info("调研数据校验完成！");
        }

        return null;
    }

    @Override
    public void processStream(QwRagStreamRequest request, SseEmitter emitter) {
        // 通过code值获取数据库中真是的问题内容
        if (!StringUtils.isEmpty(request.getCode()) && !StringUtils.isEmpty(request.getCode())) {
            RagMetric metric = ragMetricService.getRagMetric(request.getCode());
            {
                if (metric != null) {
                    request.setMessage(metric.getProgramName());
                }
            }
        }
        // 拼接messageDesc到message字段上
        if (!StringUtils.isEmpty(request.getMessageDesc()) && !StringUtils.isEmpty(request.getMessageDesc())){
            request.setMessage(request.getMessage() + "-" + request.getMessageDesc());
        }
        CompletableFuture.runAsync(() -> {
            try {
                doProcess(request, emitter);
            } catch (Exception e) {
                log.error("RAG处理失败: {}", e.getMessage(), e);
                // 使用安全方法发送错误
                SseEmitterUtil.sendErrorEventSafely(emitter, e.getMessage());
            }
        }, taskExecutor);
    }


    @Override
    public String getObjectSQlAndData(QwRagStreamRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 500);
        log.info("开始检验客观数据参数参数");
        String errorMessage = validateRequestForObjectSql(request);
        log.info("检验客观数据参数参数完成");
        try {
            if (errorMessage != null) {
                result.put("msg", errorMessage + "校验失败，请检查参数是否正确！");
                return objectMapper.writeValueAsString(result);
            }
            String message = request.getMessage();
            String dbName = request.getDbName();
            String tableName = request.getTableName();
            String lId = request.getLId();
            String sqlStr = request.getSqlStr();
            log.info("调用千问大模型生成sql");
            String generatedSql = generateSql(message, dbName, tableName, lId, sqlStr);
            log.info("调用千问大模型生成sql完成,开始处理模型返回结果");
            String cleanSql = cleanSql(generatedSql);
            List<Map<String, Object>> queryResult = sqlExecutionService.execute(cleanSql);
            result.put("code", 200);
            result.put("sql", cleanSql);
            result.put("data", queryResult);
            log.info("处理模型返回结果完成,请求即将返回");
            return objectMapper.writeValueAsString(result);
        }catch (Exception e){
            log.error("RAG处理失败: {}", e.getMessage(), e);
            result.put("msg", "RAG处理失败");
            return "{\"code\": 500, \"msg\": \"RAG处理失败\"}";
        }
    }

    @Override
    public String getSurveySQLAndData(QwRagStreamRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 500);
        log.info("开始检验调研数据参数参数");
        String errorMessage = validateRequestForSurveySql(request);
        log.info("检验调研数据参数参数完成");
        try {
            if (errorMessage != null) {
                result.put("msg", errorMessage + "校验失败，请检查参数是否正确！");
                return objectMapper.writeValueAsString(result);
            }
            String message = request.getMessage();
            String dbName = request.getDbName();
            String tableName = request.getTableName();
            String lQuestionnaireId = request.getLQuestionnaireId();
            String strAnswerColumn = request.getStrAnswerColumn();
            String strDesc = request.getStrDesc();
            String strType = request.getStrType();
            String snapshotId = request.getSnapshotId();
            String lRespondentId = request.getLRespondentId();
            String lOrgId = request.getLOrgId();
            String[] strTitleList = request.getStrTitleList();
            String[] strOptionList = request.getStrOptionList();
            log.info("调用千问大模型生成sql");
            String generatedSql = generateSqlForSurvey(message, dbName, tableName, lQuestionnaireId, strAnswerColumn, strDesc, strType, snapshotId, lRespondentId, lOrgId, strTitleList, strOptionList);
            log.info("调用千问大模型生成sql完成,开始处理模型返回结果");
            String cleanSql = cleanSql(generatedSql);
            List<Map<String, Object>> queryResult = sqlExecutionService.execute(cleanSql);
            result.put("code", 200);
            result.put("sql", cleanSql);
            result.put("data", queryResult);
            log.info("处理模型返回结果完成,请求即将返回");
            return objectMapper.writeValueAsString(result);
        }catch (Exception e){
            log.error("RAG处理失败: {}", e.getMessage(), e);
            result.put("msg", "RAG处理失败");
            return "{\"code\": 500, \"msg\": \"RAG处理失败\"}";
        }
    }

    private String validateRequestForSurveySql(QwRagStreamRequest request){
        // ... 保持原有校验逻辑不变 ...
        String message = request.getMessage();
        String dbName = request.getDbName();
        String tableName = request.getTableName();
        String dbType = request.getDbType();
        String lQuestionnaireId = request.getLQuestionnaireId();
        String strAnswerColumn = request.getStrAnswerColumn();
        String strType = request.getStrType();
        String strDesc = request.getStrDesc();
        String snapshotId = request.getSnapshotId();
        String lRespondentId = request.getLRespondentId();
        String lOrgId = request.getLOrgId();
        log.info("调研数据类型，需要校验 lQuestionnaireId, strAnswerColumn strType strDesc 参数");
        if (StringUtils.isBlank(message)) {
            log.error("message 不能为空");
            return "message 不能为空";
        }
        if (StringUtils.isBlank(dbName)) {
            log.error("dbName 不能为空");
            return "dbName 不能为空";
        }
        if (StringUtils.isBlank(tableName)) {
            log.error("tableName 不能为空");
            return "tableName 不能为空";
        }
        if (StringUtils.isBlank(dbType)) {
            log.error("dbType 不能为空");
            return "dbType 不能为空";
        }

        if (StringUtils.isBlank(lQuestionnaireId)) {
            log.error("lQuestionnaireId 不能为空");
            return "lQuestionnaireId 不能为空";
        }
        if (StringUtils.isBlank(strAnswerColumn)) {
            log.error("strAnswerColumn 不能为空");
            return "strAnswerColumn 不能为空";
        }
        if (StringUtils.isBlank(strType)) {
            log.error("strType 不能为空");
            return "strType 不能为空";
        }
        if (StringUtils.isBlank(strDesc)) {
            log.error("strDesc 不能为空");
            return "strDesc 不能为空";
        }
        if (StringUtils.isBlank(snapshotId)) {
            log.error("snapshotId 不能为空");
            return "snapshotId 不能为空";
        }
        if (StringUtils.isBlank(lRespondentId)) {
            log.error("lRespondentId 不能为空");
            return "lRespondentId 不能为空";
        }
        if (StringUtils.isBlank(lOrgId)) {
            log.error("lOrgId 不能为空");
            return "lOrgId 不能为空";
        }
        log.info("调研数据校验完成！");
        return null;
    }

    public String validateRequestForObjectSql(QwRagStreamRequest request) {
        String message = request.getMessage();
        String dbName = request.getDbName();
        String tableName = request.getTableName();
        String lId = request.getLId();
        String dbType = request.getDbType();
        String sqlStr = request.getSqlStr();

        if (StringUtils.isBlank(message)) {
            log.error("message 不能为空");
            return "message";
        }
        if (StringUtils.isBlank(dbName)) {
            log.error("dbName 不能为空");
            return "dbName";
        }
        if (StringUtils.isBlank(tableName)) {
            log.error("tableName 不能为空");
            return "tableName";
        }
        if (StringUtils.isBlank(dbType)) {
            log.error("dbType 不能为空");
            return "dbType";
        }
        if (StringUtils.isBlank(lId)) {
            log.error("lId 不能为空");
            return "lId";
        }
        if (StringUtils.isBlank(sqlStr)) {
            log.error("sqlStr 不能为空");
            return "sqlStr";
        }
        return null;
    }

    /**
     * 执行核心处理流程 - 修复逻辑错误
     */
    private void doProcess(QwRagStreamRequest request, SseEmitter emitter) {
        String dbType = request.getDbType();

        // 先校验dbType
        if (StringUtils.isBlank(dbType)) {
            SseEmitterUtil.sendErrorEventSafely(emitter, "请选择一个数据集");
            return;
        }

        // 修复逻辑：使用if-else避免重复执行
        if (StringUtils.equals(SqlProsConstant.OBJECTIVE_DB_TYPE, dbType)) {
            doProcessForObjective(request, emitter);
        } else if (StringUtils.equals(SqlProsConstant.SURVEY_DB_TYPE, dbType)) {
            doProcessForSurvey(request, emitter);
        } else if(StringUtils.equals(SqlProsConstant.ORGANIZATION_DB_TYPE, dbType)){
            doProcessForSurvey(request, emitter);
        }else {
            SseEmitterUtil.sendErrorEventSafely(emitter, "不支持的数据集类型: " + dbType);
        }
    }

    /**
     * 处理客观数据 - 增加连接状态检查
     */
    private void doProcessForObjective(QwRagStreamRequest request, SseEmitter emitter) {
        String dbName = request.getDbName();
        String tableName = request.getTableName();
        String lId = request.getLId();
        String sqlStr = request.getSqlStr();
        String code = request.getCode();
        String message = request.getMessage();
        String strOutType = request.getOutType();
        RagMetric metric = ragMetricService.getRagMetric(code);
        // ===================== 第一步：验证接受到的信息 =====================
        log.info("【第一步】验证请求参数");
        if (!SseEmitterUtil.sendEventAndCheck(emitter, "step", "正在校验请求信息...")) {
            log.warn("连接已关闭，停止处理");
            return;
        }

        // 查询指标校验
        if (StringUtils.isAnyBlank(sqlStr, code)) {
            SseEmitterUtil.sendErrorEventSafely(emitter, "请选择一个查询指标");
            return;
        }

        // 如果 tableName 为空，直接走千问对话
        if (StringUtils.isBlank(tableName)) {
            log.info("tableName为空，直接进入千问对话");
            if (!SseEmitterUtil.sendEventAndCheck(emitter, "step", "直接对话模式")) {
                return;
            }
            qwenService.streamChat(message, emitter);
            return;
        }

        if (!SseEmitterUtil.sendEventAndCheck(emitter, "step_done", "信息校验完成")) {
            return;
        }
        log.info("【第一步完成】参数校验通过");

        // ===================== 第二步：千问生成 SQL =====================
        log.info("【第二步】千问生成SQL");
        if (!SseEmitterUtil.sendEventAndCheck(emitter, "step", "正在分析数据并生成查询SQL...")) {
            return;
        }

        String generatedSql = generateSql(message, dbName, tableName, lId, sqlStr);
        String cleanSql = cleanSql(generatedSql);

        log.info("【第二步完成】生成SQL: {}", cleanSql);
        if (!SseEmitterUtil.sendEventAndCheck(emitter, "sql_generated", cleanSql)) {
            return;
        }

        // ===================== 第三步：执行SQL =====================
        log.info("【第三步】执行SQL");
        if (!SseEmitterUtil.sendEventAndCheck(emitter, "step", "正在执行查询SQL...")) {
            return;
        }

        if (StringUtils.isBlank(cleanSql)) {
            if (!SseEmitterUtil.sendEventAndCheck(emitter, "step_done", "无需查询，直接回答问题")) {
                return;
            }
            qwenService.streamChat(message, emitter);
            return;
        }

        List<List<Map<String, Object>>> allResult = sqlExecutionService.executeManyResult(cleanSql);
        //使用 Stream API 检查是否有任何非空结果集
        boolean hasData = allResult != null && !allResult.isEmpty()
                && allResult.stream().anyMatch(result -> result != null && !result.isEmpty());

        if (!hasData) {
            log.warn("【第三步】所有查询结果集都为空");
            SseEmitterUtil.sendEventSafely(emitter, "step_done", "查询完成，未查询到数据");
            SseEmitterUtil.sendEventSafely(emitter, "message", "未查询到符合条件的数据");

            CompletableFuture.delayedExecutor(500, TimeUnit.MILLISECONDS)
                    .execute(() -> {
                        try {
                            SseEmitterUtil.sendEventSafely(emitter, "done", "[DONE]");
                            emitter.complete();
                        } catch (Exception e) {
                            log.warn("关闭连接失败: {}", e.getMessage());
                        }
                    });
            return;
        }
        // 获取第一个非空结果集用于后续处理
        List<Map<String, Object>> firstNonEmptyResult = allResult.stream()
                .filter(result -> result != null && !result.isEmpty())
                .findFirst()
                .orElse(null);
        String resultJson;
        try {
            resultJson = objectMapper.writeValueAsString(firstNonEmptyResult);
        } catch (Exception e) {
            log.error("序列化查询结果失败", e);
            SseEmitterUtil.sendErrorEventSafely(emitter, "数据序列化失败");
            return;
        }

        log.info("【第三步完成】查询完成，返回 {} 条数据", firstNonEmptyResult.size());
        if (!SseEmitterUtil.sendEventAndCheck(emitter, "step_done", "查询完成，共返回 " + firstNonEmptyResult.size() + " 条数据")) {
            return;
        }

        log.info("【第四步】生成ElementUI Table结构");
        if (!SseEmitterUtil.sendEventAndCheck(emitter, "step", "正在生成表格展示结构...")) {
            return;
        }
        String elementUITableJson = generateElementUITableConfigForManyResult(allResult);

        // ===================== 第四步：流式渲染 =====================
        log.info("【第四步】千问流式渲染");
        if (!SseEmitterUtil.sendEventAndCheck(emitter, "step", "正在生成回答...")) {
            return;
        }
        // 4.1 生成图表配置
        String chartResult = "";
        log.info("【第四步-图表】生成ECharts图表配置");
        if(!StringUtils.isEmpty(strOutType) || !StringUtils.isBlank(strOutType)){
            chartResult = generateChartConfig(message, resultJson, strOutType);
        }


        // 4.2 流式请求生成自然语言回答
        String[][] renderMessages = buildRenderMessages(message, resultJson);
        String programDesc = metric.getProgramDesc();
        programDesc = cleanDesc(programDesc);
        programDesc = "指标计算说明：" + programDesc;
        String satisfactionFormula = buildSatisfactionFormula(resultJson);
        qwenService.streamChatForSummery(renderMessages, emitter, "chart", chartResult,programDesc);

        // ===================== 第五步：生成ElementUI Table结构 =====================
        if (elementUITableJson != null) {
            if (!SseEmitterUtil.sendEventAndCheck(emitter, "tableData", elementUITableJson)) {
                return;
            }
            SseEmitterUtil.sendEventSafely(emitter, "step_done", "表格结构生成完成");
            log.info("【第五步完成】ElementUI Table配置生成成功");
        } else {
            SseEmitterUtil.sendEventSafely(emitter, "step_done", "无需生成表格结构");
            log.info("【第五步完成】无需生成表格结构");
        }
    }

    /**
     * 处理调研数据 - 同样增加连接状态检查
     */
    private void doProcessForSurvey(QwRagStreamRequest request, SseEmitter emitter) {
        String dbName = request.getDbName();
        String tableName = request.getTableName();
        String message = request.getMessage();
        String code = request.getCode();

        String lQuestionnaireId = request.getLQuestionnaireId();
        String strType = request.getStrType();
        String strDesc = request.getStrDesc();
        String strAnswerColumn = request.getStrAnswerColumn();
        String snapshotId = request.getSnapshotId();
        String lRespondentId = request.getLRespondentId();
        String lOrgId = request.getLOrgId();
        String[] strTitleList = request.getStrTitleList();
        String[] strOptionList = request.getStrOptionList();
        RagMetric metric = ragMetricService.getRagMetric(code);
        if (metric == null){
            metric = new RagMetric();
            metric.setProgramDesc(QuestionTypeEnum.fromCode(strType).getDesc());
        }
        // ===================== 第一步：验证接受到的信息 =====================
        log.info("【第一步】验证请求参数");
        if (!SseEmitterUtil.sendEventAndCheck(emitter, "step", "正在校验请求信息...")) {
            log.warn("连接已关闭，停止处理");
            return;
        }

        // 查询指标校验
        if (StringUtils.isAnyBlank(lQuestionnaireId, strAnswerColumn, strType, strDesc, snapshotId, lRespondentId)) {
            SseEmitterUtil.sendErrorEventSafely(emitter, "调研数据参数错误");
            return;
        }

        // 如果 tableName 为空，直接走千问对话
        if (StringUtils.isBlank(tableName)) {
            log.info("tableName为空，直接进入千问对话");
            if (!SseEmitterUtil.sendEventAndCheck(emitter, "step", "直接对话模式")) {
                return;
            }
            qwenService.streamChat(message, emitter);
            return;
        }

        if (!SseEmitterUtil.sendEventAndCheck(emitter, "step_done", "信息校验完成")) {
            return;
        }
        log.info("【第一步完成】参数校验通过");

        // ===================== 第二步：千问生成 SQL =====================
        log.info("【第二步】千问生成SQL");
        if (!SseEmitterUtil.sendEventAndCheck(emitter, "step", "正在分析数据并生成查询SQL...")) {
            return;
        }

        String generatedSql = generateSqlForSurvey(message, dbName, tableName, lQuestionnaireId, strAnswerColumn, strDesc, strType, snapshotId, lRespondentId, lOrgId, strTitleList, strOptionList );
        String cleanSql = cleanSql(generatedSql);

        log.info("【第二步完成】生成SQL: {}", cleanSql);
        if (!SseEmitterUtil.sendEventAndCheck(emitter, "sql_generated", cleanSql)) {
            return;
        }

        // ===================== 第三步：执行SQL =====================
        log.info("【第三步】执行SQL");
        if (!SseEmitterUtil.sendEventAndCheck(emitter, "step", "正在执行查询SQL...")) {
            return;
        }

        if (StringUtils.isBlank(cleanSql)) {
            if (!SseEmitterUtil.sendEventAndCheck(emitter, "step_done", "无需查询，直接回答问题")) {
                return;
            }
            qwenService.streamChat(message, emitter);
            return;
        }

        List<Map<String, Object>> queryResult = sqlExecutionService.execute(cleanSql);
        if (queryResult.isEmpty()){
            log.warn("【第三步】所有查询结果集都为空");
            SseEmitterUtil.sendEventSafely(emitter, "step_done", "查询完成，未查询到数据");
            SseEmitterUtil.sendEventSafely(emitter, "message", "未查询到符合条件的数据");

            CompletableFuture.delayedExecutor(500, TimeUnit.MILLISECONDS)
                    .execute(() -> {
                        try {
                            SseEmitterUtil.sendEventSafely(emitter, "done", "[DONE]");
                            emitter.complete();
                        } catch (Exception e) {
                            log.warn("关闭连接失败: {}", e.getMessage());
                        }
                    });
            return;
        }
        String resultJson;
        try {
            resultJson = objectMapper.writeValueAsString(queryResult);
            resultJson = mergeOtherCategories(resultJson);
        } catch (Exception e) {
            log.error("序列化查询结果失败", e);
            SseEmitterUtil.sendErrorEventSafely(emitter, "数据序列化失败");
            return;
        }

        log.info("【第三步完成】查询完成，返回 {} 条数据", queryResult.size());
        if (!SseEmitterUtil.sendEventAndCheck(emitter, "step_done", "查询完成，共返回 " + queryResult.size() + " 条数据")) {
            return;
        }

        // ===================== 第四步：流式渲染 =====================
        log.info("【第四步】千问流式渲染");
        if (!SseEmitterUtil.sendEventAndCheck(emitter, "step", "正在生成回答...")) {
            return;
        }
        String elementUITableJson = generateElementUITableConfig(queryResult);
        try {
            elementUITableJson = mergeOtherCategoriesForQueryList(elementUITableJson);
        }catch (Exception e){
            log.error("表格数据合并查询结果集失败");
        }

        // 根据题型判断使用那种图表
        String chartType = null; // 默认条形图

        if ("填空".equals(QuestionTypeEnum.fromCode(strType).getName())) {
            chartType = "bar"; // 填空题使用柱状图
        }
        // 4.1 生成图表配置
        log.info("【第四步-图表】生成ECharts图表配置");
        String chartResult = generateChartConfig(message, resultJson, chartType);
        String programDesc = metric.getProgramDesc();
        programDesc = cleanDesc(programDesc);
        programDesc = "指标计算说明：" + programDesc;

        // 4.2 流式请求生成自然语言回答
        String[][] renderMessages = buildRenderMessages(message, resultJson);
        qwenService.streamChatForSummery(renderMessages, emitter, "chart", chartResult, programDesc);

        // ===================== 第五步：生成ElementUI Table结构 =====================
        log.info("【第五步】生成ElementUI Table结构");
        if (!SseEmitterUtil.sendEventAndCheck(emitter, "step", "正在生成表格展示结构...")) {
            return;
        }


        if (elementUITableJson != null) {
            if (!SseEmitterUtil.sendEventAndCheck(emitter, "tableData", elementUITableJson)) {
                return;
            }
            SseEmitterUtil.sendEventSafely(emitter, "step_done", "表格结构生成完成");
            log.info("【第五步完成】ElementUI Table配置生成成功");
        } else {
            SseEmitterUtil.sendEventSafely(emitter, "step_done", "无需生成表格结构");
            log.info("【第五步完成】无需生成表格结构");
        }
    }

    /**
     * 将未就业去向中以"其他"开头的多个结果合并为单个"其他"类别
     * @param resultJson 原始JSON字符串
     * @return 合并后的JSON字符串
     */
    public String mergeOtherCategories(String resultJson) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> originalList = objectMapper.readValue(resultJson,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));

        // 合并"其他"类别的作答人数
        int otherCount = 0;
        List<Map<String, Object>> mergedList = new ArrayList<>();

        for (Map<String, Object> item : originalList) {
            String category = (String) item.get("未就业去向");
            if (category != null && category.startsWith("其他")) {
                Object count = item.get("作答人数");
                if (count != null) {
                    otherCount += Integer.parseInt(count.toString());
                }
            } else {
                // 深拷贝保留原始项
                mergedList.add(new HashMap<>(item));
            }
        }

        // 添加合并后的"其他"项
        if (otherCount > 0) {
            int totalCount = 322; // 答题总人数保持不变
            Map<String, Object> otherItem = new HashMap<>();
            otherItem.put("未就业去向", "其他");
            otherItem.put("作答人数", otherCount);
            otherItem.put("占比", String.format("%.2f%%", (otherCount * 100.0 / totalCount)));
            otherItem.put("答题总人数", totalCount);
            mergedList.add(otherItem);
        }

        // 按作答人数降序排序
        mergedList.sort((a, b) -> {
            if (a.get("作答人数")!= null && !StringUtils.isEmpty(a.get("作答人数").toString())){
                int countA = Integer.parseInt(a.get("作答人数").toString());
                int countB = Integer.parseInt(b.get("作答人数").toString());
                return countB - countA;
            }
            return 0;
        });

        return objectMapper.writeValueAsString(mergedList);
    }

    /**
     * 将未就业去向中以"其他"开头的多个结果合并为单个"其他"类别
     * @param jsonStr 原始JSON字符串
     * @return 合并后的JSON字符串
     */
    public String mergeOtherCategoriesForQueryList(String jsonStr) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> originalMap = objectMapper.readValue(jsonStr, Map.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) originalMap.get("data");

        int otherCount = 0;
        List<Map<String, Object>> mergedList = new ArrayList<>();

        for (Map<String, Object> item : dataList) {
            String category = (String) item.get("未就业去向");
            if (category != null && category.startsWith("其他")) {
                Object count = item.get("作答人数");
                if (count != null) {
                    otherCount += Integer.parseInt(count.toString());
                }
            } else {
                mergedList.add(new HashMap<>(item));
            }
        }

        if (otherCount > 0) {
            int totalCount = 322;
            Map<String, Object> otherItem = new HashMap<>();
            otherItem.put("未就业去向", "其他");
            otherItem.put("作答人数", otherCount);
            otherItem.put("占比", String.format("%.2f%%", (otherCount * 100.0 / totalCount)));
            otherItem.put("答题总人数", totalCount);
            mergedList.add(otherItem);
        }

        mergedList.sort((a, b) -> {
            int countA = ((Number) a.get("作答人数")).intValue();
            int countB = ((Number) b.get("作答人数")).intValue();
            return countB - countA;
        });

        Map<String, Object> resultMap = new HashMap<>(originalMap);
        resultMap.put("data", mergedList);

        return objectMapper.writeValueAsString(resultMap);
    }

    /**
     * 客观数据生成 sql 方法
     * @param message
     * @param dbName
     * @param tableName
     * @param lId
     * @param sqlStr
     * @return
     */
    private String generateSql(String message, String dbName, String tableName, String lId, String sqlStr) {
        // ... 保持原有实现不变 ...
        String sqlRulesPrompt = sqlRulesLoader.buildRulesPrompt();
        String systemPrompt = "你是一个SQL专家。请根据以下SQL查询范式和规则，分析用户问题，生成对应的查询SQL语句。\n\n"
                + "## 查询库名称\n" + dbName + "\n\n"
                + "## 查询表名称\n" + tableName + "\n\n"
                + "## SQL查询范式（包含多个查询）\n" + sqlStr + "\n\n"
                + "## 必要查询条件\n" + "lDataSetId=" + lId + "\n\n"
                + "## SQL生成规则\n" + sqlRulesPrompt + "\n\n"
                + "## ⚠️ 强制要求（违反将导致回答无效）\n"
                + "1. SQL范式中包含多个用分号(;)分隔的独立查询语句\n"
                + "2. **你必须输出所有查询语句，数量必须与SQL范式中的查询数量完全一致**\n"
                + "3. 如果只输出部分查询，将被视为错误回答\n"
                + "4. 请先数一下SQL范式中有几个分号，然后生成相同数量的查询\n\n"
                + "## 输出格式\n"
                + "- 所有查询语句按顺序输出\n"
                + "- 每条语句以分号(;)结尾\n"
                + "- 语句之间用分号分隔（即每个查询后面都有分号）\n"
                + "- 示例格式：\n"
                + "  SELECT ... ;\n"
                + "  SELECT ... ;\n"
                + "  SELECT ... ;\n\n"
                + "## 要求\n"
                + "- 生成数据库名.数据库表名称形式的sql语句\n"
                + "- 只返回SQL语句，不要包含任何解释、Markdown标记或额外内容\n"
                + "- 占比比率等问题的查询结果要带上‘%’\n"
                + "- 如果用户问题与查询范式无关，返回空字符串";

        String[][] sqlGenMessages = {
                {"system", systemPrompt},
                {"user", message}
        };

        return qwenService.nonStreamChat(sqlGenMessages);
    }

    /**
     * 客观数据生成 sql 方法
     * @param message 用户问题
     * @param dbName 数据库名称
     * @param tableName 数据表名称
     * @param lQuestionnaireId 问卷调研 ID
     * @param strAnswerColumn 答案列
     * @param strDesc   问卷问题内容
     * @param strType   问卷题型
     * @param lRespondentId 组织Id
     * @return 返回大模型生成的 sql
     */
    private String generateSqlForSurvey(String message, String dbName, String tableName, String lQuestionnaireId, String strAnswerColumn, String strDesc, String strType, String snapshotId, String lRespondentId, String lOrgId, String[] strTitleList, String[] strOptionList) {
        // ... 保持原有实现不变 ...
        String sqlRulesPrompt = surveyRulesLoader.buildRulesPrompt();
        Map<String,String> databaseAndTable = getDataBaseAndTable(Integer.valueOf(lOrgId));

        String systemPrompt = "你是一个问题理解和SQL专家。请根据以下数据库、查询表名、问卷问题题型、问题内容，文件答案和列规则，分析用户问题，理解用户问题要问的内容，明确用户问题，根据SQL生成规则中的数据库版本，表描述和生成规则和统计内容生成对应的数据库版本的查询SQL语句。\n\n"
                + "## 查询库名称\n" + dbName + "\n\n"
                + "## 查询表名称\n" + tableName + "\n\n"
                + "## 快照id\n" + snapshotId + "\n\n"
                + "## lRespondentId（条件使用规则见下方）\n" + lRespondentId + "\n\n"
                + "## 问卷ID（字段映射：lQuestionnaireId = " + lQuestionnaireId + "）\n\n"
                + "## 题型\n" + QuestionTypeEnum.fromCode(strType).getName() + "\n\n"
                + "## 问卷问题内容\n" + strDesc + "\n\n"
                + "## 答案列\n" + strAnswerColumn + "\n\n"
                + "## 答案数据格式:[{\"id\":880,\"code\":\"330112\",\"value\":\"**\"}]\n\n"
                + "## 统计内容: "+ QuestionTypeEnum.fromCode(strType).getDescription()+ "\n\n"
                + "## SQL生成规则\n" + sqlRulesPrompt + "\n\n"
                + "## 满意度计算公式：选项1~5的权重分别是（5,4,3,2,1），根据权重计算所有人作答的和除作答人数\n\n"
                + "## 清洗的字段列中不包含:"+ strAnswerColumn.replace("strAnswer","")+"\n\n"
                + "## 重要：lRespondentId（必须严格遵守）\n"
                + "### 以下情况必须包含 lRespondentId 查询条件：\n"
                + "- 问题涉及「毕业年份」、「学历名称」、「学院名称」、「专业属性」等学生维度信息\n"
                + "- 问题内容中包含：毕业、学历、学院、专业、年级、班级、学号等关键词\n"
                + "- 问题需要关联学生库和/or学生表进行查询\n\n"
                + "### 以下情况禁止包含 lRespondentId 查询条件：\n"
                + "- 满意度评价类问题（如：对学校育人环境的满意度、对教学质量的满意度等）\n"
                + "- 通用统计类问题（如：总体满意度、答题人数统计等）\n"
                + "- 不涉及具体学生维度信息的问题\n\n"
                + "### 判断规则：\n"
                + "1. 首先分析「问卷问题内容」，提取关键词\n"
                + "2. 如果关键词匹配「必须包含」列表，则 WHERE 条件中添加 lRespondentId = " + lRespondentId + "\n"
                + "3. 如果关键词匹配「禁止包含」列表或无明显匹配，则 WHERE 条件中不添加 lRespondentId\n"
                + "4. 当不确定时，优先不添加 lRespondentId，因为这是全局筛选条件\n\n"
                + "## 学生库（仅当需要关联时使用）\n" + databaseAndTable.get("databaseName") + "\n\n"
                + "## 学生表（仅当需要关联时使用）\n" + databaseAndTable.get("tableName") + "\n\n"
                + "## 数据处理说明\n"
                + "- 含有strAnswer的数据列存储的是JSON数组格式，格式为：[{\"id\":xxx,\"code\":\"xxx\",\"value\":\"选项值\"}]\n"
                + "- 其他数据列为纯字符或者纯数字\n"
                + "- 查询时需要使用 JSON_EXTRACT 或 JSON_UNQUOTE 提取 value 字段的值\n"
                + "- 对于选择题，应该按提取出的 value 值进行 GROUP BY 分组统计，统计每个选项的人数\n"
                + "- 不要对特定的 value 值进行硬编码（如 '**'），应该动态提取所有不同的 value 值\n"
                + "- 示例：使用 GROUP BY JSON_UNQUOTE(JSON_EXTRACT(`strAnswer1`, '$[0].value'))\n\n"
                + "## 占比/比率字段格式化要求（重要）\n"
                + "- 所有计算占比、比率、百分比等字段，必须使用 CONCAT(ROUND(计算表达式, 2), '%') 格式\n"
                + "- 例如：CONCAT(ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM ...), 2), '%') AS 占比\n"
                + "- 例如：CONCAT(ROUND(平均值 / 5 * 100, 2), '%') AS 满意度百分比\n"
                + "- 例如：CONCAT(ROUND(SUM(权重) * 100.0 / (COUNT(*) * 5), 2), '%') AS 得分率\n"
                + "- 严禁直接使用 ROUND(...) AS 占比 或 计算表达式 AS 占比 而不带 % 符号\n"
                + "- 所有百分比结果必须包含 '%' 字符，便于前端直接展示\n\n"
                + "## 要求\n"
                + "- 单选五维题或者单选组合题型时需要根据选项和组合标题生成对应的SQL语句\n"
                + "- 选项："+ Arrays.toString(strTitleList) +"\n"
                + "- 组合标题：" + Arrays.toString(strOptionList) + "\n"
                + "- 生成数据库名.数据库表名称形式的sql语句\n"
                + "- 按照数据列的格式生成对应的sql\n"
                + "- 只返回SQL语句，不要包含任何解释、Markdown标记或额外内容\n"
                + "- SQL语句必须是合法的MySQL查询语句\n"
                + "- 题型：普通单选、多选、单选五维要统计答题总人数列\n"
                + "- 如果用户问题与查询范式无关，返回空字符串\n"
                + "- 生成SQL前，请先判断问题类型，决定是否包含 lRespondentId 条件";

        String[][] sqlGenMessages = {
                {"system", systemPrompt},
                {"user", message}
        };

        return qwenService.nonStreamChat(sqlGenMessages);
    }

    private String cleanSql(String generatedSql) {
        // ... 保持原有实现不变 ...
        if (generatedSql == null) {
            return "";
        }
        return generatedSql
                .replaceAll("```sql\\s*", "")
                .replaceAll("```\\s*", "")
                .replaceAll("^\\s*SQL\\s*", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String[][] buildRenderMessages(String message, String resultJson) {
        String systemPrompt = "你是一个数据总结、展示专家，负责根据用户的问题和查询结果，用自然语言回答用户问题。\n\n"
                + "## 用户问题\n" + message + "\n\n"
                + "## 查询结果（JSON格式）\n" + resultJson + "\n\n"
                + "## 输出要求（严格遵守）\n"
                + "1. 开头必须用一句话概括最核心的发现，不能上来就罗列数字。\n"
                + "2. 主体内容用2~3句话把数据串起来，每句话围绕一个主题展开，不要一句一罗列。句子之间要有逻辑连接词（其中、具体来看、值得注意的是、此外），数字要嵌进句子中间。\n"
                + "3. 所有计算符号（/、*、-、+、=、÷、×）严禁出现在正文中，一经出现视为格式错误。\n"
                + "4. 不得使用 ##、**、### 等 Markdown 标记。\n"
                + "5. 正文以句号结束，正文内容中不得包含任何形式的公式、计算式或指标说明文字。\n"
                + "6. 正文读起来要像新闻报道的导语加正文，而非数据表格的文本化。每句话至少包含一个数据和一个具体描述（地名、人群名、指标名等）。如果数据中有明显的大小、占比、高低关系，要在句子里体现出来。\n"
                + "7. 【数值对比约束】涉及两个数值对比时，必须同时写出被比较项的具体数值，禁止出现「略高于中位数5元」「超过均值269元」这类省略了被比较值的写法。\n"
                + "8. 【百分比格式】涉及百分比时，统一使用数字加百分号格式（如 25.78%），不得使“用百分之X点XX”等中文写法。\n";



        return new String[][]{
                {"system", systemPrompt},
                {"user", message}
        };
    }

    private String generateChartConfig(String message, String resultJson,String outType) {
        // ... 保持原有实现不变 ...
        try {
            String chartRulesPrompt = chartRulesLoader.buildRulesPrompt();

            String chartPrompt = "你是一个数据可视化专家。请根据用户的问题和查询结果，按照outType类型生成ECharts图表配置JSON。\n\n"
                    + "## 用户问题\n" + message + "\n\n"
                    + "## 查询结果（JSON格式）\n" + resultJson + "\n\n"
                    + "## outType\n" + outType + "\n\n"
                    + chartRulesPrompt + "\n\n"

                    + "## ⚠️ 第一步（最高优先级）：数据字段智能识别\n\n"
                    + "### 判断规则：遍历查询结果中所有字段，检查每行的值是否相同\n"
                    + "- 如果某字段在**所有行中的值完全相同**（如每行都是93.84）→ 判定为**汇总指标字段**\n"
                    + "- 如果某字段在**各行的值不同**（如514、22、271、1277）→ 判定为**分类指标字段**\n"
                    + "- 如果某字段是文本且各行的值不同（如'自由职业'、'单位就业'）→ 判定为**分类维度字段**\n\n"
                    + "### 严格执行：\n"
                    + "1. **汇总指标字段**（值全部相同的字段）**绝对不能**出现在xAxis、series.data、yAxis等数据位置\n"
                    + "2. 汇总指标字段只能出现在：标题（title.text）或tooltip的额外信息中\n"
                    + "3. **分类指标字段**（值各行不同的数值字段）才是图表的主数据\n"
                    + "4. 优先选择字段名含'人数'、'count'、'数量'的字段作为数据\n"
                    + "5. 次优选择字段名含'占比'、'percent'、'比例'的字段作为数据\n\n"

                    + "## 图表配置规则（必须严格遵守，每条规则对应具体的ECharts配置写法）\n\n"

                    + "### 1. 画布边距\n"
                    + "如果数据类别超过5个，必须设置grid撑开边距：\n"
                    + "grid: { left: 60, right: 60, top: 60, bottom: 80 }\n\n"

                    + "### 2. X轴标签防重叠（类别名>8字符时）\n"
                    + "必须同时设置以下三项：\n"
                    + "axisLabel: { rotate: 30, interval: 0 } + grid.bottom: 80\n\n"

                    + "### 3. 图例截断（图例项>6字符时，必须截断）\n"
                    + "必须使用legend.formatter + tooltip组合，禁止直接输出全称：\n"
                    + "legend: {\n"
                    + "  formatter: function(name) { return name.length > 6 ? name.substring(0, 6) + '...' : name; },\n"
                    + "  tooltip: { show: true }\n"
                    + "}\n\n"

                    + "### 4. 图例强制换行（图例名称含括号且>10字符时）\n"
                    + "使用\\n换行，每行不超过10个字符：\n"
                    + "legend: {\n"
                    + "  formatter: function(name) { var parts = name.split('('); return parts[0] + '\\n(' + (parts[1]||''); }\n"
                    + "}\n\n"

                    + "### 5. 图例过多（>6个）时的布局\n"
                    + "必须设置grid.top，并启用图例滚动：\n"
                    + "legend: { top: 55, type: 'scroll' }\n"
                    + "grid: { top: 90 }\n\n"

                    + "### 6. 饼图配置（所有饼图统一执行，不区分数量）\n"
                    + "- **图例靠右，图表靠左**：通过 `series.center: ['40%', '50%']` 将饼图左移，`legend.right: 20` 将图例靠右\n"
                    + "- `label` 统一不显示：`label: { show: false }`、`labelLine: { show: false }`\n"
                    + "- `radius: ['40%', '60%']`\n"
                    + "- `legend` 放在右侧，垂直居中，启用滚动，`tooltip` 显示完整信息（含百分比）\n"
                    + "- 图例项过多（>6个）时，`type: 'scroll'` 自动生效\n"
                    + "- 必须设置以下完整配置：\n"
                    + "```javascript\n"
                    + "{\n"
                    + "  series: [{\n"
                    + "    type: 'pie',\n"
                    + "    radius: ['40%', '60%'],\n"
                    + "    center: ['40%', '50%'],\n"
                    + "    label: { show: false },\n"
                    + "    labelLine: { show: false },\n"
                    + "    data: [ /* 动态从查询结果提取 */ ],\n"
                    + "    tooltip: {\n"
                    + "      trigger: 'item',\n"
                    + "      confine: true,\n"
                    + "      formatter: function(p) {\n"
                    + "        return p.name + ': ' + p.value + ' (' + p.percent + '%)';\n"
                    + "      }\n"
                    + "    }\n"
                    + "  }],\n"
                    + "  legend: {\n"
                    + "    orient: 'vertical',\n"
                    + "    right: 20,\n"
                    + "    top: 'center',\n"
                    + "    left: 'auto',\n"
                    + "    type: 'scroll',\n"
                    + "    tooltip: {\n"
                    + "      show: true,\n"
                    + "      trigger: 'item',\n"
                    + "      formatter: function(p) {\n"
                    + "        return p.name + ': ' + p.value + ' (' + p.percent + '%)';\n"
                    + "      }\n"
                    + "    }\n"
                    + "  },\n"
                    + "  grid: {\n"
                    + "    left: 60,\n"
                    + "    right: 180,\n"
                    + "    top: 60,\n"
                    + "    bottom: 60\n"
                    + "  },\n"
                    + "  tooltip: {\n"
                    + "    trigger: 'item',\n"
                    + "    confine: true,\n"
                    + "    formatter: function(p) {\n"
                    + "      return p.name + ': ' + p.value + ' (' + p.percent + '%)';\n"
                    + "    }\n"
                    + "  }\n"
                    + "}\n"
                    + "```\n"
                    + "**配置说明：**\n"
                    + "- `center: ['40%', '50%']` — 饼图水平位置左移至40%，让出右侧空间给图例\n"
                    + "- `legend.right: 20` — 图例靠右显示，距离右侧20px\n"
                    + "- `legend.left: 'auto'` — 确保 `right` 定位生效，不干扰右侧对齐\n"
                    + "- `grid.right: 180` — 为右侧图例预留足够空间，防止图例被裁剪或遮挡\n"
                    + "- `type: 'scroll'` — 图例项过多时启用滚动，避免溢出\n\n"

                    + "### 7. 数据Zoom（X轴类别>10个）\n"
                    + "必须添加：\n"
                    + "dataZoom: [{ type: 'slider' }, { type: 'inside' }]\n\n"

                    + "### 8. tooltip防溢出\n"
                    + "所有图表必须设置：\n"
                    + "tooltip: { trigger: 'axis', confine: true }\n"
                    + "饼图例外使用 trigger: 'item'\n\n"

                    + "### 9. 标题与图例位置联动\n"
                    + "当标题在顶部居中时，统一使用以下配置：\n"
                    + "title: { text: '${动态提取的标题}', top: 10, left: 'center' }\n"
                    + "legend: { top: 55, left: 'center' }\n"
                    + "grid: { top: 90 }\n\n"

                    + "### 10. Y轴数据差距极大时\n"
                    + "使用log轴：\n"
                    + "yAxis: { type: 'log', name: '${动态提取的单位}' }\n\n"

                    + "### 11. 占比与人数双指标展示\n"
                    + "当查询结果中同时包含'人数'和'占比'字段，且值在各行不同时：\n"
                    + "使用双Y轴组合图（柱状图+折线图）：\n"
                    + "yAxis: [\n"
                    + "  { type: 'value', name: '人数', axisLabel: { formatter: '{value}' } },\n"
                    + "  { type: 'value', name: '占比（%）', min: 0, max: 100, axisLabel: { formatter: '{value}%' } }\n"
                    + "]\n"
                    + "series: [\n"
                    + "  { name: '人数', type: 'bar', yAxisIndex: 0, data: [/* 从结果提取，每行不同 */] },\n"
                    + "  { name: '占比', type: 'line', yAxisIndex: 1, data: [/* 从结果提取，每行不同 */] }\n"
                    + "]\n\n"

                    + "### 12. 双指标对比（结果中仅有2个数值型字段且值在各行不同时）\n"
                    + "必须使用垂直柱状图，禁止使用横向条形图，禁止X轴旋转，禁止出现空组。\n"
                    + "### 动态生成规则（所有数据从查询结果中提取，禁止硬编码）：\n"
                    + "{\n"
                    + "  title: { text: '${从用户问题或数据中提取的标题}', left: 'center', top: 10 },\n"
                    + "  tooltip: { trigger: 'axis', confine: true },\n"
                    + "  legend: { data: ['${指标1名称}', '${指标2名称}'], top: 55, left: 'center' },\n"
                    + "  xAxis: {\n"
                    + "    type: 'category',\n"
                    + "    data: ['${分类名称}'],  // 仅1个元素\n"
                    + "    axisLabel: { show: true, rotate: 0, interval: 0 }\n"
                    + "  },\n"
                    + "  yAxis: { type: 'value', name: '${单位}', axisLabel: { formatter: '{value}' } },\n"
                    + "  series: [\n"
                    + "    { name: '${指标1名称}', type: 'bar', data: [${指标1数值}], itemStyle: { color: '#5470C6' },\n"
                    + "      label: { show: true, position: 'top', formatter: '{c}${单位}' } },\n"
                    + "    { name: '${指标2名称}', type: 'bar', data: [${指标2数值}], itemStyle: { color: '#91CC75' },\n"
                    + "      label: { show: true, position: 'top', formatter: '{c}${单位}' } }\n"
                    + "  ],\n"
                    + "  barGap: '10%',\n"
                    + "  barCategoryGap: '30%',\n"
                    + "  grid: { left: 60, right: 60, top: 90, bottom: 60 }\n"
                    + "}\n"
                    + "X轴data数组只能有1个元素，不允许多个分类，不允许有空字符串，不允许设置dataZoom\n\n"

                    + "### 13. 查询结果有多条分类数据时，优先展示人数和占比\n"
                    + "如果查询结果中包含'人数'（或count）和'占比'（或percent）字段，且值在各行不同：\n"
                    + "- 使用规则11的双Y轴组合图展示\n"
                    + "- 若outType指定为'pie'，则使用饼图展示占比\n"
                    + "- 若outType指定为'bar'，则使用柱状图展示人数\n\n"

                    + "### 14. 默认兜底行为\n"
                    + "当以上规则均不满足时，默认使用柱状图：\n"
                    + "- X轴：第一个文本字段（值在各行不同）作为分类\n"
                    + "- Y轴：第一个数值字段（值在各行不同）作为数据\n"
                    + "- 标题：从用户问题中提取关键词\n\n"

                    + "## 输出要求\n"
                    + "1. 只输出有效的JSON字符串，不要包含任何解释性文字\n"
                    + "2. JSON必须符合ECharts option规范\n"
                    + "3. **所有数据必须从查询结果中动态提取，禁止硬编码具体数值**\n"
                    + "4. **绝对禁止将值全部相同的字段（汇总指标）作为图表数据**\n"
                    + "5. 汇总指标只能出现在标题或tooltip中\n"
                    + "6. 如果outType指定了图表类型，优先使用该类型（'pie'、'bar'、'line'）\n"
                    + "7. 生成的图表配置必须完整，包含title、tooltip、legend、xAxis、yAxis、series、grid等必要字段\n";


            String[][] chartMessages = {
                    {"system", chartPrompt},
                    {"user", message}
            };

            String chartJson = qwenService.nonStreamChat(chartMessages);

            if (StringUtils.isBlank(chartJson)) {
                log.info("【第四步-图表】无需生成图表");
                return null;
            }

            chartJson = chartJson.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();

            if (!chartJson.startsWith("{")) {
                log.warn("【第四步-图表】图表配置不是合法JSON，忽略: {}", chartJson);
                return null;
            }

            objectMapper.readTree(chartJson);
            chartJson = objectMapper.writeValueAsString(objectMapper.readTree(chartJson));
            log.info("【第四步-图表】图表配置JSON合法");
            return chartJson;

        } catch (Exception e) {
            log.warn("【第四步-图表】生成图表配置失败: {}", e.getMessage());
            return null;
        }
    }

    private String generateElementUITableConfig(List<Map<String, Object>> queryResult) {
        // ... 保持原有实现不变 ...
        try {
            if (queryResult == null || queryResult.isEmpty()) {
                log.info("【第五步】查询结果为空，无需生成表格");
                return null;
            }

            Map<String, Object> firstRow = queryResult.get(0);
            List<String> fieldNames = new ArrayList<>(firstRow.keySet());

            Map<String, String> fieldNameTranslationMap = translateFieldNames(fieldNames);

            List<Map<String, Object>> columns = new ArrayList<>();
            for (String key : fieldNames) {
                Map<String, Object> column = new LinkedHashMap<>();
                column.put("prop", key);
                String label = (fieldNameTranslationMap != null && fieldNameTranslationMap.containsKey(key))
                        ? fieldNameTranslationMap.get(key)
                        : key;
                column.put("label", label);
                columns.add(column);
            }

            Map<String, Object> tableConfig = new LinkedHashMap<>();
            tableConfig.put("columns", columns);
            tableConfig.put("data", queryResult);

            String tableJson = objectMapper.writeValueAsString(tableConfig);
            log.info("【第五步】ElementUI Table配置生成成功，共 {} 列，{} 行数据", columns.size(), queryResult.size());
            return tableJson;

        } catch (Exception e) {
            log.warn("【第五步】生成ElementUI Table配置失败: {}", e.getMessage());
            return null;
        }
    }

    private String generateElementUITableConfigForManyResult(List<List<Map<String, Object>>> allResult) {
        try {
            // 如果只有1个结果集，直接生成
            if (allResult.size() == 1) {
                List<String> resultjson = new ArrayList<>();
                resultjson.add(generateSingleTableConfig(allResult.get(0)));
                return objectMapper.writeValueAsString(resultjson);
            }

            // 多个结果集：使用并行流加速
            List<String> allTableJson = allResult.parallelStream()
                    .map(this::generateSingleTableConfig)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return objectMapper.writeValueAsString(allTableJson);
        } catch (Exception e) {
            log.error("生成表格配置失败", e);
            return null;
        }
    }

    private String generateSingleTableConfig(List<Map<String, Object>> queryResult){
        if (queryResult == null || queryResult.isEmpty()) {
            return null;
        }

        Map<String, Object> firstRow = queryResult.get(0);
        List<String> fieldNames = new ArrayList<>(firstRow.keySet());

        // 批量翻译字段名（减少循环次数）
        Map<String, String> translationMap = translateFieldNames(fieldNames);

        List<Map<String, Object>> columns = fieldNames.stream()
                .map(key -> {
                    Map<String, Object> column = new LinkedHashMap<>();
                    column.put("prop", key);
                    column.put("label", translationMap.getOrDefault(key, key));
                    return column;
                })
                .collect(Collectors.toList());

        Map<String, Object> tableConfig = new LinkedHashMap<>();
        tableConfig.put("columns", columns);
        tableConfig.put("data", queryResult);

        try{
            return objectMapper.writeValueAsString(tableConfig);
        }catch (Exception e){
            log.error("解析成json数据异常！");
            return null;
        }
    }

    private Map<String, String> translateFieldNames(List<String> fieldNames) {
        // ... 保持原有实现不变 ...
        try {
            String systemPrompt = "你是一个数据领域专家。你的任务是将数据库的英文字段名翻译成准确、简洁的中文描述。\n"
                    + "请直接返回一个JSON对象，key是英文字段名，value是对应的中文描述。\n"
                    + "例如：输入 [\"user_name\", \"order_amount\"]，输出 {\"user_name\": \"用户姓名\", \"order_amount\": \"订单金额\"}";

            String userPrompt = "请翻译以下字段名：" + String.join(", ", fieldNames);

            String[][] messages = {
                    {"system", systemPrompt},
                    {"user", userPrompt}
            };

            String translationResult = qwenService.nonStreamChat(messages);

            if (StringUtils.isNotBlank(translationResult)) {
                translationResult = translationResult.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
                return objectMapper.readValue(translationResult,
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            }
        } catch (Exception e) {
            log.warn("【第五步-翻译】调用智能体翻译字段名失败: {}", e.getMessage());
        }
        return null;
    }


    public String saveQuestion(UserQAHistory history){
        Map<String, String> result = new HashMap<>();
        int m = Integer.parseInt(history.getLuserid()) % 100;
        String tableName = "user_history" + String.valueOf(m);
        try {
            int resultCount = userQAHistoryService.insertData(history, tableName);
            if (resultCount > 0){
                log.info("用户问题记录成功！");
                result.put("code", "200");
                result.put("msg", "用户问题记录成功");
                return objectMapper.writeValueAsString(result);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("保存用户问题历史出错{}", e.getMessage());
            result.put("code", "500");
            result.put("msg", "保存用户问题历史出错!");
            try {
                return objectMapper.writeValueAsString(result);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }
        return "{\"code\": \"500\", \"msg\": \"保存用户问题历史出错\"}";
    }

    @Override
    public PageResult<UserQAHistory> getQuestion(PageQuestion question) throws Exception {
        try {
            if (!validatePageQuestion(question)){
                log.error("获取问题历史记录参数异常！");
                throw new Exception("获取问题历史记录参数异常");
            }
            Integer offset = (question.getPageNum() - 1) * question.getPageSize();
            int m = Integer.parseInt(question.getLuserid()) % 100;
            String tableName = "user_history" + m;
            List<UserQAHistory> questions = userQAHistoryMapper.selectPageQuestion(question.getLuserid(),question.getPageSize(), offset, tableName);
            Long total = userQAHistoryMapper.selectQuestionCount(question.getLuserid(),tableName);
            return  new PageResult<>(questions, total, question.getPageNum(), question.getPageSize());

        }catch (Exception e){
            log.error("获取用户历史数据异常！{}", e.getMessage());
            throw new Exception("获取用户历史数据异常");
        }
    }

    private boolean validatePageQuestion(PageQuestion question){
        boolean status = true;
        if (question.getPageNum() == null || question.getPageSize() == null || question.getLuserid() == null){
            status = false;
        }
        return status;
    }

    public String deleteQuestionById(int id, String luserId){
        Map<String,String> result = new HashMap<>();
        try {
            if (id == 0){
                result.put("code", "500");
                result.put("msg", "id不能为空！");
            }
            String tableName = getTableName(luserId);
            int count = userQAHistoryMapper.deleteQuestion(id, tableName);
            if (count == 0){
                result.put("code", "200");
                result.put("msg", "未找到问题数据！");
            }
            if (count > 0){
                result.put("code", "200");
                result.put("msg", "问题已删除！");
            }
            return objectMapper.writeValueAsString(result);
        }catch (Exception e){
            log.error("删除用户问题出错，{}", e.getMessage());
            return "{\"code\": \"500\", \"msg\": \"删除用户问题出错\"}";
        }
    }

    @Override
    public String topQuestion(int id, String luserId) {
        Map<String,String> result = new HashMap<>();
        try {
            if (id == 0){
                result.put("code", "500");
                result.put("msg", "id不能为空！");
            }
            String tableName = getTableName(luserId);
            int count = userQAHistoryMapper.topQuestion(id, tableName);
            if (count == 0){
                result.put("code", "200");
                result.put("msg", "未找到问题数据！");
            }
            if (count > 0){
                result.put("code", "200");
                result.put("msg", "问题以置顶！");
            }
            return objectMapper.writeValueAsString(result);
        }catch (Exception e){
            log.error("置顶用户问题出错，{}", e.getMessage());
            return "{\"code\": \"500\", \"msg\": \"置顶用户问题出错\"}";
        }
    }

    @Override
    public String cancelTop(int id, String luserId) {
        Map<String,String> result = new HashMap<>();
        try {
            if (id == 0){
                result.put("code", "500");
                result.put("msg", "id不能为空！");
            }
            String tableName = getTableName(luserId);
            int count = userQAHistoryMapper.cancelTop(id, tableName);
            if (count == 0){
                result.put("code", "200");
                result.put("msg", "未找到问题数据！");
            }
            if (count > 0){
                result.put("code", "200");
                result.put("msg", "问题以置顶！");
            }
            return objectMapper.writeValueAsString(result);
        }catch (Exception e){
            log.error("置顶用户问题出错，{}", e.getMessage());
            return "{\"code\": \"500\", \"msg\": \"置顶用户问题出错\"}";
        }
    }

    private String getTableName(String luserId){
        int m = Integer.parseInt(luserId) % 100;
        String tableName = "user_history" + m;
        return tableName;
    }

    private Map<String, String> getDataBaseAndTable(Integer lRespondentId){
        Map<String, String> result = new HashMap<>();
        String databaseName ="research" +  String.valueOf((lRespondentId / 100) % 5);
        String tableName = "tbStudent" + String.valueOf(lRespondentId % 100);
        result.put("databaseName", databaseName);
        result.put("tableName", tableName);
        return result;
    }


    /**
     * 清洗 desc，移除数据库列名，只保留人类可读的指标描述
     */
    public static String cleanDesc(String desc) {
        if (desc == null || desc.isEmpty()) return desc;

        String cleaned = desc;

        // 去除 "（列名=值）" 整段
        cleaned = cleaned.replaceAll("（[^）]*=[^）]*）", "");
        // 去除条件前缀整段（"，条件：" 或 "条件：..."）
        cleaned = cleaned.replaceAll("，条件[：:][^{]*", "");
        cleaned = cleaned.replaceAll("条件[：:][^{]*", "");
        // 去除残留的常见列名前缀（str/n/dbl/f/t 等开头）
        cleaned = cleaned.replaceAll("str\\w+", "");
        cleaned = cleaned.replaceAll("n\\w+", "");
        cleaned = cleaned.replaceAll("dbl\\w+", "");
        cleaned = cleaned.replaceAll("\\w+Id$", "");
        // 去除空括号和多余符号
        cleaned = cleaned.replaceAll("（[）]*）", "");
        cleaned = cleaned.replaceAll("[，,]+", "，");
        cleaned = cleaned.trim();

        // 兜底：清洗后太短则还原原值
        if (cleaned.isEmpty() || cleaned.length() < 4) {
            return desc;
        }

        return cleaned;
    }
    /**
     * 从 resultJson 中自动提取满意度/匹配度选项列名，生成正确的计算公式
     * 自动适配"很满意/满意"和"很符合/比较符合"等多种命名方式
     */
    public static String buildSatisfactionFormula(String resultJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode firstRow = mapper.readTree(resultJson).get(0);
            Iterator<Map.Entry<String, JsonNode>> fields = firstRow.fields();

            // 排除的非选项列
            Set<String> excludeKeys = Set.of(
                    "指标", "满意度", "均值", "样本量", "占比",
                    "ratio", "count", "total", "人数",
                    "选项一", "选项二", "选项三", "选项四", "选项五",
                    "作答总人数", "总人数", "选项作答人数"
            );

            // 排除的数值类列（value 为纯数字的列，不是百分比选项列）
            Set<String> numericKeys = Set.of(
                    "满意度", "均值", "样本量", "占比", "ratio", "count", "total"
            );

            List<String> optionKeys = new ArrayList<>();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                String val = field.getValue().asText("");

                // 排除已知的非选项列
                if (excludeKeys.contains(key)) continue;

                // 排除满意度/均值/样本量等数值列（值为纯数字或数值百分比）
                if (numericKeys.contains(key)) continue;

                // 如果 key 是"指标"列，排除
                if ("指标".equals(key)) continue;

                // 保留以"很符合/满意"等为 key，value 为百分比的列
                // 这些列的 value 格式为 "XX.XX%"
                optionKeys.add(key);
            }

            // 按语义排序（很X → 比较X → 基本X → 不太X → 很不X）
            final Map<String, Integer> orderMap = Map.ofEntries(
                    entry("很符合", 5), entry("比较符合", 4), entry("基本符合", 3),
                    entry("比较不符合", 2), entry("很不符合", 1),
                    entry("很满意", 5), entry("满意", 4), entry("基本满意", 3),
                    entry("不太满意", 2), entry("很不满意", 1),
                    entry("非常满意", 5), entry("比较满意", 4), entry("一般满意", 3),
                    entry("不太满意2", 2), entry("不满意", 1)
            );

            optionKeys.sort((a, b) -> {
                int scoreA = orderMap.getOrDefault(a, 0);
                int scoreB = orderMap.getOrDefault(b, 0);
                // 没有匹配到语义关键词的，按字符串长度排（短的优先）
                if (scoreA == 0 && scoreB == 0) {
                    return Integer.compare(a.length(), b.length());
                }
                return Integer.compare(scoreB, scoreA);
            });

            optionKeys = optionKeys.stream()
                    .filter(key -> orderMap.getOrDefault(key, 0) >= 3)
                    .collect(Collectors.toList());

            // 生成公式
            StringBuilder formula = new StringBuilder();
            for (int i = 0; i < optionKeys.size(); i++) {
                if(i == 0) formula.append("(");
                if (i > 0) formula.append(" + ");
                formula.append(optionKeys.get(i)).append("×").append(optionKeys.get(i)).append("人数");
            }
            formula.append(")");
            formula.append(" ÷ 作答总人数");

            return formula.toString();

        } catch (Exception e) {
            return "选项×选项人数 + ... ÷ 作答总人数";
        }
    }
}