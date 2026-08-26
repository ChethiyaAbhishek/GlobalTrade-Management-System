package com.globaltrade.inventory.local;

import com.globaltrade.core.entity.InventoryItem;
import com.globaltrade.core.entity.PurchaseRequest;
import com.globaltrade.core.entity.Vendor;
import com.globaltrade.core.enums.PurchaseRequestStatus;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface PurchaseRequestFacadeLocal {

    PurchaseRequest createAutomatedRequest(InventoryItem item, Vendor vendor);

    boolean hasOpenRequest(Long itemId);

    PurchaseRequest createManualRequest(Long itemId, int quantity, Long vendorId);

    void updateRequestStatus(Long requestId, PurchaseRequestStatus requestStatus);

    void placeOrder(Long requestId);

    void fulfillRequest(Long requestId);

    List<PurchaseRequest> findByStatus(PurchaseRequestStatus status);

    List<PurchaseRequest> findAll();
}
