package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tr_stock")
public class Stock {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String coilId;
    private String warehouse;
    private String location;
    private BigDecimal quantity;
    private Integer stockStatus;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
