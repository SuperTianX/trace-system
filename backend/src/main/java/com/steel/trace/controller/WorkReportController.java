package com.steel.trace.controller;

import com.steel.trace.common.result.PageResult;
import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.ExcelUtil;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.dto.request.WorkReportSaveRequest;
import com.steel.trace.entity.WorkReport;
import com.steel.trace.service.WorkReportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/work-report")
@RequiredArgsConstructor
public class WorkReportController {

    private final WorkReportService workReportService;
    private final com.steel.trace.mapper.WorkReportMapper workReportMapper;

    @PostMapping
    @OperationLog(module = "报工管理", action = "录入", description = "录入报工")
    public Result<WorkReport> create(@Valid @RequestBody WorkReportSaveRequest request) {
        return Result.success(workReportService.create(request));
    }

    @GetMapping("/{id}")
    public Result<WorkReport> get(@PathVariable Long id) {
        WorkReport report = workReportMapper.selectById(id);
        if (report == null) {
            throw new com.steel.trace.common.exception.BusinessException("报工记录不存在");
        }
        return Result.success(report);
    }

    @PutMapping("/{id}/approve")
    @OperationLog(module = "报工管理", action = "审核", description = "审核报工")
    public Result<Void> approve(@PathVariable Long id, @RequestParam(defaultValue = "1") int status) {
        workReportService.approve(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @OperationLog(module = "报工管理", action = "驳回", description = "驳回报工")
    public Result<Void> reject(@PathVariable Long id) {
        workReportService.approve(id, 2);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @OperationLog(module = "报工管理", action = "撤销", description = "撤销报工")
    public Result<Void> cancel(@PathVariable Long id) {
        workReportService.cancel(id);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<WorkReport>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String workOrderId,
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) String coilId,
            @RequestParam(required = false) Integer approveStatus) {
        var p = workReportService.pageQuery(page, size, workOrderId, batchId, coilId, approveStatus);
        return Result.success(PageResult.of(p.getRecords(), p.getTotal(), (int)p.getCurrent(), (int) p.getSize()));
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        List<WorkReport> list = workReportService.listAll();
        ExcelUtil.export(response, "报工数据", WorkReport.class, list);
    }
}
