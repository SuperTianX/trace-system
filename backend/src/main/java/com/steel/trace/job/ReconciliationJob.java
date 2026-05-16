package com.steel.trace.job;

import com.steel.trace.service.ChainDiagnosisService;
import com.steel.trace.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReconciliationJob {

    private final ReconciliationService reconciliationService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void execute() {
        log.info("定时对账任务开始执行");
        try {
            int count = reconciliationService.execute().size();
            log.info("定时对账任务完成，共发现 {} 条差异", count);
        } catch (Exception e) {
            log.error("定时对账任务执行失败", e);
        }
    }
}
