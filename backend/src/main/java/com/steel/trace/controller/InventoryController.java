package com.steel.trace.controller;

import com.steel.trace.common.result.PageResult;
import com.steel.trace.common.result.Result;
import com.steel.trace.common.util.ExcelUtil;
import com.steel.trace.common.util.OperationLog;
import com.steel.trace.dto.request.InventorySaveRequest;
import com.steel.trace.entity.Inventory;
import com.steel.trace.entity.Stock;
import com.steel.trace.service.InventoryService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final com.steel.trace.mapper.InventoryMapper inventoryMapper;

    @PostMapping
    @OperationLog(module = "出入库", action = "新增", description = "新增出入库单据")
    public Result<Inventory> create(@Valid @RequestBody InventorySaveRequest request) {
        return Result.success(inventoryService.create(request));
    }

    @GetMapping("/{id}")
    public Result<Inventory> get(@PathVariable Long id) {
        Inventory inv = inventoryMapper.selectById(id);
        if (inv == null) {
            throw new com.steel.trace.common.exception.BusinessException("单据不存在");
        }
        return Result.success(inv);
    }

    @PutMapping("/{docNo}/approve")
    @OperationLog(module = "出入库", action = "审核", description = "审核出入库单据")
    public Result<Void> approve(@PathVariable String docNo) {
        inventoryService.approve(docNo);
        return Result.success();
    }

    @PutMapping("/{docNo}/void")
    @OperationLog(module = "出入库", action = "作废", description = "作废出入库单据")
    public Result<Void> voidDoc(@PathVariable String docNo) {
        inventoryService.voidDoc(docNo);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<Inventory>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String docNo,
            @RequestParam(required = false) Integer docType,
            @RequestParam(required = false) String coilId,
            @RequestParam(required = false) Integer status) {
        var p = inventoryService.pageQuery(page, size, docNo, docType, coilId, status);
        return Result.success(PageResult.of(p.getRecords(), p.getTotal(), (int)p.getCurrent(), (int) p.getSize()));
    }

    @GetMapping("/stock/page")
    public Result<PageResult<Stock>> stockPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String coilId,
            @RequestParam(required = false) String warehouse) {
        var p = inventoryService.stockPage(page, size, coilId, warehouse);
        return Result.success(PageResult.of(p.getRecords(), p.getTotal(), (int)p.getCurrent(), (int) p.getSize()));
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        List<Inventory> list = inventoryService.listAll();
        ExcelUtil.export(response, "出入库数据", Inventory.class, list);
    }
}
