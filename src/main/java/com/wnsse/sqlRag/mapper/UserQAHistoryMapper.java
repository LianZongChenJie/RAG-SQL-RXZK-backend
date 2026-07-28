package com.wnsse.sqlRag.mapper;

import com.wnsse.sqlRag.entity.UserQAHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserQAHistoryMapper {

    @Insert("INSERT INTO ${tableName} (luserid, question, generatedSql, tableData, message, chartData, createTime, isdel, number) " +
            "VALUES (#{param1.luserid}, #{param1.question}, #{param1.generatedSql}, #{param1.tableData}, #{param1.message}, #{param1.chartData}, NOW(), 0, 2)")
    @Options(useGeneratedKeys = true, keyProperty = "param1.id")
    int insertData(UserQAHistory history, String tableName);

    @Select("SELECT * FROM ${tableName} WHERE `isdel` = 0 and `luserid` = #{luserid} order by `number` asc , `topTime` desc, `createTime` desc limit #{pageSize} offset #{offset} ")
    List<UserQAHistory> selectPageQuestion(@Param("luserid") String luserid, @Param("pageSize") Integer pageSize, @Param("offset") Integer offset, @Param("tableName") String tableName);

    @Select("SELECT count(1) FROM ${tableName} WHERE `isdel` = 0 and `luserid` = #{luserid}")
    Long selectQuestionCount(@Param("luserid") String luserid, @Param("tableName") String tableName);

    @Update("UPDATE ${tableName} SET isdel = 1  WHERE id = #{id}")
    int deleteQuestion(@Param("id") int id, @Param("tableName") String tableName);

    @Update("UPDATE ${tableName} SET `number` = 1, topTime = NOW() WHERE id = #{id}")
    int topQuestion(@Param("id") int id, @Param("tableName") String tableName);

    @Update("UPDATE ${tableName} SET `number` = 2, topTime = Null WHERE id = #{id}")
    int cancelTop(@Param("id") int id, @Param("tableName") String tableName);
}
