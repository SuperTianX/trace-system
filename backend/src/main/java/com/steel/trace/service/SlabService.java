package com.steel.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.dto.request.SlabSaveRequest;
import com.steel.trace.entity.Heat;
import com.steel.trace.entity.Slab;
import com.steel.trace.mapper.HeatMapper;
import com.steel.trace.mapper.SlabMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SlabService {

    private final SlabMapper slabMapper;
    private final HeatMapper heatMapper;

    @Transactional
    public Slab create(SlabSaveRequest request) {
        if (slabMapper.selectOne(new LambdaQueryWrapper<Slab>().eq(Slab::getSlabId, request.getSlabId())) != null) {
            throw new BusinessException("铸坯号已存在");
        }
        Heat heat = heatMapper.selectOne(new LambdaQueryWrapper<Heat>().eq(Heat::getHeatId, request.getHeatId()));
        if (heat == null) {
            throw new BusinessException("关联炉号不存在");
        }
        Slab slab = new Slab();
        slab.setSlabId(request.getSlabId());
        slab.setHeatId(request.getHeatId());
        slab.setSpecifications(request.getSpecifications());
        slab.setWeight(request.getWeight());
        slab.setCastShift(request.getCastShift());
        slab.setCastTime(request.getCastTime());
        slab.setRollBatchId(request.getRollBatchId());
        slab.setQualityStatus(request.getQualityStatus() != null ? request.getQualityStatus() : 0);
        slabMapper.insert(slab);
        return slab;
    }

    @Transactional
    public void updateStatus(String slabId, Integer status) {
        Slab slab = slabMapper.selectOne(new LambdaQueryWrapper<Slab>().eq(Slab::getSlabId, slabId));
        if (slab == null) {
            throw new BusinessException("铸坯不存在");
        }
        slab.setQualityStatus(status);
        slabMapper.updateById(slab);
    }

    public Slab getBySlabId(String slabId) {
        Slab slab = slabMapper.selectOne(new LambdaQueryWrapper<Slab>().eq(Slab::getSlabId, slabId));
        if (slab == null) {
            throw new BusinessException("铸坯不存在");
        }
        return slab;
    }

    public Page<Slab> pageQuery(int page, int size, String heatId, String slabId, Integer qualityStatus) {
        LambdaQueryWrapper<Slab> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(heatId)) wrapper.eq(Slab::getHeatId, heatId);
        if (StringUtils.hasText(slabId)) wrapper.like(Slab::getSlabId, slabId);
        if (qualityStatus != null) wrapper.eq(Slab::getQualityStatus, qualityStatus);
        wrapper.orderByDesc(Slab::getCastTime);
        return slabMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public List<Slab> listAll() {
        return slabMapper.selectList(new LambdaQueryWrapper<Slab>().orderByDesc(Slab::getCastTime));
    }

    public List<Slab> getByHeatId(String heatId) {
        return slabMapper.selectList(new LambdaQueryWrapper<Slab>().eq(Slab::getHeatId, heatId));
    }

    public List<Slab> getByRollBatchId(String batchId) {
        return slabMapper.selectList(new LambdaQueryWrapper<Slab>().eq(Slab::getRollBatchId, batchId));
    }

    @Transactional
    public void delete(String slabId) {
        Slab slab = getBySlabId(slabId);
        slabMapper.deleteById(slab.getId());
    }
}
