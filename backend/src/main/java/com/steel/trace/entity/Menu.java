package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("tr_menu")
public class Menu {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String menuName;
    private Long parentId;
    private String path;
    private String permissionCode;
    private Integer sortOrder;
}
