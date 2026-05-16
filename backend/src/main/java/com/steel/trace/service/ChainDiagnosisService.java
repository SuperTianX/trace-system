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
public class ChainDiagnosisService {

    private final HeatMapper heatMapper;
    private final SlabMapper slabMapper;
    private final RollBatchMapper rollBatchMapper;
    private final CoilMapper coilMapper;
    private final QcRecordMapper qcRecordMapper;
    private final WorkReportMapper workReportMapper;
    private final InventoryMapper inventoryMapper;
    private final ChainBreakMapper chainBreakMapper;

    @Transactional
    public List<ChainBreak> execute() {
        List<ChainBreak> results = new ArrayList<>();

        // 1. 批次断链检查
        checkChainBreak(results);

        // 2. 质检缺失检查
        checkQcMissing(results);

        // 3. 未质检入库检查
        checkUnqcInbound(results);

        // 4. MES/ERP不一致检查
        checkMesErpMismatch(results);

        // 5. 重复报工检查
        checkDuplicateReport(results);

        // 6. 数量异常检查
        checkQtyAbnormal(results);

        log.info("断链诊断完成，共发现 {} 条问题", results.size());
        return results;
    }

    private void checkChainBreak(List<ChainBreak> results) {
        // slab 无关联 heat
        List<Slab> orphanSlabs = slabMapper.selectList(
                new LambdaQueryWrapper<Slab>()
                        .notInSql(Slab::getHeatId, "SELECT heat_id FROM tr_heat WHERE is_deleted = 0")
                        .eq(Slab::getIsDeleted, 0));
        for (Slab slab : orphanSlabs) {
            ChainBreak cb = createBreak(Constants.BreakType.CHAIN_BREAK, slab.getHeatId(),
                    slab.getSlabId(), null, null,
                    "铸坯 " + slab.getSlabId() + " 关联的炉号 " + slab.getHeatId() + " 不存在",
                    Constants.RiskLevel.CRITICAL);
            results.add(cb);
        }

        // coil 无关联 batch
        List<Coil> orphanCoils = coilMapper.selectList(
                new LambdaQueryWrapper<Coil>()
                        .notInSql(Coil::getBatchId, "SELECT batch_id FROM tr_roll_batch WHERE is_deleted = 0")
                        .eq(Coil::getIsDeleted, 0));
        for (Coil coil : orphanCoils) {
            ChainBreak cb = createBreak(Constants.BreakType.CHAIN_BREAK, null, null,
                    coil.getBatchId(), coil.getCoilId(),
                    "钢卷 " + coil.getCoilId() + " 关联的批次 " + coil.getBatchId() + " 不存在",
                    Constants.RiskLevel.CRITICAL);
            results.add(cb);
        }
    }

    private void checkQcMissing(List<ChainBreak> results) {
        List<Coil> inStockCoils = coilMapper.selectList(
                new LambdaQueryWrapper<Coil>()
                        .in(Coil::getLifecycleStatus, Arrays.asList(2, 3))
                        .eq(Coil::getIsDeleted, 0));
        for (Coil coil : inStockCoils) {
            Integer passCount = qcRecordMapper.countPassByCoilId(coil.getCoilId());
            if (passCount == null || passCount == 0) {
                ChainBreak cb = createBreak(Constants.BreakType.QC_MISSING, null, null,
                        coil.getBatchId(), coil.getCoilId(),
                        "钢卷 " + coil.getCoilId() + " 无合格质检记录",
                        Constants.RiskLevel.HIGH);
                results.add(cb);
            }
        }
    }

