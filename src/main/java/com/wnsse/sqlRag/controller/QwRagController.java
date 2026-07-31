package com.wnsse.sqlRag.controller;

import com.wnsse.sqlRag.common.Result;
import com.wnsse.sqlRag.constant.SqlProsConstant;
import com.wnsse.sqlRag.entity.PageQuestion;
import com.wnsse.sqlRag.entity.QwRagStreamRequest;
import com.wnsse.sqlRag.entity.UserQAHistory;
import com.wnsse.sqlRag.service.QwRagService;
import com.wnsse.sqlRag.util.SseEmitterUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/qwRag")
@RequiredArgsConstructor
public class QwRagController {

    private final QwRagService qwRagService;

    /**
     * SSE流式RAG接口 - 5步处理流程
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody QwRagStreamRequest request) {
        log.info("收到RAG流式请求 - {}", request.toString());

        // 参数校验
        String validationError = qwRagService.validateRequest(request);
        if (validationError != null) {
            log.warn("参数校验失败: {}", validationError);
            return errorEmitter(validationError);
        }

        // 创建SSE连接
        SseEmitter emitter = SseEmitterUtil.getSseEmitter();

        // 设置生命周期监听
        emitter.onCompletion(() -> {
            log.debug("SSE连接正常完成");
        });

        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            // 超时时尝试发送超时提示
            try {
                emitter.send(SseEmitter.event().name("timeout").data("处理超时，请稍后重试"));
            } catch (IOException e) {
                log.debug("发送超时消息失败，连接已关闭");
            }
            SseEmitterUtil.completeSafely(emitter);
        });

        emitter.onError(throwable -> {
            log.error("SSE连接异常", throwable);
            // 注意：onError中不能再调用emitter.send，因为连接已经异常
            // 如果需要通知前端，在业务层处理
        });

        // 异步执行处理流程
        qwRagService.processStream(request, emitter);

        return emitter;
    }


    @PostMapping("/getObjectSQLAndData")
    public String getObjectSQlAndData(@RequestBody QwRagStreamRequest request) {
        request.setDbType(SqlProsConstant.OBJECTIVE_DB_TYPE);
        return qwRagService.getObjectSQlAndData(request);
    }

    /**
     * 构造一个直接返回错误的SSE响应（同步返回，仅在参数校验失败时使用）
     */
    private SseEmitter errorEmitter(String errorMessage) {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event().name("error").data(errorMessage));
            emitter.complete();
        } catch (Exception e) {
            log.error("发送错误消息失败", e);
            emitter.complete();
        }
        return emitter;
    }

    @PostMapping("/saveQuestion")
    public Result<?> saveQuestion(@RequestBody UserQAHistory history, WebRequest request) throws Exception {

        String strCookie = request.getHeader("Cookie");
        if (strCookie != null) {
            String[] cookies = strCookie.split(";");
            for(String strC : cookies){
                if(strC.contains("luserid")){
                    String[] luserIdArray = strC.split("=");
                    if (luserIdArray.length > 0){
                        history.setLuserid(luserIdArray[1]);
                    }
                }
            }
        }
        if(StringUtils.isBlank(history.getLuserid()) || StringUtils.isEmpty(history.getLuserid())){
            return Result.error("获取luserId不能为空！");
        }
        if (!StringUtils.isBlank(history.getQuestionDesc()) && !StringUtils.isEmpty(history.getQuestionDesc())){
            history.setQuestion(history.getQuestion() + "-" + history.getQuestionDesc());
        }
        try {
            qwRagService.saveQuestion(history);
            return Result.success("保存成功！");
        }catch (Exception e){
            return Result.error("保存失败！");
        }
    }

    @PostMapping("/getPageQuestion")
    public Result<?> getPageQuestion(@RequestBody PageQuestion question, WebRequest request) throws Exception {
        String strCookie = request.getHeader("Cookie");
        if (strCookie != null) {
            String[] cookies = strCookie.split(";");
            for(String strC : cookies){
                if(strC.contains("luserid")){
                    String[] luserIdArray = strC.split("=");
                    if (luserIdArray.length > 0){
                        question.setLuserid(luserIdArray[1]);
                    }
                }
            }
        }
        if(StringUtils.isBlank(question.getLuserid()) || StringUtils.isEmpty(question.getLuserid())){
            return Result.error("获取luserId不能为空！");
        }
        return Result.success("查询完成",qwRagService.getQuestion(question));
    }

    @DeleteMapping("/deleteQeustionById/{id}")
    public Result<?> deleteQuestion(@PathVariable int id,  WebRequest request){
        String strCookie = request.getHeader("Cookie");
        String luserId = "";
        if (strCookie != null) {
            String[] cookies = strCookie.split(";");
            for(String strC : cookies){
                if(strC.contains("luserid")){
                    String[] luserIdArray = strC.split("=");
                    if (luserIdArray.length > 0){
                        luserId = luserIdArray[1];
                    }
                }
            }
        }
        if(StringUtils.isBlank(luserId) || StringUtils.isEmpty(luserId)){
            return Result.error("获取luserId不能为空！");
        }
        try {
            qwRagService.deleteQuestionById(id, luserId);
            return Result.success("删除成功！");
        }catch (Exception e){
            return Result.error("删除失败！");
        }
    }

    @PutMapping("/topQuestion/{id}")
    public Result<?> topQuestion(@PathVariable int id,  WebRequest request){
        String strCookie = request.getHeader("Cookie");
        String luserId = "";
        if (strCookie != null) {
            String[] cookies = strCookie.split(";");
            for(String strC : cookies){
                if(strC.contains("luserid")){
                    String[] luserIdArray = strC.split("=");
                    if (luserIdArray.length > 0){
                        luserId = luserIdArray[1];
                    }
                }
            }
        }
        if(StringUtils.isBlank(luserId) || StringUtils.isEmpty(luserId)){
            return Result.error("获取luserId不能为空！");
        }
        try {
            qwRagService.topQuestion(id, luserId);
            return Result.success("置顶成功！");
        }catch (Exception e){
            return Result.error("指定失败！");
        }
    }

    @PutMapping("/cancelTop/{id}")
    public Result<?> cancelTop(@PathVariable int id,  WebRequest request){
        String strCookie = request.getHeader("Cookie");
        String luserId = "";
        if (strCookie != null) {
            String[] cookies = strCookie.split(";");
            for(String strC : cookies){
                if(strC.contains("luserid")){
                    String[] luserIdArray = strC.split("=");
                    if (luserIdArray.length > 0){
                        luserId = luserIdArray[1];
                    }
                }
            }
        }
        if(StringUtils.isBlank(luserId) || StringUtils.isEmpty(luserId)){
            return Result.error("获取luserId不能为空！");
        }
        try {
            qwRagService.cancelTop(id, luserId);
            return Result.success("取消置顶成功！");
        }catch (Exception e){
            return Result.error("取消置顶失败！");
        }
    }


    @PostMapping("/getSurveySQLAndData")
    public String getSurVeySQlAndData(@RequestBody QwRagStreamRequest request) {
        request.setDbType(SqlProsConstant.OBJECTIVE_DB_TYPE);
        return qwRagService.getSurveySQLAndData(request);
    }

}