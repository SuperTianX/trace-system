package com.steel.trace.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.steel.trace.common.constant.Constants;
import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.dto.request.TraceRequest;
import com.steel.trace.entity.*;
import com.steel.trace.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraceService {

    private final HeatMapper heatMapper;
    private final SlabMapper slabMapper;
    private final RollBatchMapper rollBatchMapper;
    private final CoilMapper coilMapper;
    private final QcRecordMapper qcRecordMapper;
    private final WorkReportMapper workReportMapper;
    private final InventoryMapper inventoryMapper;
    private final ComplaintMapper complaintMapper;
    private final TraceLinkMapper traceLinkMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> traceForward(TraceRequest request) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, String>> edges = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        String entryType = request.getInputType();
        String entryValue = request.getInputValue();

        switch (entryType) {
            case "heat" -> traceFromHeat(entryValue, nodes, edges, visited);
            case "slab" -> traceFromSlab(entryValue, nodes, edges, visited);
            case "batch" -> traceFromBatch(entryValue, nodes, edges, visited);
            default -> throw new BusinessException("不支持的追溯入口类型: " + entryType);
        }

        long abnormalCount = nodes.stream().filter(n -> !"normal".equals(n.get("status"))).count();
        boolean isComplete = nodes.stream().noneMatch(n -> "abnormal".equals(n.get("status")));

        // 保存链路快照
        saveTraceLink(request.getInputType() + ":" + request.getInputValue(), nodes, edges,
                Constants.TraceType.FORWARD, nodes.size(), (int) abnormalCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inputValue", entryValue);
        result.put("nodes", nodes);
        result.put("edges", edges);
        result.put("isComplete", isComplete);
        result.put("abnormalNodeCount", abnormalCount);
        return result;
    }

    @Transactional
    public Map<String, Object> traceBackward(TraceRequest request) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, String>> edges = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        String entryType = request.getInputType();
        String entryValue = request.getInputValue();

        switch (entryType) {
            case "complaint" -> traceFromComplaint(entryValue, nodes, edges, visited);
            case "coil" -> traceFromCoilBackward(entryValue, nodes, edges, visited);
            case "outbound" -> traceFromOutbound(entryValue, nodes, edges, visited);
            default -> throw new BusinessException("不支持的追溯入口类型: " + entryType);
        }

        long abnormalCount = nodes.stream().filter(n -> !"normal".equals(n.get("status"))).count();

        saveTraceLink(request.getInputType() + ":" + request.getInputValue(), nodes, edges,
                Constants.TraceType.BACKWARD, nodes.size(), (int) abnormalCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inputValue", entryValue);
        result.put("nodes", nodes);
        result.put("edges", edges);
        result.put("abnormalNodeCount", abnormalCount);
        return result;
    }

    private void traceFromHeat(String heatId, List<Map<String, Object>> nodes, List<Map<String, String>> edges, Set<String> visited) {
        if (visited.contains("heat:" + heatId)) return;
        visited.add("heat:" + heatId);

        Heat heat = heatMapper.selectOne(new LambdaQueryWrapper<Heat>().eq(Heat::getHeatId, heatId));
        if (heat == null) {
            addNode(nodes, "heat", heatId, "缺失", "abnormal");
            return;
        }
        addNode(nodes, "heat", heatId, heat, heat.getStatus() == 1 ? "abnormal" : "normal");

        List<Slab> slabs = slabMapper.selectList(new LambdaQueryWrapper<Slab>().eq(Slab::getHeatId, heatId));
        for (Slab slab : slabs) {
            if (!visited.contains("slab:" + slab.getSlabId())) {
                addEdge(edges, heatId, slab.getSlabId());
                traceFromSlab(slab.getSlabId(), nodes, edges, visited);
            }
        }
    }

    private void traceFromSlab(String slabId, List<Map<String, Object>> nodes, List<Map<String, String>> edges, Set<String> visited) {
        if (visited.contains("slab:" + slabId)) return;
        visited.add("slab:" + slabId);

        Slab slab = slabMapper.selectOne(new LambdaQueryWrapper<Slab>().eq(Slab::getSlabId, slabId));
        if (slab == null) {
            addNode(nodes, "slab", slabId, "缺失", "abnormal");
            return;
        }
        String status = slab.getQualityStatus() == 2 ? "abnormal" : "normal";
        addNode(nodes, "slab", slabId, slab, status);

        if (slab.getRollBatchId() != null && !slab.getRollBatchId().isEmpty()) {
            addEdge(edges, slabId, slab.getRollBatchId());
            traceFromBatch(slab.getRollBatchId(), nodes, edges, visited);
        }
    }

    private void traceFromBatch(String batchId, List<Map<String, Object>> nodes, List<Map<String, String>> edges, Set<String> visited) {
        if (visited.contains("batch:" + batchId)) return;
        visited.add("batch:" + batchId);

        RollBatch batch = rollBatchMapper.selectOne(new LambdaQueryWrapper<RollBatch>().eq(RollBatch::getBatchId, batchId));
        if (batch == null) {
            addNode(nodes, "batch", batchId, "缺失", "abnormal");
            return;
        }
        String status = batch.getStatus() == 1 ? "abnormal" : "normal";
        addNode(nodes, "batch", batchId, batch, status);

        List<Coil> coils = coilMapper.selectList(new LambdaQueryWrapper<Coil>().eq(Coil::getBatchId, batchId));
        for (Coil coil : coils) {
            if (!visited.contains("coil:" + coil.getCoilId())) {
                addEdge(edges, batchId, coil.getCoilId());
                traceFromCoilForward(coil.getCoilId(), nodes, edges, visited);
            }
        }
    }

    private void traceFromCoilForward(String coilId, List<Map<String, Object>> nodes, List<Map<String, String>> edges, Set<String> visited) {
        if (visited.contains("coil:" + coilId)) return;
        visited.add("coil:" + coilId);

        Coil coil = coilMapper.selectOne(new LambdaQueryWrapper<Coil>().eq(Coil::getCoilId, coilId));
        if (coil == null) {
            addNode(nodes, "coil", coilId, "缺失", "abnormal");
            return;
        }
        addNode(nodes, "coil", coilId, coil, "normal");

        // 质检记录
        List<QcRecord> qcRecords = qcRecordMapper.selectList(
                new LambdaQueryWrapper<QcRecord>().eq(QcRecord::getRelateId, coilId).eq(QcRecord::getRelateType, 3));
        if (qcRecords.isEmpty()) {
            addNode(nodes, "qc", coilId + "-qc", "质检缺失", "abnormal");
        } else {
            for (QcRecord qc : qcRecords) {
                String nodeId = "qc:" + qc.getId();
                if (!visited.contains(nodeId)) {
                    visited.add(nodeId);
                    String qcStatus = qc.getResult() == 1 ? "normal" : "abnormal";
                    addNode(nodes, "qc", nodeId, qc, qcStatus);
                    addEdge(edges, coilId, nodeId);
                }
            }
        }

        // 入库
        List<Inventory> inbounds = inventoryMapper.selectList(
                new LambdaQueryWrapper<Inventory>().eq(Inventory::getCoilId, coilId).eq(Inventory::getDocType, 1));
        for (Inventory inv : inbounds) {
            String nodeId = "inbound:" + inv.getDocNo();
            if (!visited.contains(nodeId)) {
                visited.add(nodeId);
                addNode(nodes, "inbound", nodeId, inv, "normal");
                addEdge(edges, coilId, nodeId);
            }
        }

        // 出库
        List<Inventory> outbounds = inventoryMapper.selectList(
                new LambdaQueryWrapper<Inventory>().eq(Inventory::getCoilId, coilId).eq(Inventory::getDocType, 2));
        for (Inventory inv : outbounds) {
            String nodeId = "outbound:" + inv.getDocNo();
            if (!visited.contains(nodeId)) {
                visited.add(nodeId);
                addNode(nodes, "outbound", nodeId, inv, "normal");
                addEdge(edges, coilId, nodeId);
            }
        }
    }

    private void traceFromCoilBackward(String coilId, List<Map<String, Object>> nodes, List<Map<String, String>> edges, Set<String> visited) {
        if (visited.contains("coil:" + coilId)) return;
        visited.add("coil:" + coilId);

        Coil coil = coilMapper.selectOne(new LambdaQueryWrapper<Coil>().eq(Coil::getCoilId, coilId));
        if (coil == null) {
            addNode(nodes, "coil", coilId, "缺失", "abnormal");
            return;
        }
        addNode(nodes, "coil", coilId, coil, "normal");

        // 反向查批次
        if (coil.getBatchId() != null) {
            addEdge(edges, coil.getBatchId(), coilId);
            traceFromBatchBackward(coil.getBatchId(), nodes, edges, visited);
        }
    }

    private void traceFromBatchBackward(String batchId, List<Map<String, Object>> nodes, List<Map<String, String>> edges, Set<String> visited) {
        if (visited.contains("batch:" + batchId)) return;
        visited.add("batch:" + batchId);

        RollBatch batch = rollBatchMapper.selectOne(new LambdaQueryWrapper<RollBatch>().eq(RollBatch::getBatchId, batchId));
        if (batch == null) {
            addNode(nodes, "batch", batchId, "缺失", "abnormal");
            return;
        }
        addNode(nodes, "batch", batchId, batch, "normal");

        List<Slab> slabs = slabMapper.selectList(new LambdaQueryWrapper<Slab>().eq(Slab::getRollBatchId, batchId));
        for (Slab slab : slabs) {
            addEdge(edges, slab.getSlabId(), batchId);
            traceFromSlabBackward(slab.getSlabId(), nodes, edges, visited);
        }
    }

    private void traceFromSlabBackward(String slabId, List<Map<String, Object>> nodes, List<Map<String, String>> edges, Set<String> visited) {
        if (visited.contains("slab:" + slabId)) return;
        visited.add("slab:" + slabId);

        Slab slab = slabMapper.selectOne(new LambdaQueryWrapper<Slab>().eq(Slab::getSlabId, slabId));
        if (slab == null) {
            addNode(nodes, "slab", slabId, "缺失", "abnormal");
            return;
        }
        addNode(nodes, "slab", slabId, slab, "normal");

        if (slab.getHeatId() != null) {
            addEdge(edges, slab.getHeatId(), slabId);
            traceFromHeatBackward(slab.getHeatId(), nodes, edges, visited);
        }
    }

    private void traceFromHeatBackward(String heatId, List<Map<String, Object>> nodes, List<Map<String, String>> edges, Set<String> visited) {
        if (visited.contains("heat:" + heatId)) return;
        visited.add("heat:" + heatId);

        Heat heat = heatMapper.selectOne(new LambdaQueryWrapper<Heat>().eq(Heat::getHeatId, heatId));
        if (heat == null) {
            addNode(nodes, "heat", heatId, "缺失", "abnormal");
            return;
        }
        addNode(nodes, "heat", heatId, heat, heat.getStatus() == 1 ? "abnormal" : "normal");
    }

    private void traceFromComplaint(String complaintId, List<Map<String, Object>> nodes, List<Map<String, String>> edges, Set<String> visited) {
        Complaint complaint = complaintMapper.selectOne(
                new LambdaQueryWrapper<Complaint>().eq(Complaint::getComplaintId, complaintId));
        if (complaint == null) {
            addNode(nodes, "complaint", complaintId, "缺失", "abnormal");
            return;
        }
        addNode(nodes, "complaint", complaintId, complaint, "normal");

        if (complaint.getCoilId() != null) {
            addEdge(edges, complaintId, complaint.getCoilId());
            traceFromCoilBackward(complaint.getCoilId(), nodes, edges, visited);
        }
    }

    private void traceFromOutbound(String outboundDocNo, List<Map<String, Object>> nodes, List<Map<String, String>> edges, Set<String> visited) {
        Inventory inv = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>().eq(Inventory::getDocNo, outboundDocNo).eq(Inventory::getDocType, 2));
        if (inv == null) {
            addNode(nodes, "outbound", outboundDocNo, "缺失", "abnormal");
            return;
        }
        addNode(nodes, "outbound", outboundDocNo, inv, "normal");
        if (inv.getCoilId() != null) {
            addEdge(edges, outboundDocNo, inv.getCoilId());
            traceFromCoilBackward(inv.getCoilId(), nodes, edges, visited);
        }
    }

    private void addNode(List<Map<String, Object>> nodes, String type, String id, Object data, String status) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("type", type);
        node.put("id", id);
        node.put("data", data);
        node.put("status", status);
        nodes.add(node);
    }

    private void addEdge(List<Map<String, String>> edges, String from, String to) {
        Map<String, String> edge = new LinkedHashMap<>();
        edge.put("from", from);
        edge.put("to", to);
        edge.put("relation", "关联");
        edges.add(edge);
    }

    private void saveTraceLink(String inputValue, List<Map<String, Object>> nodes,
                                List<Map<String, String>> edges, int type, int nodeCount, int abnormalCount) {
        try {
            Map<String, Object> linkData = new LinkedHashMap<>();
            linkData.put("nodes", nodes);
            linkData.put("edges", edges);

            TraceLink traceLink = new TraceLink();
            traceLink.setLinkId("TL-" + System.currentTimeMillis());
            traceLink.setSourceType(type);
            traceLink.setInputValue(inputValue);
            traceLink.setLinkData(objectMapper.writeValueAsString(linkData));
            traceLink.setNodeCount(nodeCount);
            traceLink.setAbnormalNodeCount(abnormalCount);
            traceLink.setCreateBy("system");
            traceLinkMapper.insert(traceLink);
        } catch (JsonProcessingException e) {
            log.error("保存链路快照失败", e);
        }
    }
}
