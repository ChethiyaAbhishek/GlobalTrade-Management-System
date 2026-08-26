package com.globaltrade.web.rest;

import com.globaltrade.core.entity.CustomsDocument;
import com.globaltrade.customs.local.CustomsFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/customs")
@Produces(MediaType.APPLICATION_JSON)
public class CustomsResource {

    @EJB
    private CustomsFacadeLocal customsFacade;

    @GET
    @Path("/deadlines")
    public Response getDeadlines() {
        return Response.ok(summarize(customsFacade.getApproachingDeadlines())).build();
    }

    @GET
    @Path("/awaiting-clearance")
    public Response getAwaitingClearance() {
        return Response.ok(summarize(customsFacade.getDocumentsAwaitingClearance())).build();
    }

    private List<Map<String, Object>> summarize(List<CustomsDocument> documents) {

        List<Map<String, Object>> summaries = new ArrayList<>();

        for (CustomsDocument doc : documents) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("documentId", doc.getId());
            entry.put("type", doc.getType().name());
            entry.put("status", doc.getStatus().name());
            entry.put("filedAt", doc.getFiledAt());
            entry.put("deadline", doc.getDeadline());
            summaries.add(entry);
        }

        return summaries;
    }

}
