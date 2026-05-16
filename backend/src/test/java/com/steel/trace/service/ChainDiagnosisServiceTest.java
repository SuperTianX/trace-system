package com.steel.trace.service;

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
class ChainDiagnosisServiceTest {

    @Mock private HeatMapper heatMapper;
    @Mock private SlabMapper slabMapper;
    @Mock private RollBatchMapper rollBatchMapper;
    @Mock private CoilMapper coilMapper;
    @Mock private QcRecordMapper qcRecordMapper;
    @Mock private WorkReportMapper workReportMapper;
    @Mock private InventoryMapper inventoryMapper;
    @Mock private ChainBreakMapper chainBreakMapper;

    private ChainDiagnosisService chainDiagnosisService;

    @BeforeEach
    void setUp() {
        chainDiagnosisService = new ChainDiagnosisService(heatMapper, slabMapper, rollBatchMapper,
                coilMapper, qcRecordMapper, workReportMapper, inventoryMapper, chainBreakMapper);
    }

    @Test
    void execute_shouldRunDiagnosis() {
        when(slabMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(coilMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(rollBatchMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(heatMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(workReportMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(qcRecordMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(inventoryMapper.selectList(any())).thenReturn(new ArrayList<>());

        List<ChainBreak> result = chainDiagnosisService.execute();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void execute_shouldDetectOrphanSlabs() {
        Slab orphanSlab = new Slab();
        orphanSlab.setSlabId("S202605-001");
        orphanSlab.setHeatId("H202605-001");

        when(slabMapper.selectList(any())).thenReturn(Collections.singletonList(orphanSlab));
        when(coilMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(rollBatchMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(heatMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(workReportMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(qcRecordMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(inventoryMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(chainBreakMapper.insert(any())).thenReturn(1);

        List<ChainBreak> result = chainDiagnosisService.execute();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("S202605-001", result.get(0).getSlabId());
    }

    @Test
    void execute_shouldDetectOrphanCoils() {
        // Mock empty slabs list
        when(slabMapper.selectList(any())).thenReturn(new ArrayList<>());

        // Mock orphan coil with batch not in roll_batch table
        Coil orphanCoil = new Coil();
        orphanCoil.setCoilId("C202605-001");
        orphanCoil.setBatchId("B202605-001");

        when(coilMapper.selectList(any())).thenReturn(Collections.singletonList(orphanCoil));
        when(rollBatchMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(heatMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(workReportMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(qcRecordMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(inventoryMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(chainBreakMapper.insert(any())).thenReturn(1);

        List<ChainBreak> result = chainDiagnosisService.execute();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void assign_shouldUpdateStatus() {
        ChainBreak cb = new ChainBreak();
        cb.setId(1L);
        when(chainBreakMapper.selectById(1L)).thenReturn(cb);
        when(chainBreakMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> chainDiagnosisService.assign(1L, "质检部"));
    }

    @Test
    void close_shouldUpdateStatus() {
        ChainBreak cb = new ChainBreak();
        cb.setId(1L);
        when(chainBreakMapper.selectById(1L)).thenReturn(cb);
        when(chainBreakMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> chainDiagnosisService.close(1L));
    }
}
