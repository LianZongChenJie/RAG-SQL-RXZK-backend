package com.wnsse.sqlRag.service;

import com.wnsse.sqlRag.common.PageResult;
import com.wnsse.sqlRag.entity.PageQuestion;
import com.wnsse.sqlRag.entity.QwRagStreamRequest;
import com.wnsse.sqlRag.entity.UserQAHistory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Service
public interface QwRagService {


    /**
     * 校验请求参数
     * @param request 请求对象
     * @return 校验失败返回错误信息，成功返回null
     */
    String validateRequest(QwRagStreamRequest request);

    /**
     * 处理流式RAG请求
     * @param request 请求对象
     * @param emitter SSE发射器
     */
    void processStream(QwRagStreamRequest request, SseEmitter emitter);


    /**
     * 获取客观SQL和执行数据
     * @param request
     * @return
     */
    String getObjectSQlAndData(QwRagStreamRequest request);


    /**
     * 获取调研数据sql和数据
     * @param request
     * @return
     */
    String getSurveySQLAndData(QwRagStreamRequest request);


    /**
     * 保存用户问题记录
     * @param history
     * @return
     */
    String saveQuestion(UserQAHistory history);


    /**
     * 分页获取用户问题记录
     */

    PageResult<UserQAHistory> getQuestion(PageQuestion question) throws Exception;

    /**
     * 根据问题id删除用户历史问题记录
     * @param id 历史问题id
     * @return 操作数据条数
     */
    String deleteQuestionById(int id, String luserId);

    /**
     * 置顶用户问题
     * @param id 问题id
     * @return 返回操作条数
     */
    String topQuestion(int id, String luserId);

    /**
     * 取消置顶用户问题
     * @param id 问题id
     * @return 返回操作条数
     */
    String cancelTop(int id, String luserId);

}
