package com.steel.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steel.trace.entity.*;
import com.steel.trace.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ComplaintMapper complaintMapper;
    private final ChainBreakMapper chainBreakMapper;
    private final ReconDiffMapper reconDiffMapper;
    private final TraceLinkMapper traceLinkMapper;

    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();

        // 待处理异议
        long pendingComplaints = complaintMapper.selectCount(
                new LambdaQueryWrapper<Complaint>()
                        .in(Complaint::getStatus, Arrays.asList(0, 1, 2, 3)));
        overview.put("pendingComplaints", pendingComplaints);

        // 待处理断链
        long pendingChainBreaks = chainBreakMapper.selectCount(
                new LambdaQueryWrapper<ChainBreak>().eq(ChainBreak::getStatus, 0));
        overview.put("pendingChainBreaks", pendingChainBreaks);

        // 待处理对账差异
        long pendingDiffs = reconDiffMapper.selectCount(
                new LambdaQueryWrapper<ReconDiff>().eq(ReconDiff::getStatus, 0));
        overview.put("pendingReconDiffs", pendingDiffs);

        // 追溯次数
        long traceCount = traceLinkMapper.selectCount(null);
        overview.put("traceCount", traceCount);

        // 严重断链
        long criticalBreaks = chainBreakMapper.selectCount(
                new LambdaQueryWrapper<ChainBreak>()
                        .eq(ChainBreak::getRiskLevel, 4)
                        .eq(ChainBreak::getStatus, 0));
        overview.put("criticalBreaks", criticalBreaks);

        // 最新待处理清单
        List<ChainBreak> recentBreaks = chainBreakMapper.selectList(
                new LambdaQueryWrapper<ChainBreak>()
                        .eq(ChainBreak::getStatus, 0)
                        .orderByDesc(ChainBreak::getRiskLevel)
                        .last("LIMIT 10"));
        overview.put("recentChainBreaks", recentBreaks);

        List<ReconDiff> recentDiffs = reconDiffMapper.selectList(
                new LambdaQueryWrapper<ReconDiff>()
                        .eq(ReconDiff::getStatus, 0)
                        .orderByDesc(ReconDiff::getCreateTime)
                        .last("LIMIT 10"));
        overview.put("recentReconDiffs", recentDiffs);

        return overview;
    }
}
