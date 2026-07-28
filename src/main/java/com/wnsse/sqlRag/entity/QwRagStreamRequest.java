package com.wnsse.sqlRag.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Arrays;

@Data
public class QwRagStreamRequest {

    // 共有字段
    private String luserid;
    private String dbType;
    private String message;
    private String dbName;
    private String tableName;
    private String messageDesc;

    // 客观独有字段
    private String sqlStr;
    @JsonProperty("lId")
    private String lId;
    private String code;
    private String outType;
    // 问卷调研相关字段
    @JsonProperty("lQuestionnaireId")
    private String lQuestionnaireId;
    // 题型
    private String strType;
    private String snapshotId;
    // 原始问题
    private String strDesc;
    // 调研对象id
    @JsonProperty("lRespondentId")
    private String lRespondentId;

    @JsonProperty("lOrgId")
    private String lOrgId;

    private String[] strTitleList;
    private String[] strOptionList;

    // 问题导航
    private String strName;
    private String strAnswerColumn;

    @Override
    public String toString() {
        return "QwRagStreamRequest{" +
                "dbType='" + dbType + '\'' +
                ", message='" + message + '\'' +
                ", messageDesc='" + messageDesc + '\'' +
                ", dbName='" + dbName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", sqlStr='" + sqlStr + '\'' +
                ", lId='" + lId + '\'' +
                ", code='" + code + '\'' +
                ", lQuestionnaireId='" + lQuestionnaireId + '\'' +
                ", strType='" + strType + '\'' +
                ", snapshotId='" + snapshotId + '\'' +
                ", strDesc='" + strDesc + '\'' +
                ", lRespondentId='" + lRespondentId + '\'' +
                ", lOrgId='" + lOrgId + '\'' +
                ", strTitleList='" + Arrays.toString(strTitleList) + '\'' +
                ", strOptionList='" + Arrays.toString(strOptionList) + '\'' +
                ", strName='" + strName + '\'' +
                ", strAnswerColumn='" + strAnswerColumn + '\'' +
                '}';
    }
}
