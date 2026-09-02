package com.globaltrade.interceptor;

import com.globaltrade.core.annotations.Audited;
import com.globaltrade.core.annotations.ExcludeFromAudit;
import com.globaltrade.core.entity.AuditLog;
import com.globaltrade.core.service.AuditLogService;
import jakarta.annotation.Priority;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Interceptor
@Audited
@Priority(Interceptor.Priority.APPLICATION)
public class AuditLoggingInterceptor implements Serializable {

    private static final Logger logger = Logger.getLogger(AuditLoggingInterceptor.class.getName());

    @Resource
    private SessionContext sessionContext;

    private volatile AuditLogService auditLogService;

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    @AroundInvoke
    public Object auditLogging(InvocationContext context) throws Exception {

        if (context.getMethod().isAnnotationPresent(ExcludeFromAudit.class)) {
            return context.proceed();
        }

        String beanName = context.getTarget().getClass().getSimpleName();
        String methodName = context.getMethod().getName();
        String params = safeParams(context.getParameters());
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

    private String safeParams(Object[] params) {

        if (params == null || params.length == 0) return "[]";

        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < params.length; i++) {

            Object p = params[i];
            if (p == null) {
                sb.append("null");
            } else if (p instanceof String || p instanceof Number || p instanceof Boolean) {
                sb.append(p);
            } else {
                sb.append(p.getClass().getSimpleName()).append("(obj)");
            }

            if (i < params.length - 1) sb.append(", ");
        }
        sb.append("]");

        String result = sb.toString();
        return result.length() > 255 ? result.substring(0, 250) + "..." : result;
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
        try {
            AuditLog log = new AuditLog();
            log.setUsername(username);
            log.setBeanName(beanName);
            log.setMethodName(methodName);
            log.setParameters(params);
            log.setResult(result);
            log.setExecutionTimeMs(duration);

            AuditLogService svc = auditService();
            if (svc != null) {
                svc.persistAuditLog(log);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to persist audit record", e);
        }
    }

    private AuditLogService auditService() {
        AuditLogService svc = auditLogService;
        if (svc == null) {
            try {
                InitialContext ctx = new InitialContext();
                String[] candidates = {
                        "java:app/globaltrade-messaging-ejb/AuditLogServiceImpl!com.globaltrade.core.service.AuditLogService",
                        "java:global/globaltrade-ear/globaltrade-messaging-ejb/AuditLogServiceImpl!com.globaltrade.core.service.AuditLogService"
                };

                for (String name : candidates) {
                    try {
                        svc = (AuditLogService) ctx.lookup(name);
                        break;
                    } catch (NamingException ignored) { }
                }
                if (svc != null) auditLogService = svc;

            } catch (Exception e) {
            }
        }

        return svc;
    }

}
