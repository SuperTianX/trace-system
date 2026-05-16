package com.steel.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steel.trace.common.constant.Constants;
import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.dto.request.ComplaintSaveRequest;
import com.steel.trace.entity.Complaint;
import com.steel.trace.mapper.ComplaintMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintMapper complaintMapper;

    @Transactional
    public Complaint register(ComplaintSaveRequest request) {
        if (complaintMapper.selectOne(new LambdaQueryWrapper<Complaint>()
                .eq(Complaint::getComplaintId, request.getComplaintId())) != null) {
            throw new BusinessException("投诉单号已存在");
        }
        Complaint complaint = new Complaint();
        complaint.setComplaintId(request.getComplaintId());
        complaint.setCustomerId(request.getCustomerId());
        complaint.setCustomerName(request.getCustomerName());
        complaint.setCoilId(request.getCoilId());
        complaint.setProblemDesc(request.getProblemDesc());
        complaint.setSeverity(request.getSeverity());
        complaint.setStatus(Constants.ComplaintStatus.REGISTERED);
        complaintMapper.insert(complaint);
        return complaint;
    }

    @Transactional
    public void startTracing(Long id) {
        Complaint complaint = getById(id);
        complaint.setStatus(Constants.ComplaintStatus.TRACING);
        complaintMapper.updateById(complaint);
    }

    @Transactional
    public void assignResponsibility(Long id, String responsibleDept, String rootCause) {
        Complaint complaint = getById(id);
        complaint.setResponsibleDept(responsibleDept);
        complaint.setRootCause(rootCause);
        complaint.setStatus(Constants.ComplaintStatus.PROCESSING);
        complaintMapper.updateById(complaint);
    }

    @Transactional
    public void setMeasures(Long id, String measures) {
        Complaint complaint = getById(id);
        complaint.setCorrectiveMeasures(measures);
        complaint.setStatus(Constants.ComplaintStatus.RECHECKING);
        complaintMapper.updateById(complaint);
    }

    @Transactional
    public void setRectification(Long id, String result) {
        Complaint complaint = getById(id);
        complaint.setRectificationResult(result);
        complaint.setStatus(Constants.ComplaintStatus.RECHECKING);
        complaintMapper.updateById(complaint);
    }

    @Transactional
    public void close(Long id) {
        Complaint complaint = getById(id);
        complaint.setStatus(Constants.ComplaintStatus.CLOSED);
        complaint.setCloseTime(LocalDateTime.now());
        complaintMapper.updateById(complaint);
    }

    @Transactional
    public void archive(Long id) {
        Complaint complaint = getById(id);
        complaint.setStatus(Constants.ComplaintStatus.ARCHIVED);
        complaintMapper.updateById(complaint);
    }

    public Complaint getById(Long id) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null) throw new BusinessException("质量异议不存在");
        return complaint;
    }

    public List<Complaint> listAll() {
        return complaintMapper.selectList(new LambdaQueryWrapper<Complaint>().orderByDesc(Complaint::getCreateTime));
    }

    public Complaint getByComplaintId(String complaintId) {
        return complaintMapper.selectOne(new LambdaQueryWrapper<Complaint>()
                .eq(Complaint::getComplaintId, complaintId));
    }

    public Page<Complaint> pageQuery(int page, int size, String complaintId, String coilId, Integer status, Integer severity) {
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(complaintId)) wrapper.like(Complaint::getComplaintId, complaintId);
        if (StringUtils.hasText(coilId)) wrapper.eq(Complaint::getCoilId, coilId);
        if (status != null) wrapper.eq(Complaint::getStatus, status);
        if (severity != null) wrapper.eq(Complaint::getSeverity, severity);
        wrapper.orderByDesc(Complaint::getCreateTime);
        return complaintMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
