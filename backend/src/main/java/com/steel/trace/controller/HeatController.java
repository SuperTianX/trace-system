package com.steel.trace.controller;

import com.steel.trace.common.result.PageResult;
import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.ExcelUtil;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.dto.request.HeatPageQuery;
import com.steel.trace.dto.request.HeatSaveRequest;
import com.steel.trace.entity.Heat;
import com.steel.trace.service.HeatService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/heat")
@RequiredArgsConstructor
public class HeatController {

    private final HeatService heatService;

    @PostMapping
    @OperationLog(module = "炉次管理", action = "新增", description = "新增炉次")
    public Result<Heat> create(@Valid @RequestBody HeatSaveRequest request) {
        return Result.success(heatService.create(request));
    }

    @PutMapping("/{heatId}")
    @OperationLog(module = "炉次管理", action = "修改", description = "修改炉次")
    public Result<Heat> update(@PathVariable String heatId, @Valid @RequestBody HeatSaveRequest request) {
        return Result.success(heatService.update(heatId, request));
    }

    @GetMapping("/{heatId}")
    public Result<Heat> get(@PathVariable String heatId) {
        return Result.success(heatService.getByHeatId(heatId));
    }

    @GetMapping("/page")
    public Result<PageResult<Heat>> page(HeatPageQuery query) {
        var page = heatService.pageQuery(query);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @GetMapping("/list")
    public Result<List<Heat>> list() {
        return Result.success(heatService.listAll());
    }

    @DeleteMapping("/{heatId}")
    @OperationLog(module = "炉次管理", action = "作废", description = "作废炉次")
    public Result<Void> delete(@PathVariable String heatId) {
        heatService.delete(heatId);
        return Result.success();
    }

    @PostMapping("/batch-import")
    @OperationLog(module = "炉次管理", action = "批量导入", description = "批量导入炉次")
    public Result<Void> batchImport(@RequestParam("file") MultipartFile file) throws IOException {
        List<HeatSaveRequest> list = ExcelUtil.importData(file.getInputStream(), HeatSaveRequest.class);
        heatService.batchImport(list);
        return Result.success();
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        List<Heat> list = heatService.listAll();
        ExcelUtil.export(response, "炉次数据", Heat.class, list);
    }
}
