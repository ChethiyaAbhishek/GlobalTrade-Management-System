package com.globaltrade.inventory.local;

import com.globaltrade.core.dto.StockAdjustmentResult;
import com.globaltrade.core.entity.InventoryItem;
import com.globaltrade.core.exception.InsufficientInventoryException;
import com.globaltrade.core.exception.NoItemFoundException;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface InventoryFacadeLocal {

    void create(InventoryItem item);

    InventoryItem findById(Long id);

    StockAdjustmentResult adjustStock(Long itemId, int change) throws InsufficientInventoryException, NoItemFoundException;

    boolean checkStock(Long itemId, int requiredQty);

    List<InventoryItem> getItemsBelowReorderLevel();

    List<InventoryItem> findAll();

    long count();
}
