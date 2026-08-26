package com.globaltrade.shipment.beans;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import java.util.Random;
import java.util.logging.Logger;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class RouteOptimizationBean {

    private static final Logger logger = Logger.getLogger(RouteOptimizationBean.class.getName());

    private static final Random random = new Random();

    private static final String[] GLOBAL_HUBS = {"Singapore", "United Kingdom", "Dubai", "China", "USA"};

    public String calculateOptimalRoute(String origin, String destination) {

        if (origin == null || origin.trim().isEmpty() || destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException("Origin and destination are required");
        }

        String route = origin + " -> [hub: " + selectHub(origin, destination) + "] -> " + destination;

        logger.fine("Route calculated: " + route);
        return route;
    }

    private String selectHub(String origin, String destination) {
        return GLOBAL_HUBS[random.nextInt(5)];
    }
}
