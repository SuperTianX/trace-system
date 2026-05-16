package com.steel.trace.service;

import com.steel.trace.entity.*;
import com.steel.trace.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    @Mock private WorkReportMapper workReportMapper;
    @Mock private QcRecordMapper qcRecordMapper;
    @Mock private InventoryMapper inventoryMapper;
    @Mock private StockMapper stockMapper;
    @Mock private ReconDiffMapper reconDiffMapper;
    @Mock private CoilMapper coilMapper;

    private ReconciliationService reconciliationService;

    @BeforeEach
    void setUp() {
        reconciliationService = new ReconciliationService(workReportMapper, qcRecordMapper,
                inventoryMapper, stockMapper, reconDiffMapper, coilMapper);
    }

    @Test
    void execute_shouldDetectMismatch() {
        // Mock work report data
        WorkReport wr = new WorkReport();
        wr.setBatchId("B202605-001");
        wr.setReportQuantity(BigDecimal.valueOf(100));
        wr.setApproveStatus(1);
        when(workReportMapper.selectList(any())).thenReturn(Collections.singletonList(wr));

        // Mock coil data for the batch
        Coil coil = new Coil();
        coil.setCoilId("C202605-001");
        coil.setBatchId("B202605-001");
        coil.setLifecycleStatus(0);
        when(coilMapper.selectList(any())).thenReturn(Collections.singletonList(coil));

        // Mock no qc pass, no inventory, no stock
        when(qcRecordMapper.countPassByCoilId(anyString())).thenReturn(0);
        when(inventoryMapper.sumInboundQtyByCoilId(anyString())).thenReturn(BigDecimal.ZERO);
        when(stockMapper.selectOne(any())).thenReturn(null);
        when(reconDiffMapper.insert(any())).thenReturn(1);

        List<ReconDiff> result = reconciliationService.execute();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void execute_shouldReturnEmptyWhenNoMismatch() {
        // Mock no work reports
        when(workReportMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(coilMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(inventoryMapper.selectList(any())).thenReturn(new ArrayList<>());

        List<ReconDiff> result = reconciliationService.execute();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
