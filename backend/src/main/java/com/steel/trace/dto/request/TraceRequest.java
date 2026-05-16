package com.steel.trace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TraceRequest {
    @NotBlank(message = "输入类型不能为空")
    private String inputType;  // heat | slab | batch | coil | complaint

    @NotBlank(message = "输入值不能为空")
    private String inputValue;
}
