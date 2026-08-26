package com.globaltrade.customs.beans;

import com.globaltrade.core.entity.CustomsDocument;
import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.enums.DocumentStatus;
import com.globaltrade.core.enums.DocumentType;
import com.globaltrade.core.exception.CustomsDeadlineException;
import jakarta.annotation.Resource;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

import java.util.logging.Logger;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class CustomsDocumentationBean {

    private static final Logger logger = Logger.getLogger(CustomsDocumentationBean.class.getName());

    private static final int TRANSACTION_TIMEOUT_SECONDS = 30;

    @Resource
    private UserTransaction userTransaction;

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    public void submitDocumentationPackage(Shipment shipment) throws CustomsDeadlineException {
        try {
            userTransaction.setTransactionTimeout(TRANSACTION_TIMEOUT_SECONDS);
            userTransaction.begin();

            if (shipment == null) {
                throw new CustomsDeadlineException("Cannot file documents: shipment not found");
            }

            CustomsDocument exportDeclaration = new CustomsDocument(shipment, DocumentType.EXPORT_DECLARATION);
            exportDeclaration.setStatus(DocumentStatus.SUBMITTED);
            em.persist(exportDeclaration);

            CustomsDocument commercialInvoice = new CustomsDocument(shipment, DocumentType.COMMERCIAL_INVOICE);
            commercialInvoice.setStatus(DocumentStatus.SUBMITTED);
            em.persist(commercialInvoice);

            /// ------------------------------------
            /// External API call [customs gateway]
            /// ------------------------------------

            userTransaction.commit();
            logger.info("BMT COMMITTED: documentation package for shipment " + shipment.getId());

        } catch (CustomsDeadlineException e) {
            rollbackQuietly();
            logger.warning("BMT ROLLED BACK: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            rollbackQuietly();
            logger.severe("BMT ROLLED BACK (system failure): " + e.getMessage());
            throw new EJBException("Documentation package submission failed: " + e.getMessage(), e);
        }
    }

    private void rollbackQuietly() {
        try {
            int status = userTransaction.getStatus();
            if (status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK) {
                userTransaction.rollback();
            }
        } catch (Exception rollbackEx) {
            logger.severe("Rollback failed: " + rollbackEx.getMessage());
        }
    }
}
