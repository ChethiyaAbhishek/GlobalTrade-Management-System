package com.globaltrade.core.facade;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

public abstract class BaseFacade<T> {

    private final Class<T> entityClass;

    protected BaseFacade(Class<T> entityClass){
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

    public void create(T entity){
        getEntityManager().persist(entity);
    }

    public void update(T entity){
        getEntityManager().merge(entity);
    }

    public void remove(T entity){
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    public T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    public List<T> findAll() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        cq.select(root);
        return getEntityManager().createQuery(cq).getResultList();
    }

    public long count() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(entityClass);
        cq.select(cb.count(root));
        return getEntityManager().createQuery(cq).getSingleResult();
    }

    public void flush(){
        getEntityManager().flush();
    }

}
