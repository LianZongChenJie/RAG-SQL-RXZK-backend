package com.wnsse.sqlRag.mapper;

import com.wnsse.sqlRag.entity.RagMetric;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface RagMetricMapper {

    @Select("<script>" +
            "SELECT * FROM rag_metric WHERE 1=1" +
            "<if test='name != null and name != \"\"'> AND name LIKE CONCAT('%', #{name}, '%')</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            " ORDER BY createdTime DESC LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<RagMetric> selectPageList(@Param("offset") Long offset, @Param("pageSize") Long pageSize,
                                   @Param("name") String name, @Param("status") Integer status);

    @Select("<script>" +
            "SELECT COUNT(*) FROM rag_metric WHERE 1=1" +
            "<if test='name != null and name != \"\"'> AND name LIKE CONCAT('%', #{name}, '%')</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "</script>")
    Long count(@Param("name") String name, @Param("status") Integer status);

    @Select("SELECT * FROM rag_metric WHERE id = #{id}")
    RagMetric selectById(@Param("id") Integer id);

    @Insert("INSERT INTO rag_metric (pcode, code, name, `desc`, node, sqlStr, decimalPlace, status, outType, createdTime) " +
            "VALUES (#{pcode}, #{code}, #{name}, #{desc}, #{node}, #{sqlStr}, #{decimalPlace}, #{status}, #{outType}, #{createdTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RagMetric ragMetric);

    @Update("UPDATE rag_metric SET pcode=#{pcode}, code=#{code}, name=#{name}, " +
            "`desc`=#{desc}, node=#{node}, sqlStr=#{sqlStr}, decimalPlace=#{decimalPlace}, status=#{status}, " +
            "outType=#{outType}, updateUser=#{updateUser}, updateTime=#{updateTime} WHERE id=#{id}")
    void updateById(RagMetric ragMetric);

    @Select("SELECT * FROM rag_metric WHERE status = -2")
    List<RagMetric> selectPendingGenerate();

    @Update("UPDATE rag_metric SET sqlStr=#{sqlStr}, dsl=#{dsl}, status=#{status}, `comment`=#{comment}, updateTime=#{updateTime} WHERE id=#{id}")
    void updateSqlAndDsl(@Param("id") Integer id, @Param("sqlStr") String sqlStr, @Param("dsl") String dsl,
                          @Param("status") Integer status, @Param("comment") String comment, @Param("updateTime") java.time.LocalDateTime updateTime);

    @Select("SELECT * FROM rag_metric WHERE status = #{status}")
    List<RagMetric> selectByStatus(@Param("status") Integer status);

    @Select("SELECT * FROM rag_metric WHERE code = #{code}")
    RagMetric selectByCode(@Param("code") String code);
}
