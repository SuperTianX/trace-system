package com.steel.trace.job;

import com.steel.trace.service.ChainDiagnosisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChainDiagnosisJob {

    private final ChainDiagnosisService chainDiagnosisService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {
        log.info("定时断链诊断任务开始执行");
        try {
            int count = chainDiagnosisService.execute().size();
            log.info("定时断链诊断任务完成，共发现 {} 条断链", count);
        } catch (Exception e) {
            log.error("定时断链诊断任务执行失败", e);
        }
    }
}
