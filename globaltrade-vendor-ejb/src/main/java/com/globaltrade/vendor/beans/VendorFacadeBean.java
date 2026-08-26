package com.globaltrade.vendor.beans;

import com.globaltrade.core.annotations.Audited;
import com.globaltrade.core.annotations.Validated;
import com.globaltrade.core.constants.Roles;
import com.globaltrade.core.dto.VendorReportDTO;
import com.globaltrade.core.entity.Vendor;
import com.globaltrade.core.entity.VendorPerformance;
import com.globaltrade.core.enums.VendorStatus;
import com.globaltrade.core.exception.VendorValidationException;
import com.globaltrade.core.facade.BaseFacade;
import com.globaltrade.vendor.remote.VendorFacadeRemote;
import com.globaltrade.vendor.local.VendorFacadeLocal;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
@Audited
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class VendorFacadeBean extends BaseFacade<Vendor> implements VendorFacadeLocal, VendorFacadeRemote {

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    @Resource
    private SessionContext sessionContext;

    public VendorFacadeBean() {
        super(Vendor.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    @Validated
    /// TODO::
    @RolesAllowed({Roles.COORDINATOR, Roles.ADMIN})
    public void createVendor(Vendor vendor) throws VendorValidationException {
        em.persist(vendor);
    }

    @Override
    @RolesAllowed(Roles.ADMIN)
    public void suspendVendor(Long id) {
        Vendor vendor = em.find(Vendor.class, id);
        if (vendor == null) {
            throw new IllegalArgumentException("Vendor not found by vendorId: " + id);
        }
        vendor.setStatus(VendorStatus.SUSPENDED);
        em.merge(vendor);
    }

    @Override
    public VendorPerformance recordEvaluation(Long vendorId, double onTimeDeliveryRate, double defectRate, String evaluationPeriod) {

        if (onTimeDeliveryRate < 0 || onTimeDeliveryRate > 100
                || defectRate < 0 || defectRate > 100) {
            throw new IllegalArgumentException("Rates must be between 0 and 100");
        }

        Vendor vendor = em.find(Vendor.class, vendorId);
        if (vendor == null) {
            throw new IllegalArgumentException("Vendor not found: " + vendorId);
        }

        double score = (onTimeDeliveryRate * 0.7) + ((100 - defectRate) * 0.3);
        score = Math.round(score * 100.0) / 100.0;

        VendorPerformance performance = new VendorPerformance();
        performance.setVendor(vendor);
        performance.setEvaluationPeriod(evaluationPeriod);
        performance.setOnTimeDeliveryRate(Math.round(onTimeDeliveryRate * 100.0) / 100.0);
        performance.setDefectRate(Math.round(defectRate * 100.0) / 100.0);
        performance.setOverallScore(score);
        em.persist(performance);

        vendor.setRating(score);
        em.merge(vendor);

        return performance;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Vendor findById(Long id) {
        return super.find(id);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Vendor> findByCountry(String country) {
        return em.createNamedQuery("Vendor.findByCountry", Vendor.class)
                .setParameter("country", country)
                .getResultList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Vendor findPreferredVendor() {
        List<Vendor> top = em.createNamedQuery("Vendor.findTopRatedActive", Vendor.class)
                .setMaxResults(1)
                .getResultList();
        return top.isEmpty() ? null : top.get(0);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public VendorReportDTO getVendorReport(Long vendorId) {
        canAccessVendorData();

        Vendor vendor = em.find(Vendor.class, vendorId);
        if (vendor == null) {
            return null;
        }

        List<VendorPerformance> latest = em.createNamedQuery("VendorPerformance.findByVendor", VendorPerformance.class)
                .setParameter("vendorId", vendorId)
                .setMaxResults(1)
                .getResultList();

        VendorPerformance perf = latest.isEmpty() ? null : latest.get(0);

        return new VendorReportDTO(
                vendor.getId(),
                vendor.getName(),
                vendor.getCountry(),
                vendor.getRating(),
                perf != null ? perf.getOnTimeDeliveryRate() : null,
                perf != null ? perf.getOverallScore() : null);
    }

    private void canAccessVendorData() {
        if (sessionContext == null) {
            throw new EJBAccessException("No context found");
        }

        boolean allowed = sessionContext.isCallerInRole(Roles.ADMIN)
                || sessionContext.isCallerInRole(Roles.COORDINATOR)
                || sessionContext.isCallerInRole(Roles.VENDOR_REP);

        if (!allowed) {
            throw new EJBAccessException("User: " + sessionContext.getCallerPrincipal().getName()
                    + " is not authorized to access vendor data");
        }
    }
}
