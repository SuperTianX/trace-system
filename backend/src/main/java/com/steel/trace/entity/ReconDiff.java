package com.steel.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tr_recon_diff")
public class ReconDiff {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchId;
    private String coilId;
    private BigDecimal workReportQty;
    private BigDecimal qcPassQty;
    private BigDecimal erpInboundQty;
    private BigDecimal stockQty;
    private Integer diffType;
    private String description;
    private String responsibleDept;
    private String remark;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public ReconDiff(String batchId, String coilId, BigDecimal workReportQty, BigDecimal qcPassQty,
                     BigDecimal erpInboundQty, BigDecimal stockQty, Integer diffType, String description) {
        this.batchId = batchId;
        this.coilId = coilId;
        this.workReportQty = workReportQty;
        this.qcPassQty = qcPassQty;
        this.erpInboundQty = erpInboundQty;
        this.stockQty = stockQty;
        this.diffType = diffType;
        this.description = description;
        this.status = 0;
    }
}
