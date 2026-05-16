package com.steel.trace.controller;

import com.steel.trace.common.result.PageResult;
import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.entity.RuleConfig;
import com.steel.trace.service.RuleConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rule-config")
@RequiredArgsConstructor
public class RuleConfigController {

    private final RuleConfigService ruleConfigService;

    @PostMapping
    @OperationLog(module = "规则配置", action = "新增", description = "新增规则")
    public Result<RuleConfig> create(@RequestBody RuleConfig ruleConfig) {
        return Result.success(ruleConfigService.create(ruleConfig));
    }

    @GetMapping("/{id}")
    public Result<RuleConfig> get(@PathVariable Long id) {
        return Result.success(ruleConfigService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResult<RuleConfig>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer ruleType) {
        var p = ruleConfigService.pageQuery(page, size, ruleType);
        return Result.success(PageResult.of(p.getRecords(), p.getTotal(), (int)p.getCurrent(), (int) p.getSize()));
    }

    @PutMapping("/{id}/toggle")
    @OperationLog(module = "规则配置", action = "启用/禁用", description = "切换规则启用状态")
    public Result<Void> toggleEnabled(@PathVariable Long id) {
        ruleConfigService.toggleEnabled(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "规则配置", action = "删除", description = "删除规则")
    public Result<Void> delete(@PathVariable Long id) {
        ruleConfigService.delete(id);
        return Result.success();
    }
}
