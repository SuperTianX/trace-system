package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tr_inventory")
public class Inventory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String docNo;
    private Integer docType;
    private String batchId;
    private String coilId;
    private BigDecimal quantity;
    private LocalDateTime operateTime;
    private String warehouse;
    private String operator;
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
