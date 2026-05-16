package com.steel.trace.controller;

import com.steel.trace.common.result.PageResult;
import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.entity.ReconDiff;
import com.steel.trace.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @PostMapping("/execute")
    @OperationLog(module = "对账", action = "执行对账", description = "手动执行报工入库对账")
    public Result<List<ReconDiff>> execute() {
        return Result.success(reconciliationService.execute());
    }

    @GetMapping("/diff/page")
    public Result<PageResult<ReconDiff>> diffPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer diffType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String batchId) {
        var p = reconciliationService.pageQuery(page, size, diffType, status, batchId);
        return Result.success(PageResult.of(p.getRecords(), p.getTotal(), (int)p.getCurrent(), (int) p.getSize()));
    }

    @PutMapping("/diff/{id}/assign")
    @OperationLog(module = "对账", action = "分配责任", description = "分配对账差异责任")
    public Result<Void> assign(@PathVariable Long id, @RequestBody Map<String, String> body) {
        reconciliationService.assign(id, body.get("responsibleDept"));
        return Result.success();
    }

    @PutMapping("/diff/{id}/close")
    @OperationLog(module = "对账", action = "关闭", description = "关闭对账差异")
    public Result<Void> close(@PathVariable Long id) {
        reconciliationService.close(id);
        return Result.success();
    }

    @PutMapping("/diff/{id}/remark")
    @OperationLog(module = "对账", action = "备注", description = "添加对账差异备注")
    public Result<Void> remark(@PathVariable Long id, @RequestBody Map<String, String> body) {
        reconciliationService.remark(id, body.get("remark"));
        return Result.success();
    }
}
