package com.steel.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.dto.request.QcRecordSaveRequest;
import com.steel.trace.entity.QcRecord;
import com.steel.trace.mapper.QcRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QcRecordService {

    private final QcRecordMapper qcRecordMapper;

    @Transactional
    public QcRecord create(QcRecordSaveRequest request) {
        QcRecord record = new QcRecord();
        record.setRelateType(request.getRelateType());
        record.setRelateId(request.getRelateId());
        record.setInspectItem(request.getInspectItem());
        record.setInspectValue(request.getInspectValue());
        record.setStandardValue(request.getStandardValue());
        record.setResult(request.getResult());
        record.setInspectTime(request.getInspectTime());
        record.setInspector(request.getInspector());
        record.setFailReason(request.getFailReason());
        record.setDisposeMethod(request.getDisposeMethod());
        qcRecordMapper.insert(record);
        return record;
    }

    @Transactional
    public QcRecord update(Long id, QcRecordSaveRequest request) {
        QcRecord record = qcRecordMapper.selectById(id);
        if (record == null) throw new BusinessException("质检记录不存在");
        record.setInspectItem(request.getInspectItem());
        record.setInspectValue(request.getInspectValue());
        record.setStandardValue(request.getStandardValue());
        record.setResult(request.getResult());
        record.setInspectTime(request.getInspectTime());
        record.setInspector(request.getInspector());
        record.setFailReason(request.getFailReason());
        record.setDisposeMethod(request.getDisposeMethod());
        qcRecordMapper.updateById(record);
        return record;
    }

    public QcRecord getById(Long id) {
        QcRecord record = qcRecordMapper.selectById(id);
        if (record == null) throw new BusinessException("质检记录不存在");
        return record;
    }

    public Page<QcRecord> pageQuery(int page, int size, Integer relateType, String relateId, Integer result) {
        LambdaQueryWrapper<QcRecord> wrapper = new LambdaQueryWrapper<>();
        if (relateType != null) wrapper.eq(QcRecord::getRelateType, relateType);
        if (StringUtils.hasText(relateId)) wrapper.eq(QcRecord::getRelateId, relateId);
        if (result != null) wrapper.eq(QcRecord::getResult, result);
        wrapper.orderByDesc(QcRecord::getInspectTime);
        return qcRecordMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public List<QcRecord> listAll() {
        return qcRecordMapper.selectList(new LambdaQueryWrapper<QcRecord>().orderByDesc(QcRecord::getInspectTime));
    }

    public List<QcRecord> getByRelate(String relateId, Integer relateType) {
        return qcRecordMapper.selectList(new LambdaQueryWrapper<QcRecord>()
                .eq(QcRecord::getRelateId, relateId)
                .eq(QcRecord::getRelateType, relateType));
    }

    @Transactional
    public void delete(Long id) {
        qcRecordMapper.deleteById(id);
    }
}
