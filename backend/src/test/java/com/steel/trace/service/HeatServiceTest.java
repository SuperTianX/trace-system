package com.steel.trace.service;

import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.dto.request.HeatSaveRequest;
import com.steel.trace.entity.Heat;
import com.steel.trace.mapper.HeatMapper;
import com.steel.trace.mapper.SlabMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeatServiceTest {

    @Mock
    private HeatMapper heatMapper;
    @Mock
    private SlabMapper slabMapper;

    private HeatService heatService;

    @BeforeEach
    void setUp() {
        heatService = new HeatService(heatMapper, slabMapper);
    }

    @Test
    void create_shouldSucceed() {
        HeatSaveRequest req = new HeatSaveRequest();
        req.setHeatId("H202605-001");
        req.setSmeltDate(LocalDate.of(2026, 5, 1));
        req.setSteelGrade("Q235B");

        when(heatMapper.selectOne(any())).thenReturn(null);
        when(heatMapper.insert(any())).thenReturn(1);

        Heat result = heatService.create(req);
        assertNotNull(result);
        assertEquals("H202605-001", result.getHeatId());
    }

    @Test
    void create_shouldThrowWhenHeatIdExists() {
        HeatSaveRequest req = new HeatSaveRequest();
        req.setHeatId("H202605-001");
        req.setSmeltDate(LocalDate.of(2026, 5, 1));

        when(heatMapper.selectOne(any())).thenReturn(new Heat());

        assertThrows(BusinessException.class, () -> heatService.create(req));
    }

    @Test
    void getByHeatId_shouldReturnHeat() {
        Heat heat = new Heat();
        heat.setHeatId("H202605-001");
        when(heatMapper.selectOne(any())).thenReturn(heat);

        Heat result = heatService.getByHeatId("H202605-001");
        assertNotNull(result);
        assertEquals("H202605-001", result.getHeatId());
    }

    @Test
    void getByHeatId_shouldThrowWhenNotFound() {
        when(heatMapper.selectOne(any())).thenReturn(null);
        assertThrows(BusinessException.class, () -> heatService.getByHeatId("INVALID"));
    }

    @Test
    void delete_shouldSucceed() {
        Heat heat = new Heat();
        heat.setId(1L);
        heat.setHeatId("H202605-001");
        when(heatMapper.selectOne(any())).thenReturn(heat);
        when(heatMapper.deleteById(anyLong())).thenReturn(1);

        assertDoesNotThrow(() -> heatService.delete("H202605-001"));

        verify(heatMapper).deleteById(1L);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(heatMapper.selectOne(any())).thenReturn(null);
        assertThrows(BusinessException.class, () -> heatService.delete("INVALID"));
    }
}
