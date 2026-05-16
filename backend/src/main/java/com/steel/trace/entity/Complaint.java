package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tr_complaint")
public class Complaint {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String complaintId;
    private String customerId;
    private String customerName;
    private String coilId;
    private String problemDesc;
    private Integer severity;
    private String responsibleDept;
    private String rootCause;
    private String correctiveMeasures;
    private String rectificationResult;
    private Integer status;
    private LocalDateTime closeTime;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
