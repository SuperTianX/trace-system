package com.steel.trace.controller;

import com.steel.trace.common.result.PageResult;
import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.ExcelUtil;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.dto.request.ComplaintSaveRequest;
import com.steel.trace.entity.Complaint;
import com.steel.trace.service.ComplaintService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/complaint")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    @OperationLog(module = "质量异议", action = "登记", description = "登记质量异议")
    public Result<Complaint> register(@Valid @RequestBody ComplaintSaveRequest request) {
        return Result.success(complaintService.register(request));
    }

    @PutMapping("/{id}/trace")
    @OperationLog(module = "质量异议", action = "追溯", description = "开始异议追溯")
    public Result<Void> startTrace(@PathVariable Long id) {
        complaintService.startTracing(id);
        return Result.success();
    }

    @PutMapping("/{id}/responsible")
    @OperationLog(module = "质量异议", action = "责任判定", description = "判定异议责任")
    public Result<Void> assignResponsibility(@PathVariable Long id,
                                              @RequestParam String responsibleDept,
                                              @RequestParam String rootCause) {
        complaintService.assignResponsibility(id, responsibleDept, rootCause);
        return Result.success();
    }

    @PutMapping("/{id}/measure")
    @OperationLog(module = "质量异议", action = "制定措施", description = "制定处理措施")
    public Result<Void> setMeasures(@PathVariable Long id, @RequestParam String measures) {
        complaintService.setMeasures(id, measures);
        return Result.success();
    }

    @PutMapping("/{id}/rectification")
    @OperationLog(module = "质量异议", action = "整改", description = "执行整改")
    public Result<Void> rectification(@PathVariable Long id, @RequestParam String result) {
        complaintService.setRectification(id, result);
        return Result.success();
    }

    @PutMapping("/{id}/close")
    @OperationLog(module = "质量异议", action = "关闭", description = "关闭质量异议")
    public Result<Void> close(@PathVariable Long id) {
        complaintService.close(id);
        return Result.success();
    }

    @PutMapping("/{id}/archive")
    @OperationLog(module = "质量异议", action = "归档", description = "归档质量异议")
    public Result<Void> archive(@PathVariable Long id) {
        complaintService.archive(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Complaint> get(@PathVariable Long id) {
        return Result.success(complaintService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResult<Complaint>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String complaintId,
            @RequestParam(required = false) String coilId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer severity) {
        var p = complaintService.pageQuery(page, size, complaintId, coilId, status, severity);
        return Result.success(PageResult.of(p.getRecords(), p.getTotal(), (int)p.getCurrent(), (int) p.getSize()));
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        List<Complaint> list = complaintService.listAll();
        ExcelUtil.export(response, "质量异议数据", Complaint.class, list);
    }
}
