package com.globaltrade.interceptor;

import com.globaltrade.core.annotations.Monitored;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.logging.Logger;

@Interceptor
@Monitored
@Priority(Interceptor.Priority.APPLICATION + 100)
public class PerformanceMonitorInterceptor {

    private static final Logger logger = Logger.getLogger(PerformanceMonitorInterceptor.class.getName());

    private static final long SLOW_THRESHOLD_MS = 500;

    @AroundInvoke
    public Object monitorPerformance(InvocationContext context) throws Exception{
        String target = context.getTarget().getClass().getSimpleName()+":"+context.getMethod().getName();
        long startNanos = System.nanoTime();

        try{
            return context.proceed();
        }finally {
            long duration = (System.nanoTime() - startNanos)/1000000;
            if(duration > SLOW_THRESHOLD_MS){
                logger.warning("PERFORMANCE WARNING: " + target + " took " + duration
                        + " ms (threshold: " + SLOW_THRESHOLD_MS + " ms)");
            } else {
                logger.fine("PERFORMANCE: " + target + " completed in " + duration + " ms");
            }
        }
    }

}
