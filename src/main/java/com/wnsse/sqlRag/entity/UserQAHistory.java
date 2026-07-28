package com.wnsse.sqlRag.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class UserQAHistory {
    private int id;
    //用户原始问题
    private String question;
    // 用户id
    public String luserid;
    //生成的sql
    public String generatedSql;
    //表格数据
    public String tableData;
    //模型返回的文本
    public String message;

    public String questionDesc;
    //模型返回的图数据
    public String chartData;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;
    //序号
    public Integer number;
    //置顶时间
    public Date topTime;
    //删除标志
    public Integer isDel;
}
