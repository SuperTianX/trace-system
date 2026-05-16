package com.steel.trace.controller;

import com.steel.trace.common.result.PageResult;
import com.steel.trace.common.result.Result;
import com.steel.trace.entity.OperationLog;
import com.steel.trace.mapper.OperationLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/operation-log")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogMapper operationLogMapper;

    @GetMapping("/page")
    public Result<PageResult<OperationLog>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) wrapper.eq(OperationLog::getUsername, username);
        if (module != null && !module.isEmpty()) wrapper.eq(OperationLog::getModule, module);
        wrapper.orderByDesc(OperationLog::getCreateTime);
        var p = operationLogMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(PageResult.of(p.getRecords(), p.getTotal(), (int)p.getCurrent(), (int) p.getSize()));
    }
}
