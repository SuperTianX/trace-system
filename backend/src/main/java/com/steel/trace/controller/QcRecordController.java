package com.steel.trace.controller;

import com.steel.trace.common.result.PageResult;
import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.ExcelUtil;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.dto.request.QcRecordSaveRequest;
import com.steel.trace.entity.QcRecord;
import com.steel.trace.service.QcRecordService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/qc-record")
@RequiredArgsConstructor
public class QcRecordController {

    private final QcRecordService qcRecordService;

    @PostMapping
    @OperationLog(module = "质检记录", action = "新增", description = "新增质检记录")
    public Result<QcRecord> create(@Valid @RequestBody QcRecordSaveRequest request) {
        return Result.success(qcRecordService.create(request));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "质检记录", action = "修改", description = "修改质检记录")
    public Result<QcRecord> update(@PathVariable Long id, @Valid @RequestBody QcRecordSaveRequest request) {
        return Result.success(qcRecordService.update(id, request));
    }

    @GetMapping("/{id}")
    public Result<QcRecord> get(@PathVariable Long id) {
        return Result.success(qcRecordService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResult<QcRecord>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer relateType,
            @RequestParam(required = false) String relateId,
            @RequestParam(required = false) Integer result) {
        var p = qcRecordService.pageQuery(page, size, relateType, relateId, result);
        return Result.success(PageResult.of(p.getRecords(), p.getTotal(), (int)p.getCurrent(), (int) p.getSize()));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "质检记录", action = "删除", description = "删除质检记录")
    public Result<Void> delete(@PathVariable Long id) {
        qcRecordService.delete(id);
        return Result.success();
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        List<QcRecord> list = qcRecordService.listAll();
        ExcelUtil.export(response, "质检记录数据", QcRecord.class, list);
    }
}
