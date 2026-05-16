package com.steel.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.dto.request.RollBatchSaveRequest;
import com.steel.trace.entity.RollBatch;
import com.steel.trace.entity.Slab;
import com.steel.trace.mapper.RollBatchMapper;
import com.steel.trace.mapper.SlabMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RollBatchService {

    private final RollBatchMapper rollBatchMapper;
    private final SlabMapper slabMapper;

    @Transactional
    public RollBatch create(RollBatchSaveRequest request) {
        if (rollBatchMapper.selectOne(new LambdaQueryWrapper<RollBatch>().eq(RollBatch::getBatchId, request.getBatchId())) != null) {
            throw new BusinessException("批次号已存在");
        }
        RollBatch batch = new RollBatch();
        batch.setBatchId(request.getBatchId());
        batch.setProductionLine(request.getProductionLine());
        batch.setWorkOrderId(request.getWorkOrderId());
        batch.setRollDate(request.getRollDate());
        batch.setShiftGroup(request.getShiftGroup());
        batch.setStatus(request.getStatus() != null ? request.getStatus() : 0);
        batch.setAbnormalDesc(request.getAbnormalDesc());
        rollBatchMapper.insert(batch);
        return batch;
    }

    public RollBatch getByBatchId(String batchId) {
        RollBatch batch = rollBatchMapper.selectOne(new LambdaQueryWrapper<RollBatch>().eq(RollBatch::getBatchId, batchId));
        if (batch == null) throw new BusinessException("批次不存在");
        return batch;
    }

    public Page<RollBatch> pageQuery(int page, int size, String batchId, String workOrderId, Integer status) {
        LambdaQueryWrapper<RollBatch> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(batchId)) wrapper.like(RollBatch::getBatchId, batchId);
        if (StringUtils.hasText(workOrderId)) wrapper.eq(RollBatch::getWorkOrderId, workOrderId);
        if (status != null) wrapper.eq(RollBatch::getStatus, status);
        wrapper.orderByDesc(RollBatch::getRollDate);
        return rollBatchMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public List<RollBatch> listAll() {
        return rollBatchMapper.selectList(new LambdaQueryWrapper<RollBatch>().orderByDesc(RollBatch::getRollDate));
    }

    @Transactional
    public void merge(String batchId, String targetBatchId) {
        RollBatch source = getByBatchId(batchId);
        getByBatchId(targetBatchId); // 验证目标批次存在
        source.setStatus(2); // 已合并
        source.setAbnormalDesc("合并至" + targetBatchId);
        rollBatchMapper.updateById(source);

        // 更新关联铸坯
        List<Slab> slabs = slabMapper.selectList(new LambdaQueryWrapper<Slab>().eq(Slab::getRollBatchId, batchId));
        for (Slab slab : slabs) {
            slab.setRollBatchId(targetBatchId);
            slabMapper.updateById(slab);
        }
    }

    @Transactional
    public RollBatch split(String batchId, RollBatchSaveRequest newBatchReq) {
        getByBatchId(batchId);
        RollBatch newBatch = create(newBatchReq);
        RollBatch original = getByBatchId(batchId);
        original.setStatus(3); // 已拆分
        original.setAbnormalDesc("拆分出" + newBatch.getBatchId());
        rollBatchMapper.updateById(original);
        return newBatch;
    }

    @Transactional
    public void delete(String batchId) {
        RollBatch batch = getByBatchId(batchId);
        rollBatchMapper.deleteById(batch.getId());
    }
}
