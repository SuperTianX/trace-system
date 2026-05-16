package com.steel.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.dto.request.HeatPageQuery;
import com.steel.trace.dto.request.HeatSaveRequest;
import com.steel.trace.entity.Heat;
import com.steel.trace.mapper.HeatMapper;
import com.steel.trace.mapper.SlabMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeatService {

    private final HeatMapper heatMapper;
    private final SlabMapper slabMapper;

    @Transactional
    public Heat create(HeatSaveRequest request) {
        if (heatMapper.selectOne(new LambdaQueryWrapper<Heat>().eq(Heat::getHeatId, request.getHeatId())) != null) {
            throw new BusinessException("炉号已存在");
        }
        Heat heat = new Heat();
        heat.setHeatId(request.getHeatId());
        heat.setSmeltDate(request.getSmeltDate());
        heat.setSteelGrade(request.getSteelGrade());
        heat.setCContent(request.getCContent());
        heat.setSiContent(request.getSiContent());
        heat.setMnContent(request.getMnContent());
        heat.setPContent(request.getPContent());
        heat.setSContent(request.getSContent());
        heat.setCastStartTime(request.getCastStartTime());
        heat.setShiftGroup(request.getShiftGroup());
        heat.setEquipmentId(request.getEquipmentId());
        heat.setStatus(request.getStatus() != null ? request.getStatus() : 0);
        heat.setAbnormalDesc(request.getAbnormalDesc());
        heatMapper.insert(heat);
        return heat;
    }

    @Transactional
    public Heat update(String heatId, HeatSaveRequest request) {
        Heat heat = heatMapper.selectOne(new LambdaQueryWrapper<Heat>().eq(Heat::getHeatId, heatId));
        if (heat == null) {
            throw new BusinessException("炉次不存在");
        }
        heat.setSmeltDate(request.getSmeltDate());
        heat.setSteelGrade(request.getSteelGrade());
        heat.setCContent(request.getCContent());
        heat.setSiContent(request.getSiContent());
        heat.setMnContent(request.getMnContent());
        heat.setPContent(request.getPContent());
        heat.setSContent(request.getSContent());
        heat.setCastStartTime(request.getCastStartTime());
        heat.setShiftGroup(request.getShiftGroup());
        heat.setEquipmentId(request.getEquipmentId());
        heat.setStatus(request.getStatus());
        heat.setAbnormalDesc(request.getAbnormalDesc());
        heatMapper.updateById(heat);
        return heat;
    }

    public Heat getByHeatId(String heatId) {
        Heat heat = heatMapper.selectOne(new LambdaQueryWrapper<Heat>().eq(Heat::getHeatId, heatId));
        if (heat == null) {
            throw new BusinessException("炉次不存在");
        }
        return heat;
    }

    public Page<Heat> pageQuery(HeatPageQuery query) {
        LambdaQueryWrapper<Heat> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getHeatId())) {
            wrapper.like(Heat::getHeatId, query.getHeatId());
        }
        if (StringUtils.hasText(query.getSteelGrade())) {
            wrapper.like(Heat::getSteelGrade, query.getSteelGrade());
        }
        if (query.getSmeltDateStart() != null) {
            wrapper.ge(Heat::getSmeltDate, query.getSmeltDateStart());
        }
        if (query.getSmeltDateEnd() != null) {
            wrapper.le(Heat::getSmeltDate, query.getSmeltDateEnd());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Heat::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Heat::getSmeltDate);
        return heatMapper.selectPage(new Page<>(query.getPage(), query.getSize()), wrapper);
    }

    public List<Heat> listAll() {
        return heatMapper.selectList(new LambdaQueryWrapper<Heat>().orderByDesc(Heat::getSmeltDate));
    }

    @Transactional
    public void delete(String heatId) {
        Heat heat = getByHeatId(heatId);
        heatMapper.deleteById(heat.getId());
    }

    @Transactional
    public void batchImport(List<HeatSaveRequest> requests) {
        for (HeatSaveRequest req : requests) {
            create(req);
        }
    }
}
