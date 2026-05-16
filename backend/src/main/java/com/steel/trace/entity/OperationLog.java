package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tr_operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String ip;
    private String module;
    private String action;
    private String description;
    private String requestUrl;
    private String requestMethod;
    private String requestParams;
    private String responseResult;
    private Long executeTime;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
