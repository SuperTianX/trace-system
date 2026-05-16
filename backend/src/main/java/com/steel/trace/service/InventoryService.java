package com.steel.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.dto.request.InventorySaveRequest;
import com.steel.trace.entity.Coil;
import com.steel.trace.entity.Inventory;
import com.steel.trace.entity.Stock;
import com.steel.trace.mapper.CoilMapper;
import com.steel.trace.mapper.InventoryMapper;
import com.steel.trace.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.steel.trace.common.constant.Constants.InventoryDocType;
import static com.steel.trace.common.constant.Constants.LifecycleStatus;
import static com.steel.trace.common.constant.Constants.StockStatus;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryMapper inventoryMapper;
    private final StockMapper stockMapper;
    private final CoilMapper coilMapper;

    @Transactional
    public Inventory create(InventorySaveRequest request) {
        Coil coil = coilMapper.selectOne(new LambdaQueryWrapper<Coil>().eq(Coil::getCoilId, request.getCoilId()));
        if (coil == null) throw new BusinessException("卷号不存在");

        if (request.getDocType() == InventoryDocType.OUTBOUND) {
            Stock stock = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                    .eq(Stock::getCoilId, request.getCoilId())
                    .eq(Stock::getWarehouse, request.getWarehouse()));
            if (stock == null || stock.getQuantity().compareTo(request.getQuantity()) < 0) {
                throw new BusinessException("库存不足");
            }
        }

        Inventory inventory = new Inventory();
        inventory.setDocNo(request.getDocNo());
        inventory.setDocType(request.getDocType());
        inventory.setBatchId(request.getBatchId());
        inventory.setCoilId(request.getCoilId());
        inventory.setQuantity(request.getQuantity());
        inventory.setOperateTime(request.getOperateTime());
        inventory.setWarehouse(request.getWarehouse());
        inventory.setOperator(request.getOperator());
        inventory.setStatus(0);
        inventoryMapper.insert(inventory);

        // 更新钢卷生命周期
        if (request.getDocType() == InventoryDocType.INBOUND) {
            coil.setInboundOrderNo(request.getDocNo());
            coil.setLifecycleStatus(LifecycleStatus.INBOUND);
            coil.setStockStatus(StockStatus.IN_STOCK);
        } else {
            coil.setOutboundOrderNo(request.getDocNo());
            coil.setLifecycleStatus(LifecycleStatus.OUTBOUND);
            coil.setStockStatus(StockStatus.OUTBOUND);
        }
        coilMapper.updateById(coil);

        return inventory;
    }

    @Transactional
    public void approve(String docNo) {
        Inventory inventory = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>().eq(Inventory::getDocNo, docNo));
        if (inventory == null) throw new BusinessException("单据不存在");
        inventory.setStatus(1);
        inventoryMapper.updateById(inventory);

        // 更新库存台账
        Stock stock = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getCoilId, inventory.getCoilId())
                .eq(Stock::getWarehouse, inventory.getWarehouse()));
        if (stock == null) {
            stock = new Stock();
            stock.setCoilId(inventory.getCoilId());
            stock.setWarehouse(inventory.getWarehouse());
            stock.setQuantity(inventory.getQuantity());
            stock.setStockStatus(0);
            stockMapper.insert(stock);
        } else {
            if (inventory.getDocType() == InventoryDocType.INBOUND) {
                stock.setQuantity(stock.getQuantity().add(inventory.getQuantity()));
            } else {
                stock.setQuantity(stock.getQuantity().subtract(inventory.getQuantity()));
            }
            stockMapper.updateById(stock);
        }
    }

    @Transactional
    public void voidDoc(String docNo) {
        Inventory inventory = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>().eq(Inventory::getDocNo, docNo));
        if (inventory == null) throw new BusinessException("单据不存在");
        inventory.setStatus(2);
        inventoryMapper.updateById(inventory);
    }

    public Page<Inventory> pageQuery(int page, int size, String docNo, Integer docType, String coilId, Integer status) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(docNo)) wrapper.eq(Inventory::getDocNo, docNo);
        if (docType != null) wrapper.eq(Inventory::getDocType, docType);
        if (StringUtils.hasText(coilId)) wrapper.eq(Inventory::getCoilId, coilId);
        if (status != null) wrapper.eq(Inventory::getStatus, status);
        wrapper.orderByDesc(Inventory::getOperateTime);
        return inventoryMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public List<Inventory> listAll() {
        return inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>().orderByDesc(Inventory::getOperateTime));
    }

    public Page<Stock> stockPage(int page, int size, String coilId, String warehouse) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(coilId)) wrapper.eq(Stock::getCoilId, coilId);
        if (StringUtils.hasText(warehouse)) wrapper.eq(Stock::getWarehouse, warehouse);
        return stockMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
