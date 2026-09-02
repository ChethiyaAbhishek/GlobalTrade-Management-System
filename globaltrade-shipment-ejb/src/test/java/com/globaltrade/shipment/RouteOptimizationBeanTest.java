package com.globaltrade.shipment;

import com.globaltrade.shipment.beans.RouteOptimizationBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RouteOptimizationBeanTest {

    private final RouteOptimizationBean bean = new RouteOptimizationBean();

    @Test
    @DisplayName("Route contains origin, a global hub and destination")
    void routeContainsAllSegments() {
        String route = bean.calculateOptimalRoute("China", "USA");
        assertTrue(route.startsWith("China"));
        assertTrue(route.contains("hub"));
        assertTrue(route.endsWith("USA"));
    }

    @Test
    @DisplayName("Invalid inputs are rejected")
    void invalidInputsRejected() {
        assertThrows(IllegalArgumentException.class, () -> bean.calculateOptimalRoute(null, "X"));
        assertThrows(IllegalArgumentException.class, () -> bean.calculateOptimalRoute("X", "  "));
    }
}
