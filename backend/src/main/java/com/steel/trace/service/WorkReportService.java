package com.steel.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.dto.request.WorkReportSaveRequest;
import com.steel.trace.entity.WorkReport;
import com.steel.trace.mapper.WorkReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkReportService {

    private final WorkReportMapper workReportMapper;

    @Transactional
    public WorkReport create(WorkReportSaveRequest request) {
        WorkReport report = new WorkReport();
        report.setWorkOrderId(request.getWorkOrderId());
        report.setProcessName(request.getProcessName());
        report.setBatchId(request.getBatchId());
        report.setCoilId(request.getCoilId());
        report.setReportQuantity(request.getReportQuantity());
        report.setReportTime(request.getReportTime());
        report.setOperator(request.getOperator());
        report.setShiftGroup(request.getShiftGroup());
        report.setApproveStatus(0);
        workReportMapper.insert(report);
        return report;
    }

    @Transactional
    public void approve(Long id, int status) {
        WorkReport report = workReportMapper.selectById(id);
        if (report == null) throw new BusinessException("报工记录不存在");
        report.setApproveStatus(status);
        workReportMapper.updateById(report);
    }

    @Transactional
    public void cancel(Long id) {
        WorkReport report = workReportMapper.selectById(id);
        if (report == null) throw new BusinessException("报工记录不存在");
        report.setApproveStatus(2);
        workReportMapper.updateById(report);
    }

    public Page<WorkReport> pageQuery(int page, int size, String workOrderId, String batchId, String coilId, Integer approveStatus) {
        LambdaQueryWrapper<WorkReport> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(workOrderId)) wrapper.eq(WorkReport::getWorkOrderId, workOrderId);
        if (StringUtils.hasText(batchId)) wrapper.eq(WorkReport::getBatchId, batchId);
        if (StringUtils.hasText(coilId)) wrapper.eq(WorkReport::getCoilId, coilId);
        if (approveStatus != null) wrapper.eq(WorkReport::getApproveStatus, approveStatus);
        wrapper.orderByDesc(WorkReport::getReportTime);
        return workReportMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public List<WorkReport> listAll() {
        return workReportMapper.selectList(new LambdaQueryWrapper<WorkReport>().orderByDesc(WorkReport::getReportTime));
    }

    public List<WorkReport> getByBatchId(String batchId) {
        return workReportMapper.selectList(new LambdaQueryWrapper<WorkReport>()
                .eq(WorkReport::getBatchId, batchId)
                .eq(WorkReport::getApproveStatus, 1));
    }

    public List<WorkReport> getByCoilId(String coilId) {
        return workReportMapper.selectList(new LambdaQueryWrapper<WorkReport>()
                .eq(WorkReport::getCoilId, coilId)
                .eq(WorkReport::getApproveStatus, 1));
    }

    @Transactional
    public void batchImport(List<WorkReportSaveRequest> requests) {
        for (WorkReportSaveRequest req : requests) {
            create(req);
        }
    }
}
