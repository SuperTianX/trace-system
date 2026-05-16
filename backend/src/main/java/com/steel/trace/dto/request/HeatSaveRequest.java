package com.steel.trace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HeatSaveRequest {
    @NotBlank(message = "炉号不能为空")
    private String heatId;

    @NotNull(message = "冶炼日期不能为空")
    private LocalDate smeltDate;

    private String steelGrade;
    private BigDecimal cContent;
    private BigDecimal siContent;
    private BigDecimal mnContent;
    private BigDecimal pContent;
    private BigDecimal sContent;
    private LocalDateTime castStartTime;
    private String shiftGroup;
    private String equipmentId;
    private Integer status;
    private String abnormalDesc;
}
