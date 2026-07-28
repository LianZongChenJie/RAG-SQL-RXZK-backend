package com.wnsse.sqlRag.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqlExecutionService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取指定表的 CREATE TABLE DDL 语句
     */
    public String getTableDDL(String tableName) {
        // 校验表名合法性，防止SQL注入
        if (!tableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("找不到对应的数据集: " + tableName);
        }
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SHOW CREATE TABLE " + tableName);
            if (rows.isEmpty()) {
                throw new RuntimeException("找不到对应的数据集: " + tableName);
            }
            String ddl = (String) rows.get(0).get("Create Table");
            log.info("获取表结构成功 - tableName: {}", tableName);
            return ddl;
        } catch (DataAccessException e) {
            log.error("获取表结构失败 - tableName: {}", tableName, e);
            throw new RuntimeException("找不到对应的数据集: " + tableName);
        }
    }

    /**
     * 执行查询SQL并返回结果
     */
    @DS("slave")
    public List<Map<String, Object>> executeQuery(String sql) {
        log.info("执行SQL: {}", sql);
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            log.info("SQL执行完成, 返回行数: {}", result.size());
            return result;
        } catch (DataAccessException e) {
            log.error("SQL执行失败: {}", sql, e);
            throw new RuntimeException("查询执行失败: " + e.getMessage());
        }
    }

    /**
     * 执行查询SQL并返回结果
     */
    @DS("slave")
    public List<Map<String, Object>> execute(String sql) {
        log.info("执行SQL: {}", sql);
        try {
            return jdbcTemplate.execute((Connection connection) -> {
                try (Statement stmt = connection.createStatement()) {
                    boolean hasResultSet = stmt.execute(sql);
                    List<Map<String, Object>> results = new ArrayList<>();

                    do {
                        if (hasResultSet) {
                            ResultSet rs = stmt.getResultSet();
                            ResultSetMetaData metaData = rs.getMetaData();
                            int columnCount = metaData.getColumnCount();

                            while (rs.next()) {
                                Map<String, Object> row = new LinkedHashMap<>();
                                for (int i = 1; i <= columnCount; i++) {
                                    // ✅ 改这里：getColumnName → getColumnLabel
                                    row.put(metaData.getColumnLabel(i), rs.getObject(i));
                                }
                                results.add(row);
                            }
                        }
                        hasResultSet = stmt.getMoreResults();
                    } while (hasResultSet || stmt.getUpdateCount() != -1);

                    return results;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("查询执行失败: " + e.getMessage(), e);
        }
    }


    @DS("slave")
    public List<List<Map<String, Object>>> executeManyResult(String sql) {
        log.info("执行SQL: {}", sql);
        try {
            return jdbcTemplate.execute((Connection connection) -> {
                try (Statement stmt = connection.createStatement()) {
                    boolean hasResultSet = stmt.execute(sql);
                    List<List<Map<String, Object>>> allResults = new ArrayList<>();

                    do {
                        if (hasResultSet) {
                            ResultSet rs = stmt.getResultSet();
                            ResultSetMetaData metaData = rs.getMetaData();
                            int columnCount = metaData.getColumnCount();
                            List<Map<String, Object>> resultSetRows = new ArrayList<>();

                            while (rs.next()) {
                                Map<String, Object> row = new LinkedHashMap<>();
                                for (int i = 1; i <= columnCount; i++) {
                                    row.put(metaData.getColumnLabel(i), rs.getObject(i));
                                }
                                resultSetRows.add(row);
                            }
                            allResults.add(resultSetRows);
                        }
                        hasResultSet = stmt.getMoreResults();
                    } while (hasResultSet || stmt.getUpdateCount() != -1);

                    return allResults;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("查询执行失败: " + e.getMessage(), e);
        }
    }
}
