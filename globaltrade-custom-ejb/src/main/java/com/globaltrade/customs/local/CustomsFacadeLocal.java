package com.globaltrade.customs.local;

import com.globaltrade.core.entity.CustomsDocument;
import com.globaltrade.core.enums.DocumentStatus;
import com.globaltrade.core.exception.CustomsDeadlineException;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface CustomsFacadeLocal {

    void fileDocument(CustomsDocument document) throws CustomsDeadlineException;

    List<CustomsDocument> getApproachingDeadlines();

    List<CustomsDocument> getDocumentsAwaitingClearance();

    void processClearance(Long documentId);

    CustomsDocument findById(Long id);

    void fileDraftDocument(Long documentId);

    List<CustomsDocument> findByStatus(DocumentStatus status);
}
