package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("tr_user_role")
public class UserRole {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long roleId;
}
