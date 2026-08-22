package com.globaltrade.inventory;

import com.globaltrade.core.entity.InventoryItem;
import com.globaltrade.core.exception.NoItemFoundException;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface InventoryFacadeLocal {

//    void create(InventoryItem item);

    InventoryItem findById(Long id);

    void addStock(Long itemId, int change) throws NoItemFoundException;

    boolean checkStock(Long itemId, int requiredQty);

    List<InventoryItem> getItemsBelowReorderLevel();

    List<InventoryItem> findAll();

    long count();
}
