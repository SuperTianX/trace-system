package com.steel.trace.controller;

import com.steel.trace.common.result.PageResult;
import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.ExcelUtil;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.dto.request.CoilSaveRequest;
import com.steel.trace.entity.Coil;
import com.steel.trace.service.CoilService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coil")
@RequiredArgsConstructor
public class CoilController {

    private final CoilService coilService;

    @PostMapping
    @OperationLog(module = "卷号档案", action = "新增", description = "新增钢卷")
    public Result<Coil> create(@Valid @RequestBody CoilSaveRequest request) {
        return Result.success(coilService.create(request));
    }

    @GetMapping("/{coilId}")
    public Result<Coil> get(@PathVariable String coilId) {
        return Result.success(coilService.getByCoilId(coilId));
    }

    @GetMapping("/page")
    public Result<PageResult<Coil>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String coilId,
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) Integer stockStatus,
            @RequestParam(required = false) Integer lifecycleStatus) {
        var p = coilService.pageQuery(page, size, coilId, batchId, stockStatus, lifecycleStatus);
        return Result.success(PageResult.of(p.getRecords(), p.getTotal(), (int)p.getCurrent(), (int) p.getSize()));
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        List<Coil> list = coilService.getByBatchId(null); // 实际应传参
        ExcelUtil.export(response, "钢卷数据", Coil.class, list);
    }

    @DeleteMapping("/{coilId}")
    @OperationLog(module = "卷号档案", action = "删除", description = "删除钢卷")
    public Result<Void> delete(@PathVariable String coilId) {
        coilService.delete(coilId);
        return Result.success();
    }
}
