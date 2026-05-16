package com.steel.trace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SlabSaveRequest {
    @NotBlank(message = "铸坯号不能为空")
    private String slabId;

    @NotBlank(message = "关联炉号不能为空")
    private String heatId;

    private String specifications;
    private BigDecimal weight;
    private String castShift;
    private LocalDateTime castTime;
    private String rollBatchId;
    private Integer qualityStatus;
}
