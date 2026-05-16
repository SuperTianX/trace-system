package com.steel.trace.common.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class ExcelUtil {

    public static <T> void export(HttpServletResponse response, String fileName,
                                   Class<T> clazz, List<T> data) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), clazz)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("Sheet1")
                    .doWrite(data);
        } catch (IOException e) {
            log.error("导出Excel失败", e);
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    public static <T> List<T> importData(InputStream inputStream, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        EasyExcel.read(inputStream, clazz, new ReadListener<T>() {
            @Override
            public void invoke(T data, AnalysisContext context) {
                list.add(data);
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                log.info("导入完成，共{}条", list.size());
            }
        }).sheet().doRead();
        return list;
    }

    public static <T> void importWithCallback(InputStream inputStream, Class<T> clazz,
                                               Consumer<List<T>> batchProcessor, int batchSize) {
        List<T> batch = new ArrayList<>();
        EasyExcel.read(inputStream, clazz, new ReadListener<T>() {
            @Override
            public void invoke(T data, AnalysisContext context) {
                batch.add(data);
                if (batch.size() >= batchSize) {
                    batchProcessor.accept(new ArrayList<>(batch));
                    batch.clear();
                }
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                if (!batch.isEmpty()) {
                    batchProcessor.accept(batch);
                }
            }
        }).sheet().doRead();
    }
}
