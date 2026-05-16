package com.steel.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.trace.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Select("SELECT COALESCE(SUM(quantity), 0) FROM tr_inventory WHERE doc_type = 1 AND coil_id = #{coilId} AND is_deleted = 0 AND status = 1")
    BigDecimal sumInboundQtyByCoilId(@Param("coilId") String coilId);
}
