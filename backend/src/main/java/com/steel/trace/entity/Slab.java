package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tr_slab")
public class Slab {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String slabId;
    private String heatId;
    private String specifications;
    private BigDecimal weight;
    private String castShift;
    private LocalDateTime castTime;
    private String rollBatchId;
    private Integer qualityStatus;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
