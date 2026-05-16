package com.steel.trace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.steel.trace.dto.request.TraceRequest;
import com.steel.trace.entity.*;
import com.steel.trace.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TraceServiceTest {

    @Mock private HeatMapper heatMapper;
    @Mock private SlabMapper slabMapper;
    @Mock private RollBatchMapper rollBatchMapper;
    @Mock private CoilMapper coilMapper;
    @Mock private QcRecordMapper qcRecordMapper;
    @Mock private WorkReportMapper workReportMapper;
    @Mock private InventoryMapper inventoryMapper;
    @Mock private ComplaintMapper complaintMapper;
    @Mock private TraceLinkMapper traceLinkMapper;

    private TraceService traceService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        traceService = new TraceService(heatMapper, slabMapper, rollBatchMapper,
                coilMapper, qcRecordMapper, workReportMapper, inventoryMapper,
                complaintMapper, traceLinkMapper, objectMapper);
    }

    @Test
    void traceForward_fromHeat_shouldReturnCompleteChain() {
        Heat heat = new Heat();
        heat.setHeatId("H202605-001");
        heat.setStatus(0);

        Slab slab = new Slab();
        slab.setSlabId("S202605-001");
        slab.setHeatId("H202605-001");
        slab.setRollBatchId("B202605-001");
        slab.setQualityStatus(1);

        RollBatch batch = new RollBatch();
        batch.setBatchId("B202605-001");
        batch.setStatus(0);

        Coil coil = new Coil();
        coil.setCoilId("C202605-001");
        coil.setBatchId("B202605-001");

        when(heatMapper.selectOne(any())).thenReturn(heat);
        when(slabMapper.selectList(any())).thenReturn(Collections.singletonList(slab));
        when(rollBatchMapper.selectOne(any())).thenReturn(batch);
        when(coilMapper.selectList(any())).thenReturn(Collections.singletonList(coil));
        when(qcRecordMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(inventoryMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(traceLinkMapper.insert(any())).thenReturn(1);

        TraceRequest request = new TraceRequest();
        request.setInputType("heat");
        request.setInputValue("H202605-001");

        Map<String, Object> result = traceService.traceForward(request);
        assertNotNull(result);
        assertTrue(result.containsKey("nodes"));
        assertTrue(result.containsKey("edges"));
    }

    @Test
    void traceForward_invalidHeat_shouldReturnAbnormal() {
        when(heatMapper.selectOne(any())).thenReturn(null);
        when(traceLinkMapper.insert(any())).thenReturn(1);

        TraceRequest request = new TraceRequest();
        request.setInputType("heat");
        request.setInputValue("INVALID");

        Map<String, Object> result = traceService.traceForward(request);
        assertNotNull(result);
        assertTrue(result.containsKey("nodes"));
        assertTrue(result.containsKey("edges"));
    }

    @Test
    void traceBackward_fromCoil_shouldReturnChain() {
        Coil coil = new Coil();
        coil.setCoilId("C202605-001");
        coil.setBatchId("B202605-001");

        RollBatch batch = new RollBatch();
        batch.setBatchId("B202605-001");
        batch.setStatus(0);

        Slab slab = new Slab();
        slab.setSlabId("S202605-001");
        slab.setHeatId("H202605-001");
        slab.setRollBatchId("B202605-001");

        Heat heat = new Heat();
        heat.setHeatId("H202605-001");
        heat.setStatus(0);

        when(coilMapper.selectOne(any())).thenReturn(coil);
        when(rollBatchMapper.selectOne(any())).thenReturn(batch);
        when(slabMapper.selectList(any())).thenReturn(Collections.singletonList(slab));
        when(heatMapper.selectOne(any())).thenReturn(heat);
        when(traceLinkMapper.insert(any())).thenReturn(1);

        TraceRequest request = new TraceRequest();
        request.setInputType("coil");
        request.setInputValue("C202605-001");

        Map<String, Object> result = traceService.traceBackward(request);
        assertNotNull(result);
        assertTrue(result.containsKey("nodes"));
        assertTrue(result.containsKey("edges"));
    }

    @Test
    void traceForward_fromSlab_shouldReturnChain() {
        Slab slab = new Slab();
        slab.setSlabId("S202605-001");
        slab.setHeatId("H202605-001");
        slab.setRollBatchId("B202605-001");
        slab.setQualityStatus(1);

        Heat heat = new Heat();
        heat.setHeatId("H202605-001");
        heat.setStatus(0);

        RollBatch batch = new RollBatch();
        batch.setBatchId("B202605-001");
        batch.setStatus(0);

        Coil coil = new Coil();
        coil.setCoilId("C202605-001");
        coil.setBatchId("B202605-001");

        when(slabMapper.selectOne(any())).thenReturn(slab);
        when(heatMapper.selectOne(any())).thenReturn(heat);
        when(rollBatchMapper.selectOne(any())).thenReturn(batch);
        when(coilMapper.selectList(any())).thenReturn(Collections.singletonList(coil));
        when(qcRecordMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(inventoryMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(traceLinkMapper.insert(any())).thenReturn(1);

        TraceRequest request = new TraceRequest();
        request.setInputType("slab");
        request.setInputValue("S202605-001");

        Map<String, Object> result = traceService.traceForward(request);
        assertNotNull(result);
        assertTrue(result.containsKey("nodes"));
    }
}
