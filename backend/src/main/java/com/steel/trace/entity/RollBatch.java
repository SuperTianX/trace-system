package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tr_roll_batch")
public class RollBatch {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchId;
    private String productionLine;
    private String workOrderId;
    private LocalDate rollDate;
    private String shiftGroup;
    private Integer status;
    private String abnormalDesc;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
