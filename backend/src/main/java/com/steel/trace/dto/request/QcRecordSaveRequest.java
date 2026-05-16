package com.steel.trace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QcRecordSaveRequest {
    @NotNull(message = "关联类型不能为空")
    private Integer relateType;

    @NotBlank(message = "关联编号不能为空")
    private String relateId;

    private String inspectItem;
    private String inspectValue;
    private String standardValue;

    @NotNull(message = "判定结果不能为空")
    private Integer result;

    private LocalDateTime inspectTime;
    private String inspector;
    private String failReason;
    private String disposeMethod;
}
