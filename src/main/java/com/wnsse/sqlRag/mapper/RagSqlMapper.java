package com.wnsse.sqlRag.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface RagSqlMapper {
    List<Map<String, Object>> getObjectBy(String lRespondentId);
}
