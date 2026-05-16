package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tr_chain_break")
public class ChainBreak {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer breakType;
    private String heatId;
    private String slabId;
    private String batchId;
    private String coilId;
    private String breakDesc;
    private Integer riskLevel;
    private String responsibleDept;
    private String remark;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
