package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("tr_role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String roleCode;
    private String roleName;
    private String description;
    private Integer status;
}
