package com.globaltrade.security.authorization;

import com.globaltrade.core.constants.Roles;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.SessionContext;

public final class ProgrammaticSecurityHelper {

    private ProgrammaticSecurityHelper() {
    }

    public static void assertCanAccessVendorData(SessionContext ctx) {

        if (ctx == null) {
            throw new EJBAccessException("No security context available");
        }

        boolean allowed = ctx.isCallerInRole(Roles.VENDOR_REPRESENTATIVE)
                || ctx.isCallerInRole(Roles.COORDINATOR)
                || ctx.isCallerInRole(Roles.ADMIN);

        if (!allowed) {
            throw new EJBAccessException("User '" + ctx.getCallerPrincipal().getName()
                    + "' is not authorized to access vendor data");
        }
    }

    public static boolean isCurrentUserAdmin(SessionContext ctx) {
        return ctx != null && ctx.isCallerInRole(Roles.ADMIN);
    }

    public static String currentUsername(SessionContext ctx) {
        if (ctx == null || ctx.getCallerPrincipal() == null) {
            return "anonymous";
        }
        return ctx.getCallerPrincipal().getName();
    }

    public static void assertRole(SessionContext ctx, String requiredRole, String operationDescription) {

        if (ctx == null || !ctx.isCallerInRole(requiredRole)) {

            String user = (ctx != null && ctx.getCallerPrincipal() != null) ? ctx.getCallerPrincipal().getName() : "anonymous";

            throw new EJBAccessException("User '" + user + "' cannot " + operationDescription + " - required role: " + requiredRole);
        }
    }
}
