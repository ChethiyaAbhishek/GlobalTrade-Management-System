package com.globaltrade.customs.beans;

import com.globaltrade.core.annotations.Audited;
import com.globaltrade.core.annotations.Compliant;
import com.globaltrade.core.constants.Roles;
import com.globaltrade.core.entity.CustomsDocument;
import com.globaltrade.core.enums.DocumentStatus;
import com.globaltrade.core.exception.CustomsDeadlineException;
import com.globaltrade.core.facade.BaseFacade;
import com.globaltrade.customs.local.CustomsFacadeLocal;
import com.globaltrade.customs.timer.CustomsDeadlineTimerBean;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Stateless
@Audited
@Compliant
public class CustomsFacadeBean extends BaseFacade<CustomsDocument> implements CustomsFacadeLocal {

    private static final int DEADLINE_DAYS = 3;

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    @EJB
    private CustomsDeadlineTimerBean customsDeadlineTimer;

    public CustomsFacadeBean(){
        super(CustomsDocument.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void fileDocument(CustomsDocument document) throws CustomsDeadlineException {

        if (document == null) {
            throw new IllegalArgumentException("Customs document is required");
        }

        if (document.getDeadline() != null && document.getDeadline().isBefore(LocalDate.now())) {
            throw new CustomsDeadlineException(
                    "Filing rejected: regulatory deadline " + document.getDeadline()
                            + " has already passed");
        }
        document.setStatus(DocumentStatus.SUBMITTED);
        document.setFiledAt(Instant.now());
        em.persist(document);

        if (document.getDeadline() != null) {
            customsDeadlineTimer.scheduleDeadlineAlert(document.getId(), document.getDeadline());
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<CustomsDocument> getApproachingDeadlines() {

        return em.createNamedQuery("CustomsDocument.findApproachingDeadlines", CustomsDocument.class)
                .setParameter("deadline", LocalDate.now().plusDays(DEADLINE_DAYS))
                .getResultList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<CustomsDocument> getDocumentsAwaitingClearance() {

        return em.createNamedQuery("CustomsDocument.findAwaitingClearance", CustomsDocument.class)
                .getResultList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void processClearance(Long documentId) {

        CustomsDocument document = em.find(CustomsDocument.class, documentId);

        if (document == null) {
            throw new IllegalArgumentException("Customs document not found: " + documentId);
        }

        document.setStatus(DocumentStatus.APPROVED);
        em.merge(document);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public CustomsDocument findById(Long id) {
        return super.find(id);
    }

    @Override
    @RolesAllowed({Roles.CUSTOMS_AGENT, Roles.ADMIN})
    public void fileDraftDocument(Long documentId) {

        CustomsDocument doc = em.find(CustomsDocument.class, documentId);

        if (doc == null) throw new IllegalArgumentException("Document not found: " + documentId);

        if (doc.getStatus() != DocumentStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT documents can be filed");
        }

        doc.setStatus(DocumentStatus.SUBMITTED);
        doc.setFiledAt(Instant.now());
        em.merge(doc);

        if (doc.getDeadline() != null) {
            customsDeadlineTimer.scheduleDeadlineAlert(doc.getId(), doc.getDeadline());
        }
    }

    @Override
    public List<CustomsDocument> findByStatus(DocumentStatus status) {

        return em.createNamedQuery("CustomsDocument.findByStatus", CustomsDocument.class)
                .setParameter("status", status)
                .getResultList();
    }
}
