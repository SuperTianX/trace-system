package com.steel.trace.controller;

import com.steel.trace.common.result.PageResult;
import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.ExcelUtil;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.dto.request.SlabSaveRequest;
import com.steel.trace.entity.Slab;
import com.steel.trace.service.SlabService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/slab")
@RequiredArgsConstructor
public class SlabController {

    private final SlabService slabService;

    @PostMapping
    @OperationLog(module = "铸坯管理", action = "新增", description = "新增铸坯")
    public Result<Slab> create(@Valid @RequestBody SlabSaveRequest request) {
        return Result.success(slabService.create(request));
    }

    @PutMapping("/{slabId}/status")
    @OperationLog(module = "铸坯管理", action = "状态变更", description = "变更铸坯状态")
    public Result<Void> updateStatus(@PathVariable String slabId, @RequestParam Integer status) {
        slabService.updateStatus(slabId, status);
        return Result.success();
    }

    @GetMapping("/{slabId}")
    public Result<Slab> get(@PathVariable String slabId) {
        return Result.success(slabService.getBySlabId(slabId));
    }

    @GetMapping("/page")
    public Result<PageResult<Slab>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String heatId,
            @RequestParam(required = false) String slabId,
            @RequestParam(required = false) Integer qualityStatus) {
        var p = slabService.pageQuery(page, size, heatId, slabId, qualityStatus);
        return Result.success(PageResult.of(p.getRecords(), p.getTotal(), (int)p.getCurrent(), (int) p.getSize()));
    }

    @DeleteMapping("/{slabId}")
    @OperationLog(module = "铸坯管理", action = "删除", description = "删除铸坯")
    public Result<Void> delete(@PathVariable String slabId) {
        slabService.delete(slabId);
        return Result.success();
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        List<Slab> list = slabService.listAll();
        ExcelUtil.export(response, "铸坯数据", Slab.class, list);
    }
}
