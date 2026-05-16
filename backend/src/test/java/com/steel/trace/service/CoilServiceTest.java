package com.steel.trace.service;

import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.dto.request.CoilSaveRequest;
import com.steel.trace.entity.Coil;
import com.steel.trace.mapper.CoilMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoilServiceTest {

    @Mock private CoilMapper coilMapper;
    private CoilService coilService;

    @BeforeEach
    void setUp() {
        coilService = new CoilService(coilMapper);
    }

    @Test
    void create_shouldSucceed() {
        CoilSaveRequest req = new CoilSaveRequest();
        req.setCoilId("C202605-001");

        when(coilMapper.selectOne(any())).thenReturn(null);
        when(coilMapper.insert(any())).thenReturn(1);

        Coil result = coilService.create(req);
        assertNotNull(result);
        assertEquals("C202605-001", result.getCoilId());
    }

    @Test
    void create_shouldThrowOnDuplicate() {
        CoilSaveRequest req = new CoilSaveRequest();
        req.setCoilId("C202605-001");

        when(coilMapper.selectOne(any())).thenReturn(new Coil());

        assertThrows(BusinessException.class, () -> coilService.create(req));
    }

    @Test
    void getByCoilId_shouldReturnCoil() {
        Coil coil = new Coil();
        coil.setCoilId("C202605-001");
        when(coilMapper.selectOne(any())).thenReturn(coil);

        Coil result = coilService.getByCoilId("C202605-001");
        assertNotNull(result);
        assertEquals("C202605-001", result.getCoilId());
    }

    @Test
    void getByCoilId_shouldThrowWhenNotFound() {
        when(coilMapper.selectOne(any())).thenReturn(null);
        assertThrows(BusinessException.class, () -> coilService.getByCoilId("INVALID"));
    }

    @Test
    void getByBatchId_shouldReturnList() {
        Coil coil = new Coil();
        coil.setCoilId("C202605-001");
        coil.setBatchId("B202605-001");

        when(coilMapper.selectList(any())).thenReturn(java.util.Collections.singletonList(coil));

        var result = coilService.getByBatchId("B202605-001");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void delete_shouldSucceed() {
        Coil coil = new Coil();
        coil.setId(1L);
        coil.setCoilId("C202605-001");
        when(coilMapper.selectOne(any())).thenReturn(coil);
        when(coilMapper.deleteById(anyLong())).thenReturn(1);

        assertDoesNotThrow(() -> coilService.delete("C202605-001"));
        verify(coilMapper).deleteById(1L);
    }
}
