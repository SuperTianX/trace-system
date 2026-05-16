package com.steel.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.trace.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {

    @Select("SELECT COALESCE(SUM(quantity), 0) FROM tr_stock WHERE stock_status = 0")
    BigDecimal sumAllInStock();
}
