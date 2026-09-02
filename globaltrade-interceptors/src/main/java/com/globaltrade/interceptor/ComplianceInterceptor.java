package com.globaltrade.interceptor;

import com.globaltrade.core.annotations.Compliant;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.logging.Logger;

@Interceptor
@Compliant
public class ComplianceInterceptor {
    private static final Logger logger =
            Logger.getLogger(ComplianceInterceptor.class.getName());

    @AroundInvoke
    public Object checkCompliance(InvocationContext context) throws Exception {
        String target = context.getTarget().getClass().getSimpleName()
                + ":" + context.getMethod().getName();

        logger.info("COMPLIANCE CHECK started: " + target);

        boolean customsOk = verifyCustomsRegulations(context);
        boolean tradeAgreementsOk = verifyTradeAgreements(context);
        boolean securityOk = verifySecurityProtocols(context);

        if (!(customsOk && tradeAgreementsOk && securityOk)) {
            logger.severe("COMPLIANCE VIOLATION blocked: " + target);
            throw new IllegalStateException(
                    "Operation blocked by compliance check: " + target);
        }

        Object result = context.proceed();

        logger.info("COMPLIANCE PASSED: " + target);
        return result;
    }

    private boolean verifyCustomsRegulations(InvocationContext context) {
        return true;
    }

    private boolean verifyTradeAgreements(InvocationContext context) {
        return true;
    }

    private boolean verifySecurityProtocols(InvocationContext context) {
        return true;
    }
}
