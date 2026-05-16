package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tr_work_report")
public class WorkReport {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String workOrderId;
    private String processName;
    private String batchId;
    private String coilId;
    private BigDecimal reportQuantity;
    private LocalDateTime reportTime;
    private String operator;
    private String shiftGroup;
    private Integer approveStatus;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
