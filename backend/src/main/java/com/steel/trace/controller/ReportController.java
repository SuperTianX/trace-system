package com.steel.trace.controller;

import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/trace-stat")
    public Result<Map<String, Object>> traceStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(reportService.traceStatistics(startDate, endDate));
    }

    @GetMapping("/chain-break-stat")
    public Result<Map<String, Object>> chainBreakAnalysis() {
        return Result.success(reportService.chainBreakAnalysis());
    }

    @GetMapping("/recon-diff-stat")
    public Result<Map<String, Object>> reconDiffReport() {
        return Result.success(reportService.reconDiffReport());
    }

    @GetMapping("/complaint-stat")
    public Result<Map<String, Object>> complaintStatistics() {
        return Result.success(reportService.complaintStatistics());
    }

    @GetMapping("/batch-quality")
    public Result<Map<String, Object>> batchQualityAnalysis() {
        return Result.success(reportService.batchQualityAnalysis());
    }

    @GetMapping("/production-anomaly")
    public Result<Map<String, Object>> productionAnomalyStats() {
        return Result.success(reportService.productionAnomalyStats());
    }
}
