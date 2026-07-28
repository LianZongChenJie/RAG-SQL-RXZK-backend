package com.wnsse.sqlRag.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RagMetric {

    private Integer id;
    private String pcode;
    private String code;
    private String name;
    private String desc;
    private String node;
    private String dsl;
    private String sqlStr;
    private Integer decimalPlace;
    private Integer status;
    private String approver;
    private String comment;
    private String outType;
    private LocalDateTime createdTime;
    private LocalDateTime updateTime;
    private String updateUser;
}
