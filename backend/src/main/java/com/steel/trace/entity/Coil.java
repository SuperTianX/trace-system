package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tr_coil")
public class Coil {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String coilId;
    private String specifications;
    private BigDecimal weight;
    private String material;
    private String qualityGrade;
    private String batchId;
    private String inboundOrderNo;
    private String storageLocation;
    private Integer stockStatus;
    private String outboundOrderNo;
    private String customerId;
    private String customerName;
    private Integer lifecycleStatus;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
