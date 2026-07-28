package com.wnsse.sqlRag.controller;

import com.wnsse.sqlRag.common.Result;
import com.wnsse.sqlRag.service.RagDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dict")
public class RagDictController {

    @Autowired
    private RagDictService ragDictService;

    @GetMapping("/treeList")
    public Result<?> treeList() {
        return Result.success("查询完成", ragDictService.getTreeList());
    }
}