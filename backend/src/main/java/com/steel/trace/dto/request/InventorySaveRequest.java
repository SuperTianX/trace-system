package com.steel.trace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventorySaveRequest {
    @NotBlank(message = "单据编号不能为空")
    private String docNo;

    @NotNull(message = "单据类型不能为空")
    private Integer docType;

    private String batchId;

    @NotBlank(message = "卷号不能为空")
    private String coilId;

    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    private LocalDateTime operateTime;
    private String warehouse;
    private String operator;
}
