package com.steel.trace.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HeatPageQuery {
    private int page = 1;
    private int size = 20;
    private String heatId;
    private String steelGrade;
    private LocalDate smeltDateStart;
    private LocalDate smeltDateEnd;
    private Integer status;
}
