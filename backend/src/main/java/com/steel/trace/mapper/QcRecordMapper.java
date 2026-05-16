package com.steel.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steel.trace.entity.QcRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface QcRecordMapper extends BaseMapper<QcRecord> {

    @Select("SELECT COALESCE(SUM(CASE WHEN r.result = 1 THEN 1 ELSE 0 END), 0) FROM tr_qc_record r WHERE r.relate_type = 3 AND r.relate_id = #{coilId} AND r.is_deleted = 0")
    Integer countPassByCoilId(@Param("coilId") String coilId);
}
