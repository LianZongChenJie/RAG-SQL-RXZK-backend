package com.wnsse.sqlRag.controller;

import com.wnsse.sqlRag.common.Result;
import com.wnsse.sqlRag.entity.RagMetric;
import com.wnsse.sqlRag.service.RagMetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metric")
@RequiredArgsConstructor
@Tag(name = "指标管理", description = "指标相关接口")
public class RagMetricController {

    private final RagMetricService ragMetricService;

    @GetMapping("/pageList")
    @Operation(summary = "分页查询指标列表")
    public Result<?> pageList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "指标名称") @RequestParam(required = false) String name,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return Result.success("查询完成", ragMetricService.getPageList(pageNum, pageSize, name, status));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "查询指标详情")
    public Result<?> detail(@Parameter(description = "指标ID") @PathVariable Integer id) {
        return Result.success("查询完成", ragMetricService.getDetail(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增指标")
    public Result<?> add(@RequestBody RagMetric ragMetric) {
        ragMetricService.add(ragMetric);
        return Result.success("添加成功");
    }

    @PostMapping("/update")
    @Operation(summary = "更新指标")
    public Result<?> update(@RequestBody RagMetric ragMetric) {
        ragMetricService.update(ragMetric);
        return Result.success("更新成功");
    }

    @PostMapping("/batchGenerate")
    @Operation(summary = "批量生成SQL和DSL")
    public Result<?> batchGenerate() {
        ragMetricService.generateSqlAsync();
        return Result.success("SQL语句正在生成，请稍后在列表中查看！");
    }

    @PostMapping("/execute")
    @Operation(summary = "执行SQL查询")
    public Result<?> execute(
            @Parameter(description = "数据集表名") @RequestParam String tableName,
            @Parameter(description = "指标ID") @RequestParam(required = false) Integer metricId,
            @Parameter(description = "指标编码") @RequestParam(required = false) String metricCode) {
        return Result.success("查询成功", ragMetricService.executeSqlByTableName(tableName, metricId, metricCode));
    }

    @PostMapping("/updateDsl")
    @Operation(summary = "执行DSL更新")
    public Result<?> executeDsl() {
        return ragMetricService.updateDsl();
    }
}
