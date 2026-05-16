package com.steel.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steel.trace.common.constant.Constants;
import com.steel.trace.entity.*;
import com.steel.trace.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final WorkReportMapper workReportMapper;
    private final QcRecordMapper qcRecordMapper;
    private final InventoryMapper inventoryMapper;
    private final StockMapper stockMapper;
    private final ReconDiffMapper reconDiffMapper;
    private final CoilMapper coilMapper;

    @Transactional
    public List<ReconDiff> execute() {
        List<ReconDiff> diffs = new ArrayList<>();
        Set<String> allBatchIds = new HashSet<>();

        // 收集所有批次号
        workReportMapper.selectList(new LambdaQueryWrapper<WorkReport>()
                        .eq(WorkReport::getApproveStatus, 1))
                .forEach(w -> { if (w.getBatchId() != null) allBatchIds.add(w.getBatchId()); });

        coilMapper.selectList(new LambdaQueryWrapper<Coil>()
                        .isNotNull(Coil::getBatchId))
                .forEach(c -> allBatchIds.add(c.getBatchId()));

        inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getStatus, 1))
                .forEach(i -> { if (i.getBatchId() != null) allBatchIds.add(i.getBatchId()); });

        for (String batchId : allBatchIds) {
            // 四端数量聚合
            BigDecimal wrQty = sumWorkReportQty(batchId);
            BigDecimal qcQty = sumQcPassQty(batchId);
            BigDecimal erpQty = sumErpInboundQty(batchId);
            BigDecimal stQty = sumStockQtyByBatch(batchId);

            if (hasDiff(wrQty, qcQty, erpQty, stQty)) {
                String desc = buildDiffDesc(wrQty, qcQty, erpQty, stQty);
                ReconDiff diff = new ReconDiff(batchId, null, wrQty, qcQty, erpQty, stQty,
                        Constants.DiffType.QTY_MISMATCH, desc);
                reconDiffMapper.insert(diff);
                diffs.add(diff);
            }
        }

        // 检查数据缺失：有报工无入库
        checkMissingData(diffs);

        log.info("对账完成，共发现 {} 条差异", diffs.size());
        return diffs;
    }

    private BigDecimal sumWorkReportQty(String batchId) {
        List<WorkReport> reports = workReportMapper.selectList(
                new LambdaQueryWrapper<WorkReport>()
                        .eq(WorkReport::getBatchId, batchId)
                        .eq(WorkReport::getApproveStatus, 1));
        return reports.stream()
                .map(WorkReport::getReportQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumQcPassQty(String batchId) {
        List<Coil> coils = coilMapper.selectList(
                new LambdaQueryWrapper<Coil>().eq(Coil::getBatchId, batchId));
        BigDecimal passCount = BigDecimal.ZERO;
        for (Coil coil : coils) {
            Integer count = qcRecordMapper.countPassByCoilId(coil.getCoilId());
            if (count != null && count > 0) {
                passCount = passCount.add(BigDecimal.ONE);
            }
        }
        return passCount;
    }

    private BigDecimal sumErpInboundQty(String batchId) {
        List<Coil> coils = coilMapper.selectList(
                new LambdaQueryWrapper<Coil>().eq(Coil::getBatchId, batchId));
        BigDecimal total = BigDecimal.ZERO;
        for (Coil coil : coils) {
            BigDecimal qty = inventoryMapper.sumInboundQtyByCoilId(coil.getCoilId());
            if (qty != null) total = total.add(qty);
        }
        return total;
    }

    private BigDecimal sumStockQtyByBatch(String batchId) {
        List<Coil> coils = coilMapper.selectList(
                new LambdaQueryWrapper<Coil>().eq(Coil::getBatchId, batchId));
        BigDecimal total = BigDecimal.ZERO;
        for (Coil coil : coils) {
            Stock stock = stockMapper.selectOne(
                    new LambdaQueryWrapper<Stock>().eq(Stock::getCoilId, coil.getCoilId()));
            if (stock != null && stock.getQuantity() != null) {
                total = total.add(stock.getQuantity());
            }
        }
        return total;
    }

    private boolean hasDiff(BigDecimal wr, BigDecimal qc, BigDecimal erp, BigDecimal st) {
        return wr.compareTo(qc) != 0 || qc.compareTo(erp) != 0 || erp.compareTo(st) != 0;
    }

    private String buildDiffDesc(BigDecimal wr, BigDecimal qc, BigDecimal erp, BigDecimal st) {
        return String.format("报工:%.2f 质检合格:%.2f ERP入库:%.2f 库存:%.2f", wr, qc, erp, st);
    }

    private void checkMissingData(List<ReconDiff> diffs) {
        List<Coil> coils = coilMapper.selectList(
                new LambdaQueryWrapper<Coil>().eq(Coil::getLifecycleStatus, 2));
        for (Coil coil : coils) {
            Integer passCount = qcRecordMapper.countPassByCoilId(coil.getCoilId());
            if (passCount == null || passCount == 0) {
                ReconDiff diff = new ReconDiff(null, coil.getCoilId(),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        Constants.DiffType.DATA_MISSING, "卷号" + coil.getCoilId() + "已入库但无质检记录");
                reconDiffMapper.insert(diff);
                diffs.add(diff);
            }
        }
    }

    @Transactional
    public void assign(Long diffId, String responsibleDept) {
        ReconDiff diff = reconDiffMapper.selectById(diffId);
        if (diff == null) return;
        diff.setResponsibleDept(responsibleDept);
        diff.setStatus(Constants.ReconStatus.PROCESSING);
        reconDiffMapper.updateById(diff);
    }

    @Transactional
    public void close(Long diffId) {
        ReconDiff diff = reconDiffMapper.selectById(diffId);
        if (diff == null) return;
        diff.setStatus(Constants.ReconStatus.CLOSED);
        reconDiffMapper.updateById(diff);
    }

    @Transactional
    public void remark(Long diffId, String remark) {
        ReconDiff diff = reconDiffMapper.selectById(diffId);
        if (diff == null) return;
        diff.setRemark(remark);
        reconDiffMapper.updateById(diff);
    }

    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<ReconDiff> pageQuery(
            int page, int size, Integer diffType, Integer status, String batchId) {
        LambdaQueryWrapper<ReconDiff> wrapper = new LambdaQueryWrapper<>();
        if (diffType != null) wrapper.eq(ReconDiff::getDiffType, diffType);
        if (status != null) wrapper.eq(ReconDiff::getStatus, status);
        if (batchId != null && !batchId.isEmpty()) wrapper.like(ReconDiff::getBatchId, batchId);
        wrapper.orderByDesc(ReconDiff::getCreateTime);
        return reconDiffMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), wrapper);
    }

    private static class LambdaQueryWrapper<T> extends com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<T> {}
}
