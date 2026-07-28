package com.wnsse.sqlRag.service;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.wnsse.sqlRag.config.QwenProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class QwenService {

    private final QwenProperties qwenProperties;

    // ==================== 流式对话 ====================

    /**
     * 流式对话（兼容原接口：单条用户消息）
     */
    public void streamChat(String message, SseEmitter emitter) {
        streamChat(new String[][]{{"user", message}}, emitter, null, null);
    }

    /**
     * 流式对话（支持多轮消息，如 system + user）
     * messages: 二维数组，每行 = [role, content]
     */
    public void streamChat(String[][] messages, SseEmitter emitter) {
        streamChat(messages, emitter, null, null);
    }

    /**
     * 流式对话，支持在流结束时发送后置事件（如chart）后再完成
     *
     * @param messages    对话消息
     * @param emitter     SSE发射器
     * @param postEventName  流结束后发送的事件名称（如 "chart"），null则不发送
     * @param postEventData  流结束后发送的事件数据
     */
    public void streamChat(String[][] messages, SseEmitter emitter,
                           String postEventName, String postEventData) {
        try {
            String url = buildUrl();
            String requestBody = buildRequestBody(messages, true);
            doStreamRequest(url, requestBody, emitter, postEventName, postEventData);
        } catch (Exception e) {
            log.error("创建千问流式请求失败", e);
            sendErrorAndComplete(emitter, "AI服务异常: " + e.getMessage());
        }
    }

    public void streamChatForSummery(String[][] messages, SseEmitter emitter,
                           String postEventName, String postEventData, String desc) {
        try {
            String url = buildUrl();
            String requestBody = buildRequestBody(messages, true);
            doStreamRequestForSummery(url, requestBody, emitter, postEventName, postEventData, desc);
        } catch (Exception e) {
            log.error("创建千问流式请求失败", e);
            sendErrorAndComplete(emitter, "AI服务异常: " + e.getMessage());
        }
    }

    // ==================== 非流式对话 ====================

    /**
     * 非流式对话，返回完整响应内容
     * messages: 二维数组，每行 = [role, content]
     */
    public String nonStreamChat(String[][] messages) {
        String url = buildUrl();
        String requestBody = buildRequestBody(messages, false);

        log.debug("千问非流式请求: {}", requestBody);

        String response = WebClient.create()
                .post()
                .uri(url)
                .header("Authorization", "Bearer " + qwenProperties.getKey())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(10))
                        .filter(throwable -> throwable instanceof WebClientRequestException
                                || throwable instanceof ConnectException
                                || throwable instanceof SocketTimeoutException)
                        .doBeforeRetry(retrySignal -> {
                            // 修正：使用 totalRetries() 获取重试次数
                            log.warn("调用千问API失败，正在进行第{}次重试",
                                    retrySignal.totalRetries() + 1);
                        })
                )
                .block();

        log.debug("千问非流式响应: {}", response);

        JSONObject json = JSONObject.parseObject(response);
        String content = (String) JSONPath.eval(json, "$.output.choices[0].message.content");
        return content;
    }

    // ==================== 内部方法 ====================

    private String buildUrl() {
        return "https://" + qwenProperties.getHost() + "/api/v1/services/aigc/text-generation/generation";
    }

    /**
     * 构建千问API请求体
     */
    private String buildRequestBody(String[][] messages, boolean stream) {
        StringBuilder messagesJson = new StringBuilder();
        for (int i = 0; i < messages.length; i++) {
            if (i > 0) {
                messagesJson.append(",");
            }
            String role = messages[i][0];
            String content = escapeJson(messages[i][1]);
            messagesJson.append("{\"role\":\"")
                    .append(role)
                    .append("\",\"content\":\"")
                    .append(content)
                    .append("\"}");
        }
        return "{"
                + "\"model\":\"qwen-plus\","
                + "\"input\":{\"messages\":[" + messagesJson + "]},"
                + "\"parameters\":{"
                + "\"result_format\":\"message\","
                + "\"incremental_output\":" + stream + ","
                + "\"stream\":" + stream
                + "}"
                + "}";
    }

    private void doStreamRequest(String url, String requestBody, SseEmitter emitter,
                                 String postEventName, String postEventData) {
        WebClient.create()
                .post()
                .uri(url)
                .header("Authorization", "Bearer " + qwenProperties.getKey())
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                // 添加重试机制
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(10))
                        .filter(throwable -> {
                            // 只重试网络相关异常
                            return throwable instanceof WebClientRequestException
                                    || throwable instanceof ConnectException
                                    || throwable instanceof SocketTimeoutException
                                    || throwable instanceof IOException;
                        })
                        .doBeforeRetry(retrySignal -> {
                            long retryCount = retrySignal.totalRetries() + 1;
                            log.warn("千问流式请求失败，正在进行第{}次重试，错误: {}",
                                    retryCount,
                                    retrySignal.failure().getMessage());
                            // 重试前通知前端
                            sendEvent(emitter, "retry", String.format("正在重试第%d次...", retryCount));
                        })
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("千问流式请求重试{}次后仍然失败", retrySignal.totalRetries());
                            return retrySignal.failure();
                        })
                )
                .subscribe(
                        line -> handleStreamLine(line, emitter),
                        error -> {
                            log.error("千问流式请求失败", error);
                            sendErrorAndComplete(emitter, "AI服务异常: " + error.getMessage());
                        },
                        () -> {
                            log.debug("千问流式响应完成");
                            // 后置事件（如chart）在流文本完成后发送
                            if (postEventName != null && postEventData != null) {
                                sendEvent(emitter, postEventName, postEventData);
                                log.info("后置事件推送完成 - name: {}, data: {}", postEventName, postEventData);
                            }
                            sendEvent(emitter, "done", "[DONE]");
                            emitter.complete();
                        }
                );
    }
    private void doStreamRequestForSummery(String url, String requestBody, SseEmitter emitter,
                                 String postEventName, String postEventData, String desc) {
        WebClient.create()
                .post()
                .uri(url)
                .header("Authorization", "Bearer " + qwenProperties.getKey())
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                // 添加重试机制
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(10))
                        .filter(throwable -> {
                            // 只重试网络相关异常
                            return throwable instanceof WebClientRequestException
                                    || throwable instanceof ConnectException
                                    || throwable instanceof SocketTimeoutException
                                    || throwable instanceof IOException;
                        })
                        .doBeforeRetry(retrySignal -> {
                            long retryCount = retrySignal.totalRetries() + 1;
                            log.warn("千问流式请求失败，正在进行第{}次重试，错误: {}",
                                    retryCount,
                                    retrySignal.failure().getMessage());
                            // 重试前通知前端
                            sendEvent(emitter, "retry", String.format("正在重试第%d次...", retryCount));
                        })
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("千问流式请求重试{}次后仍然失败", retrySignal.totalRetries());
                            return retrySignal.failure();
                        })
                )
                .subscribe(
                        line -> handleStreamLine(line, emitter),
                        error -> {
                            log.error("千问流式请求失败", error);
                            sendErrorAndComplete(emitter, "AI服务异常: " + error.getMessage());
                        },
                        () -> {
                            log.debug("千问流式响应完成");
                            // 后置事件（如chart）在流文本完成后发送
                            if (postEventName != null && postEventData != null) {
                                sendEvent(emitter, postEventName, postEventData);
                                log.info("后置事件推送完成 - name: {}, data: {}", postEventName, postEventData);
                            }
                            sendEvent(emitter, "message", desc);
                            sendEvent(emitter, "done", "[DONE]");
                            emitter.complete();
                        }
                );
    }

    private void handleStreamLine(String line, SseEmitter emitter) {
        try {
            if (line == null || line.isEmpty()) {
                return;
            }

            log.debug("千问SSE行: {}", line);

            String jsonStr = line.trim();

            if ("[DONE]".equals(jsonStr)) {
                log.debug("千问流式响应完成");
                sendEvent(emitter, "done", "[DONE]");
                return;
            }

            if (!jsonStr.startsWith("{")) {
                return;
            }

            try {
                JSONObject json = JSONObject.parseObject(jsonStr);
                String content = (String) JSONPath.eval(json,
                        "$.output.choices[0].message.content");

                log.debug("千问解析结果 - content: {}", content);

                if (content != null && !content.isEmpty()) {
                    sendEvent(emitter, "message", content);
                }
            } catch (Exception e) {
                log.warn("解析千问响应JSON失败: {}", jsonStr, e);
                sendEvent(emitter, "message", jsonStr);
            }
        } catch (Exception e) {
            log.error("SSE发送失败", e);
            emitter.completeWithError(e);
        }
    }

    private void sendEvent(SseEmitter emitter, String name, String data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException e) {
            log.error("SSE发送事件失败 - name: {}, data: {}", name, data, e);
            emitter.completeWithError(e);
        }
    }

    private void sendErrorAndComplete(SseEmitter emitter, String message) {
        sendEvent(emitter, "error", message);
        emitter.complete();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
