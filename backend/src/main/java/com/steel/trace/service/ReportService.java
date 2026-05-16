package com.steel.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steel.trace.entity.*;
import com.steel.trace.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final HeatMapper heatMapper;
    private final SlabMapper slabMapper;
    private final RollBatchMapper rollBatchMapper;
    private final CoilMapper coilMapper;
    private final QcRecordMapper qcRecordMapper;
    private final InventoryMapper inventoryMapper;
    private final WorkReportMapper workReportMapper;
    private final ReconDiffMapper reconDiffMapper;
    private final ChainBreakMapper chainBreakMapper;
    private final ComplaintMapper complaintMapper;
    private final TraceLinkMapper traceLinkMapper;

    public Map<String, Object> traceStatistics(String startDate, String endDate) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTraces", traceLinkMapper.selectCount(null));
        result.put("forwardTraces", traceLinkMapper.selectCount(
                new LambdaQueryWrapper<TraceLink>().eq(TraceLink::getSourceType, 1)));
        result.put("backwardTraces", traceLinkMapper.selectCount(
                new LambdaQueryWrapper<TraceLink>().eq(TraceLink::getSourceType, 2)));
        return result;
    }

    public Map<String, Object> chainBreakAnalysis() {
        Map<String, Object> result = new LinkedHashMap<>();
        long total = chainBreakMapper.selectCount(null);
        result.put("total", total);
        result.put("pending", chainBreakMapper.selectCount(
                new LambdaQueryWrapper<ChainBreak>().eq(ChainBreak::getStatus, 0)));
        result.put("critical", chainBreakMapper.selectCount(
                new LambdaQueryWrapper<ChainBreak>().eq(ChainBreak::getRiskLevel, 4)));
        result.put("high", chainBreakMapper.selectCount(
                new LambdaQueryWrapper<ChainBreak>().eq(ChainBreak::getRiskLevel, 3)));

        // 按类型统计
        Map<Integer, Long> byType = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            int type = i;
            byType.put(type, chainBreakMapper.selectCount(
                    new LambdaQueryWrapper<ChainBreak>().eq(ChainBreak::getBreakType, type)));
        }
        result.put("byType", byType);
        return result;
    }

    public Map<String, Object> reconDiffReport() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", reconDiffMapper.selectCount(null));
        result.put("unprocessed", reconDiffMapper.selectCount(
                new LambdaQueryWrapper<ReconDiff>().eq(ReconDiff::getStatus, 0)));
        result.put("processing", reconDiffMapper.selectCount(
                new LambdaQueryWrapper<ReconDiff>().eq(ReconDiff::getStatus, 1)));
        result.put("closed", reconDiffMapper.selectCount(
                new LambdaQueryWrapper<ReconDiff>().eq(ReconDiff::getStatus, 2)));
        result.put("qtyMismatch", reconDiffMapper.selectCount(
                new LambdaQueryWrapper<ReconDiff>().eq(ReconDiff::getDiffType, 1)));
        result.put("dataMissing", reconDiffMapper.selectCount(
                new LambdaQueryWrapper<ReconDiff>().eq(ReconDiff::getDiffType, 2)));
        return result;
    }

    public Map<String, Object> complaintStatistics() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", complaintMapper.selectCount(null));
        result.put("registered", complaintMapper.selectCount(
                new LambdaQueryWrapper<Complaint>().eq(Complaint::getStatus, 0)));
        result.put("processing", complaintMapper.selectCount(
                new LambdaQueryWrapper<Complaint>().in(Complaint::getStatus, Arrays.asList(1, 2, 3))));
        result.put("closed", complaintMapper.selectCount(
                new LambdaQueryWrapper<Complaint>().in(Complaint::getStatus, Arrays.asList(4, 5))));
        return result;
    }

    public Map<String, Object> batchQualityAnalysis() {
        Map<String, Object> result = new LinkedHashMap<>();
        long totalCoils = coilMapper.selectCount(null);
        long qcPassCoils = 0;
        Map<String, long[]> batchStats = new LinkedHashMap<>(); // batchId -> [total, pass]

        List<Coil> allCoils = coilMapper.selectList(null);
        for (Coil coil : allCoils) {
            Integer pass = qcRecordMapper.countPassByCoilId(coil.getCoilId());
            if (pass != null && pass > 0) qcPassCoils++;
            String batchKey = coil.getBatchId() != null ? coil.getBatchId() : "未分配";
            batchStats.putIfAbsent(batchKey, new long[]{0, 0});
            batchStats.get(batchKey)[0]++;
            if (pass != null && pass > 0) batchStats.get(batchKey)[1]++;
        }

        // 按合格率排序的批次质量列表
        List<Map<String, Object>> batchList = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : batchStats.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("batchId", entry.getKey());
            item.put("totalCoils", entry.getValue()[0]);
            item.put("passCoils", entry.getValue()[1]);
            item.put("rate", entry.getValue()[0] > 0
                    ? String.format("%.1f%%", entry.getValue()[1] * 100.0 / entry.getValue()[0])
                    : "0%");
            batchList.add(item);
        }
        batchList.sort((a, b) -> {
            double ra = a.get("totalCoils") instanceof Number
                    ? ((Number) a.get("passCoils")).doubleValue() / Math.max(((Number) a.get("totalCoils")).doubleValue(), 1)
                    : 0;
            double rb = b.get("passCoils") instanceof Number
                    ? ((Number) b.get("passCoils")).doubleValue() / Math.max(((Number) b.get("totalCoils")).doubleValue(), 1)
                    : 0;
            return Double.compare(ra, rb);
        });

        result.put("totalCoils", totalCoils);
        result.put("qcPassCoils", qcPassCoils);
        result.put("qualityRate", totalCoils > 0 ? String.format("%.2f%%", qcPassCoils * 100.0 / totalCoils) : "0%");
        result.put("batchCount", batchStats.size());
        result.put("batchDetails", batchList);
        return result;
    }

    public Map<String, Object> productionAnomalyStats() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 按类型统计断链
        Map<String, Long> chainBreakByType = new LinkedHashMap<>();
        String[] typeNames = {"", "批次断链", "质检缺失", "未检入库", "MES/ERP不符", "重复报工", "数量异常"};
        for (int i = 1; i <= 6; i++) {
            int type = i;
            long count = chainBreakMapper.selectCount(
                    new LambdaQueryWrapper<ChainBreak>().eq(ChainBreak::getBreakType, type));
            chainBreakByType.put(typeNames[i], count);
        }
        result.put("chainBreakByType", chainBreakByType);

        // 按严重度统计投诉
        Map<String, Long> complaintBySeverity = new LinkedHashMap<>();
        String[] severityNames = {"", "轻微", "一般", "严重", "致命"};
        for (int i = 1; i <= 4; i++) {
            int sev = i;
            long count = complaintMapper.selectCount(
                    new LambdaQueryWrapper<Complaint>().eq(Complaint::getSeverity, sev));
            complaintBySeverity.put(severityNames[i], count);
        }
        result.put("complaintBySeverity", complaintBySeverity);

        // 待处理异常总数
        long pendingChainBreaks = chainBreakMapper.selectCount(
                new LambdaQueryWrapper<ChainBreak>().eq(ChainBreak::getStatus, 0));
        long pendingReconDiffs = reconDiffMapper.selectCount(
                new LambdaQueryWrapper<ReconDiff>().eq(ReconDiff::getStatus, 0));
        long pendingComplaints = complaintMapper.selectCount(
                new LambdaQueryWrapper<Complaint>().in(Complaint::getStatus, 0, 1, 2, 3));
        result.put("pendingChainBreaks", pendingChainBreaks);
        result.put("pendingReconDiffs", pendingReconDiffs);
        result.put("pendingComplaints", pendingComplaints);
        result.put("totalAnomalies", pendingChainBreaks + pendingReconDiffs + pendingComplaints);

        return result;
    }
}
