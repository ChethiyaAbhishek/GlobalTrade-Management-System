package com.globaltrade.customs.beans;

import com.globaltrade.core.constants.Roles;
import com.globaltrade.core.entity.CustomsDocument;
import com.globaltrade.core.enums.DocumentStatus;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Remove;
import jakarta.ejb.Stateful;
import com.globaltrade.core.annotations.Audited;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.logging.Logger;

@Stateful
@Audited
@RolesAllowed({Roles.CUSTOMS_AGENT, Roles.ADMIN})
public class CustomsClearanceSessionBean {

    private static final Logger logger = Logger.getLogger(CustomsClearanceSessionBean.class.getName());

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    private Long documentId;

    public void startClearance(Long documentId) {

        CustomsDocument document = em.find(CustomsDocument.class, documentId);

        if (document == null) {
            throw new IllegalArgumentException("Customs document not found: " + documentId);
        }

        if (document.getStatus() != DocumentStatus.SUBMITTED) {
            throw new IllegalStateException("Document " + documentId
                    + " is not awaiting clearance (status: " + document.getStatus() + ")");
        }

        this.documentId = documentId;

        logger.fine("Clearance session started for document " + documentId);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void approveClearance() {

        requireActiveSession();

        CustomsDocument document = em.find(CustomsDocument.class, documentId);
        document.setStatus(DocumentStatus.APPROVED);
        em.merge(document);

        logger.info("Document " + documentId + ": CLEARED");
        resetConversation();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void rejectClearance() {

        requireActiveSession();

        CustomsDocument document = em.find(CustomsDocument.class, documentId);
        document.setStatus(DocumentStatus.REJECTED);
        em.merge(document);

        logger.info("Document " + documentId + ": REJECTED");
        resetConversation();
    }

    @Remove
    public void end() {
        logger.fine("Customs clearance session ended (document " + documentId + ")");
        resetConversation();
    }

    private void requireActiveSession() {
        if (documentId == null) {
            throw new IllegalStateException("Clearance session not started: session not found");
        }
    }

    private void resetConversation() {
        documentId = null;
    }
}
