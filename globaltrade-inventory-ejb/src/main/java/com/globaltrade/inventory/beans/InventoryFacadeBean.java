package com.globaltrade.inventory.beans;

import com.globaltrade.core.annotations.Audited;
import com.globaltrade.core.entity.InventoryItem;
import com.globaltrade.core.exception.InsufficientInventoryException;
import com.globaltrade.core.exception.NoItemFoundException;
import com.globaltrade.core.facade.BaseFacade;
import com.globaltrade.inventory.local.InventoryFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
@Audited
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class InventoryFacadeBean extends BaseFacade<InventoryItem> implements InventoryFacadeLocal {

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    public InventoryFacadeBean() {
        super(InventoryItem.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void adjustStock(Long itemId, int change) throws InsufficientInventoryException, NoItemFoundException {
        InventoryItem item = em.find(InventoryItem.class, itemId);
        if (item == null) {
            throw new NoItemFoundException(itemId);
        }

        int newQuantity = item.getQuantity() + change;
        if (newQuantity < 0) {
            throw new InsufficientInventoryException("ADJUST_STOCK: Insufficient stock for itemId: " + itemId
                    + " - change requested: " + Math.abs(change) + ", existing: " + item.getQuantity());
        }

        item.setQuantity(newQuantity);
        em.merge(item);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean checkStock(Long itemId, int reqQty) {
        InventoryItem item = em.find(InventoryItem.class, itemId);
        return item != null && item.getQuantity() >= reqQty;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<InventoryItem> getItemsBelowReorderLevel() {
        return em.createNamedQuery("InventoryItem.findBelowReorderLevel", InventoryItem.class)
                .getResultList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public InventoryItem findById(Long id) {
        return super.find(id);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<InventoryItem> findAll() {
        return super.findAll();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long count() {
        return super.count();
    }

}
