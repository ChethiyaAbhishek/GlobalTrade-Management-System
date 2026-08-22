package com.globaltrade.inventory;

import com.globaltrade.core.annotations.Audited;
import com.globaltrade.core.entity.InventoryItem;
import com.globaltrade.core.exception.NoItemFoundException;
import com.globaltrade.core.facade.BaseFacade;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
@Audited
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class InventoryFacadeBean extends BaseFacade<InventoryItem> implements InventoryFacadeLocal{

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    public InventoryFacadeBean(){
        super(InventoryItem.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addStock(Long itemId, int qty) throws NoItemFoundException {
        if(qty < 1){
            throw new IllegalArgumentException("Inventory update stock quantity must be greater than 0");
        }

        InventoryItem item = em.find(InventoryItem.class, itemId);
        if(item == null){
            throw new NoItemFoundException(itemId);
        }

        item.setQuantity(item.getQuantity() + qty);
        em.merge(item);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean checkStock(Long itemId, int reqQty){
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
