package com.steel.trace.controller;

import com.steel.trace.common.result.PageResult;
import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.ExcelUtil;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.dto.request.RollBatchSaveRequest;
import com.steel.trace.entity.RollBatch;
import com.steel.trace.service.RollBatchService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roll-batch")
@RequiredArgsConstructor
public class RollBatchController {

    private final RollBatchService rollBatchService;

    @PostMapping
    @OperationLog(module = "轧制批次", action = "新增", description = "新增轧制批次")
    public Result<RollBatch> create(@Valid @RequestBody RollBatchSaveRequest request) {
        return Result.success(rollBatchService.create(request));
    }

    @GetMapping("/{batchId}")
    public Result<RollBatch> get(@PathVariable String batchId) {
        return Result.success(rollBatchService.getByBatchId(batchId));
    }

    @GetMapping("/page")
    public Result<PageResult<RollBatch>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) String workOrderId,
            @RequestParam(required = false) Integer status) {
        var p = rollBatchService.pageQuery(page, size, batchId, workOrderId, status);
        return Result.success(PageResult.of(p.getRecords(), p.getTotal(), (int)p.getCurrent(), (int) p.getSize()));
    }

    @PutMapping("/{batchId}/merge")
    @OperationLog(module = "轧制批次", action = "合并", description = "批次合并")
    public Result<Void> merge(@PathVariable String batchId, @RequestParam String targetBatchId) {
        rollBatchService.merge(batchId, targetBatchId);
        return Result.success();
    }

    @PutMapping("/{batchId}/split")
    @OperationLog(module = "轧制批次", action = "拆分", description = "批次拆分")
    public Result<RollBatch> split(@PathVariable String batchId, @Valid @RequestBody RollBatchSaveRequest newBatch) {
        return Result.success(rollBatchService.split(batchId, newBatch));
    }

    @DeleteMapping("/{batchId}")
    @OperationLog(module = "轧制批次", action = "删除", description = "删除批次")
    public Result<Void> delete(@PathVariable String batchId) {
        rollBatchService.delete(batchId);
        return Result.success();
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        List<RollBatch> list = rollBatchService.listAll();
        ExcelUtil.export(response, "轧制批次数据", RollBatch.class, list);
    }
}
