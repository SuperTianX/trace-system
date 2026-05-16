package com.steel.trace.controller;

import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.dto.request.TraceRequest;
import com.steel.trace.entity.TraceLink;
import com.steel.trace.mapper.TraceLinkMapper;
import com.steel.trace.service.TraceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/trace")
@RequiredArgsConstructor
public class TraceController {

    private final TraceService traceService;
    private final TraceLinkMapper traceLinkMapper;

    @PostMapping("/forward")
    @OperationLog(module = "追溯", action = "正向追溯", description = "正向追溯炉→客户")
    public Result<Map<String, Object>> forward(@Valid @RequestBody TraceRequest request) {
        return Result.success(traceService.traceForward(request));
    }

    @PostMapping("/backward")
    @OperationLog(module = "追溯", action = "反向追溯", description = "反向追溯异议/卷→炉")
    public Result<Map<String, Object>> backward(@Valid @RequestBody TraceRequest request) {
        return Result.success(traceService.traceBackward(request));
    }

    @GetMapping("/link/{linkId}")
    public Result<TraceLink> getLink(@PathVariable String linkId) {
        return Result.success(traceLinkMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TraceLink>()
                        .eq(TraceLink::getLinkId, linkId)));
    }
}
