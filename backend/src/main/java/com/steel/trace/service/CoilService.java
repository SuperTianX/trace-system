package com.steel.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.dto.request.CoilSaveRequest;
import com.steel.trace.entity.Coil;
import com.steel.trace.mapper.CoilMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoilService {

    private final CoilMapper coilMapper;

    @Transactional
    public Coil create(CoilSaveRequest request) {
        if (coilMapper.selectOne(new LambdaQueryWrapper<Coil>().eq(Coil::getCoilId, request.getCoilId())) != null) {
            throw new BusinessException("卷号已存在");
        }
        Coil coil = new Coil();
        coil.setCoilId(request.getCoilId());
        coil.setSpecifications(request.getSpecifications());
        coil.setWeight(request.getWeight());
        coil.setMaterial(request.getMaterial());
        coil.setQualityGrade(request.getQualityGrade());
        coil.setBatchId(request.getBatchId());
        coil.setStorageLocation(request.getStorageLocation());
        coil.setStockStatus(0);
        coil.setLifecycleStatus(0);
        coilMapper.insert(coil);
        return coil;
    }

    public Coil getByCoilId(String coilId) {
        Coil coil = coilMapper.selectOne(new LambdaQueryWrapper<Coil>().eq(Coil::getCoilId, coilId));
        if (coil == null) throw new BusinessException("卷号不存在");
        return coil;
    }

    public Page<Coil> pageQuery(int page, int size, String coilId, String batchId, Integer stockStatus, Integer lifecycleStatus) {
        LambdaQueryWrapper<Coil> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(coilId)) wrapper.like(Coil::getCoilId, coilId);
        if (StringUtils.hasText(batchId)) wrapper.eq(Coil::getBatchId, batchId);
        if (stockStatus != null) wrapper.eq(Coil::getStockStatus, stockStatus);
        if (lifecycleStatus != null) wrapper.eq(Coil::getLifecycleStatus, lifecycleStatus);
        wrapper.orderByDesc(Coil::getId);
        return coilMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public List<Coil> getByBatchId(String batchId) {
        return coilMapper.selectList(new LambdaQueryWrapper<Coil>().eq(Coil::getBatchId, batchId));
    }

    @Transactional
    public void updateLifecycleStatus(String coilId, int lifecycleStatus) {
        Coil coil = getByCoilId(coilId);
        coil.setLifecycleStatus(lifecycleStatus);
        coilMapper.updateById(coil);
    }

    @Transactional
    public void delete(String coilId) {
        Coil coil = getByCoilId(coilId);
        coilMapper.deleteById(coil.getId());
    }
}
