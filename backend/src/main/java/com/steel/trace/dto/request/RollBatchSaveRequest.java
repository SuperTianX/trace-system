package com.steel.trace.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RollBatchSaveRequest {
    @NotBlank(message = "批次号不能为空")
    private String batchId;

    private String productionLine;
    private String workOrderId;
    private LocalDate rollDate;
    private String shiftGroup;
    private Integer status;
    private String abnormalDesc;
}
