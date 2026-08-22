package com.globaltrade.interceptor;

import com.globaltrade.core.annotations.Audited;
import com.globaltrade.core.entity.AuditLog;
import com.globaltrade.interceptor.service.AuditLoggingService;
import jakarta.annotation.Resource;
import jakarta.ejb.SessionContext;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@Interceptor
@Audited
public class AuditLoggingInterceptor {

    private static final Logger logger = Logger.getLogger(AuditLoggingInterceptor.class.getName());

    @Resource
    private SessionContext sessionContext;

    @Inject
    private AuditLoggingService auditService;

    @AroundInvoke
    public Object auditLogging(InvocationContext context) throws Exception {
        String beanName = context.getTarget().getClass().getSimpleName();
        String methodName = context.getMethod().getName();
        String params = Arrays.toString(context.getParameters());
        String username = getUserName();

        long start = System.currentTimeMillis();

        String resultStatus = "SUCCESS";

        try {
            return context.proceed();
        } catch (Exception e) {
            resultStatus = "FAILED: ERROR";

            logger.log(Level.WARNING, "AUDIT FAILED: user: " + username + ", " +
                    "bean: " + beanName + ", " +
                    "method: " + methodName + ", " +
                    "params: " + params + ", " +
                    "EXCEPTION: " + e.getMessage());
            throw e;
        }finally {
            long duration = System.currentTimeMillis() - start;
            persistRecord(username, beanName, methodName, params, resultStatus, duration);
        }
    }

    private String getUserName() {
        try {
            if (sessionContext != null && sessionContext.getCallerPrincipal() != null) {
                return sessionContext.getCallerPrincipal().getName();
            }
        } catch (Exception e) {
        }
        return "anonymous";
    }

    private void persistRecord(String username, String beanName, String methodName, String params, String result, long duration){
            auditService.persistRecord(AuditLog.builder()
                    .username(username)
                    .beanName(beanName)
                    .methodName(methodName)
                    .parameters(params)
                    .result(result)
                    .executionTimeMs(duration).build());
    }

}
