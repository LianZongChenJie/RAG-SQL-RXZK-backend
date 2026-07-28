package com.wnsse.sqlRag.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;

@Slf4j
public class SseEmitterUtil {

    /**
     * 创建SSE连接，设置超时时间300秒
     */
    public static SseEmitter getSseEmitter() {
        return new SseEmitter(300_000L);
    }

    /**
     * 检查emitter是否可用
     */
    public static boolean isEmitterActive(SseEmitter emitter) {
        if (emitter == null) {
            return false;
        }
        try {
            // 通过ping检测连接状态
            emitter.send(SseEmitter.event().name("ping").data(""));
            return true;
        } catch (Exception e) {
            log.debug("Emitter不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 安全发送SSE事件 - 不抛出异常
     * @return true表示发送成功，false表示连接已关闭
     */
    public static boolean sendEventSafely(SseEmitter emitter, String name, String data) {
        if (emitter == null) {
            log.warn("SSE Emitter为null，跳过发送");
            return false;
        }

        try {
            emitter.send(SseEmitter.event().name(name).data(data));
            return true;
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("already completed")) {
                log.debug("SSE连接已关闭，跳过发送 - name: {}", name);
            } else {
                log.warn("SSE发送异常 - name: {}, 异常: {}", name, e.getMessage());
            }
            return false;
        } catch (IOException e) {
            log.warn("SSE发送IO异常 - name: {}, 异常: {}", name, e.getMessage());
            return false;
        }
    }

    /**
     * 发送事件并检查是否继续（用于关键步骤）
     * @return true表示发送成功且连接存活，false表示连接已关闭
     */
    public static boolean sendEventAndCheck(SseEmitter emitter, String name, String data) {
        boolean sent = sendEventSafely(emitter, name, data);
        if (!sent) {
            log.warn("发送事件失败，连接可能已关闭，建议停止后续处理 - name: {}", name);
        }
        return sent;
    }

    /**
     * 发送错误事件并完成SSE连接 - 安全版本
     */
    public static void sendErrorEventSafely(SseEmitter emitter, String errorMessage) {
        if (emitter == null) {
            log.warn("Emitter为null，无法发送错误事件");
            return;
        }

        // 尝试发送错误事件
        boolean sent = sendEventSafely(emitter, "error", errorMessage);

        // 无论是否发送成功，都尝试完成连接
        completeSafely(emitter);
    }

    /**
     * 安全完成SSE连接
     */
    public static void completeSafely(SseEmitter emitter) {
        if (emitter == null) {
            return;
        }
        try {
            emitter.complete();
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("already completed")) {
                log.debug("SSE连接已关闭，无需重复完成");
            } else {
                log.warn("完成SSE连接异常: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.warn("完成SSE连接失败: {}", e.getMessage());
        }
    }

    /**
     * 安全关闭SSE连接（带错误原因）
     */
    public static void completeWithErrorSafely(SseEmitter emitter, String errorMessage) {
        if (emitter == null) {
            return;
        }
        sendErrorEventSafely(emitter, errorMessage);
        // sendErrorEventSafely中已调用completeSafely
    }
}