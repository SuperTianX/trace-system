package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tr_rule_config")
public class RuleConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String ruleCode;
    private String ruleName;
    private Integer ruleType;
    private String ruleContent;
    private Integer isEnabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