    private void checkUnqcInbound(List<ChainBreak> results) {
        List<Inventory> inbounds = inventoryMapper.selectList(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getDocType, 1)
                        .eq(Inventory::getStatus, 1)
                        .eq(Inventory::getIsDeleted, 0));
        for (Inventory inv : inbounds) {
            Integer passCount = qcRecordMapper.countPassByCoilId(inv.getCoilId());
            if (passCount == null || passCount == 0) {
                ChainBreak cb = createBreak(Constants.BreakType.UNQC_INBOUND, null, null,
                        inv.getBatchId(), inv.getCoilId(),
                        "入库单 " + inv.getDocNo() + " 关联卷 " + inv.getCoilId() + " 无合格质检",
                        Constants.RiskLevel.HIGH);
                results.add(cb);
            }
        }
    }

    private void checkMesErpMismatch(List<ChainBreak> results) {
        // 有报工记录但无入库记录的批次
        Set<String> reportedBatches = new HashSet<>();
        workReportMapper.selectList(new LambdaQueryWrapper<WorkReport>()
                        .eq(WorkReport::getApproveStatus, 1))
                .forEach(w -> { if (w.getBatchId() != null) reportedBatches.add(w.getBatchId()); });

        Set<String> inboundBatches = new HashSet<>();
        inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getDocType, 1)
                        .eq(Inventory::getStatus, 1)
                        .eq(Inventory::getIsDeleted, 0))
                .forEach(i -> { if (i.getBatchId() != null) inboundBatches.add(i.getBatchId()); });

        for (String batchId : reportedBatches) {
            if (!inboundBatches.contains(batchId)) {
                ChainBreak cb = createBreak(Constants.BreakType.MES_ERP_MISMATCH, null, null,
                        batchId, null,
                        "批次 " + batchId + " 有报工记录但无入库记录",
                        Constants.RiskLevel.MEDIUM);
                results.add(cb);
            }
        }
    }

    private void checkDuplicateReport(List<ChainBreak> results) {
        List<WorkReport> reports = workReportMapper.selectList(
                new LambdaQueryWrapper<WorkReport>()
                        .eq(WorkReport::getApproveStatus, 1));
        Map<String, List<WorkReport>> grouped = new HashMap<>();
        for (WorkReport r : reports) {
            String key = r.getBatchId() + "_" + r.getProcessName();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
        }
        for (Map.Entry<String, List<WorkReport>> entry : grouped.entrySet()) {
            if (entry.getValue().size() > 1) {
                WorkReport first = entry.getValue().get(0);
                ChainBreak cb = createBreak(Constants.BreakType.DUPLICATE_REPORT, null, null,
                        first.getBatchId(), first.getCoilId(),
                        "批次 " + first.getBatchId() + " 工序 " + first.getProcessName() + " 有 " +
                                entry.getValue().size() + " 条报工记录",
                        Constants.RiskLevel.MEDIUM);
                results.add(cb);
            }
        }
    }

    private void checkQtyAbnormal(List<ChainBreak> results) {
        List<Coil> coils = coilMapper.selectList(
                new LambdaQueryWrapper<Coil>()
                        .eq(Coil::getIsDeleted, 0));
        for (Coil coil : coils) {
            List<WorkReport> reports = workReportMapper.selectList(
                    new LambdaQueryWrapper<WorkReport>()
                            .eq(WorkReport::getCoilId, coil.getCoilId())
                            .eq(WorkReport::getApproveStatus, 1));
            BigDecimal reportQty = reports.stream()
                    .map(WorkReport::getReportQuantity)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal inboundQty = inventoryMapper.sumInboundQtyByCoilId(coil.getCoilId());

            if (reportQty.compareTo(BigDecimal.ZERO) > 0 && inboundQty != null &&
                    reportQty.subtract(inboundQty).abs().compareTo(new BigDecimal("0.1")) > 0) {
                ChainBreak cb = createBreak(Constants.BreakType.QTY_ABNORMAL, null, null,
                        coil.getBatchId(), coil.getCoilId(),
                        "钢卷 " + coil.getCoilId() + " 报工 " + reportQty + " 入库 " + inboundQty + " 不一致",
                        Constants.RiskLevel.LOW);
                results.add(cb);
            }
        }
    }

    private ChainBreak createBreak(int breakType, String heatId, String slabId, String batchId, String coilId, String desc, int riskLevel) {
        ChainBreak cb = new ChainBreak();
        cb.setBreakType(breakType);
        cb.setHeatId(heatId);
        cb.setSlabId(slabId);
        cb.setBatchId(batchId);
        cb.setCoilId(coilId);
        cb.setBreakDesc(desc);
        cb.setRiskLevel(riskLevel);
        cb.setStatus(Constants.ChainBreakStatus.PENDING);
        chainBreakMapper.insert(cb);
        return cb;
    }

    @Transactional
    public void assign(Long id, String responsibleDept) {
        ChainBreak cb = chainBreakMapper.selectById(id);
        if (cb == null) return;
        cb.setResponsibleDept(responsibleDept);
        cb.setStatus(Constants.ChainBreakStatus.PROCESSING);
        chainBreakMapper.updateById(cb);
    }

    @Transactional
    public void close(Long id) {
        ChainBreak cb = chainBreakMapper.selectById(id);
        if (cb == null) return;
        cb.setStatus(Constants.ChainBreakStatus.CLOSED);
        chainBreakMapper.updateById(cb);
    }

    @Transactional
    public void remark(Long id, String remark) {
        ChainBreak cb = chainBreakMapper.selectById(id);
        if (cb == null) return;
        cb.setRemark(remark);
        chainBreakMapper.updateById(cb);
    }

    /**
     * 分析断链影响范围
     */
    public Map<String, Object> analyzeImpact(Long id) {
        ChainBreak cb = chainBreakMapper.selectById(id);
        if (cb == null) {
            throw new com.steel.trace.common.exception.BusinessException("断链记录不存在");
        }

        Set<String> affectedCoilIds = new LinkedHashSet<>();
        Set<String> affectedBatchIds = new LinkedHashSet<>();
        Set<String> customers = new LinkedHashSet<>();

        // 根据断链记录中的 ID 信息向上/下游追溯影响范围
        if (cb.getCoilId() != null) {
            affectedCoilIds.add(cb.getCoilId());
            // 查找同批次的其他卷
            if (cb.getBatchId() != null) {
                coilMapper.selectList(new LambdaQueryWrapper<Coil>()
                        .eq(Coil::getBatchId, cb.getBatchId())
                        .eq(Coil::getIsDeleted, 0))
                        .forEach(c -> {
                            affectedCoilIds.add(c.getCoilId());
                            if (c.getCustomerName() != null) customers.add(c.getCustomerName());
                        });
            }
        }

        if (cb.getBatchId() != null) {
            affectedBatchIds.add(cb.getBatchId());
            // 通过批次找所有卷
            coilMapper.selectList(new LambdaQueryWrapper<Coil>()
                    .eq(Coil::getBatchId, cb.getBatchId())
                    .eq(Coil::getIsDeleted, 0))
                    .forEach(c -> {
                        affectedCoilIds.add(c.getCoilId());
                        if (c.getCustomerName() != null) customers.add(c.getCustomerName());
                    });
        }

        if (cb.getHeatId() != null) {
            // 通过炉号找所有 slab → batch → coil
            List<Slab> slabs = slabMapper.selectList(new LambdaQueryWrapper<Slab>()
                    .eq(Slab::getHeatId, cb.getHeatId())
                    .eq(Slab::getIsDeleted, 0));
            for (Slab slab : slabs) {
                if (slab.getRollBatchId() != null) {
                    affectedBatchIds.add(slab.getRollBatchId());
                }
            }
            // 再通过批次找卷
            for (String batchId : affectedBatchIds) {
                coilMapper.selectList(new LambdaQueryWrapper<Coil>()
                        .eq(Coil::getBatchId, batchId)
                        .eq(Coil::getIsDeleted, 0))
                        .forEach(c -> {
                            affectedCoilIds.add(c.getCoilId());
                            if (c.getCustomerName() != null) customers.add(c.getCustomerName());
                        });
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("breakId", id);
        result.put("breakDesc", cb.getBreakDesc());
        result.put("breakType", cb.getBreakType());
        result.put("affectedBatchCount", affectedBatchIds.size());
        result.put("affectedCoilCount", affectedCoilIds.size());
        result.put("affectedCustomerCount", customers.size());
        result.put("affectedBatches", new ArrayList<>(affectedBatchIds));
        result.put("affectedCoils", new ArrayList<>(affectedCoilIds));
        result.put("affectedCustomers", new ArrayList<>(customers));
        result.put("riskLevel", cb.getRiskLevel());
        return result;
    }

    public ChainBreak getById(Long id) {
        ChainBreak cb = chainBreakMapper.selectById(id);
        if (cb == null) throw new com.steel.trace.common.exception.BusinessException("断链记录不存在");
        return cb;
    }

    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<ChainBreak> pageQuery(
            int page, int size, Integer breakType, Integer riskLevel, Integer status) {
        LambdaQueryWrapper<ChainBreak> wrapper = new LambdaQueryWrapper<>();
        if (breakType != null) wrapper.eq(ChainBreak::getBreakType, breakType);
        if (riskLevel != null) wrapper.eq(ChainBreak::getRiskLevel, riskLevel);
        if (status != null) wrapper.eq(ChainBreak::getStatus, status);
        wrapper.orderByDesc(ChainBreak::getRiskLevel).orderByDesc(ChainBreak::getCreateTime);
        return chainBreakMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), wrapper);
    }

    private static class LambdaQueryWrapper<T> extends com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<T> {}
}
