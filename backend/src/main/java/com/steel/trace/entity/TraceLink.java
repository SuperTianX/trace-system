package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tr_trace_link")
public class TraceLink {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String linkId;
    private Integer sourceType;
    private String inputValue;
    private String linkData;
    private Integer nodeCount;
    private Integer abnormalNodeCount;
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
