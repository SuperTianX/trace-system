package com.steel.trace.controller;

import com.steel.trace.common.result.PageResult;
import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.entity.ChainBreak;
import com.steel.trace.service.ChainDiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chain-diagnosis")
@RequiredArgsConstructor
public class ChainDiagnosisController {

    private final ChainDiagnosisService chainDiagnosisService;

    @PostMapping("/execute")
    @OperationLog(module = "断链诊断", action = "执行诊断", description = "手动执行断链诊断")
    public Result<List<ChainBreak>> execute() {
        return Result.success(chainDiagnosisService.execute());
    }

    @GetMapping("/result/page")
    public Result<PageResult<ChainBreak>> resultPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer breakType,
            @RequestParam(required = false) Integer riskLevel,
            @RequestParam(required = false) Integer status) {
        var p = chainDiagnosisService.pageQuery(page, size, breakType, riskLevel, status);
        return Result.success(PageResult.of(p.getRecords(), p.getTotal(), (int)p.getCurrent(), (int) p.getSize()));
    }

    @PutMapping("/result/{id}/assign")
    @OperationLog(module = "断链诊断", action = "分配责任", description = "分配断链责任部门")
    public Result<Void> assign(@PathVariable Long id, @RequestBody Map<String, String> body) {
        chainDiagnosisService.assign(id, body.get("responsibleDept"));
        return Result.success();
    }

    @PutMapping("/result/{id}/close")
    @OperationLog(module = "断链诊断", action = "关闭", description = "关闭断链记录")
    public Result<Void> close(@PathVariable Long id) {
        chainDiagnosisService.close(id);
        return Result.success();
    }

    @PutMapping("/result/{id}/remark")
    @OperationLog(module = "断链诊断", action = "备注", description = "添加断链备注")
    public Result<Void> remark(@PathVariable Long id, @RequestBody Map<String, String> body) {
        chainDiagnosisService.remark(id, body.get("remark"));
        return Result.success();
    }

    /**
     * 分析断链影响范围
     */
    @GetMapping("/result/{id}/impact")
    public Result<Map<String, Object>> analyzeImpact(@PathVariable Long id) {
        return Result.success(chainDiagnosisService.analyzeImpact(id));
    }

    @GetMapping("/result/{id}")
    public Result<ChainBreak> getById(@PathVariable Long id) {
        return Result.success(chainDiagnosisService.getById(id));
    }
}
