package com.steel.trace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComplaintSaveRequest {
    @NotBlank(message = "投诉单号不能为空")
    private String complaintId;

    private String customerId;
    private String customerName;

    @NotBlank(message = "关联卷号不能为空")
    private String coilId;

    @NotBlank(message = "问题描述不能为空")
    private String problemDesc;

    @NotNull(message = "严重度不能为空")
    private Integer severity;
}
