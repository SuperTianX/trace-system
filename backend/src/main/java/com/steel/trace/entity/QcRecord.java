package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tr_qc_record")
public class QcRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer relateType;
    private String relateId;
    private String inspectItem;
    private String inspectValue;
    private String standardValue;
    private Integer result;
    private LocalDateTime inspectTime;
    private String inspector;
    private String failReason;
    private String disposeMethod;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
