package com.steel.trace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WorkReportSaveRequest {
    @NotBlank(message = "工单编号不能为空")
    private String workOrderId;

    private String processName;
    private String batchId;
    private String coilId;

    @NotNull(message = "报工数量不能为空")
    private BigDecimal reportQuantity;

    private LocalDateTime reportTime;
    private String operator;
    private String shiftGroup;
}
