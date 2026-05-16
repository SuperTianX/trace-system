package com.steel.trace.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoilSaveRequest {
    @NotBlank(message = "卷号不能为空")
    private String coilId;

    private String specifications;
    private BigDecimal weight;
    private String material;
    private String qualityGrade;
    private String batchId;
    private String storageLocation;
}
