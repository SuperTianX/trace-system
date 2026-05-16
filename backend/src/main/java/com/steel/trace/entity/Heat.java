package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tr_heat")
public class Heat {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String heatId;
    private LocalDate smeltDate;
    private String steelGrade;
    private BigDecimal cContent;
    private BigDecimal siContent;
    private BigDecimal mnContent;
    private BigDecimal pContent;
    private BigDecimal sContent;
    private LocalDateTime castStartTime;
    private String shiftGroup;
    private String equipmentId;
    private Integer status;
    private String abnormalDesc;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
