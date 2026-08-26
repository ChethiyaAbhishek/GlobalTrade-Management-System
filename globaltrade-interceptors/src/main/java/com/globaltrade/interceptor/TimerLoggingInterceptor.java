package com.globaltrade.interceptor;

import jakarta.ejb.Timer;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

@Interceptor
public class TimerLoggingInterceptor {

    private static final Logger logger = Logger.getLogger(TimerLoggingInterceptor.class.getName());

    @AroundTimeout
    public Object logTimerExecution(InvocationContext ctx) throws Exception {

        Timer timer = (Timer) ctx.getTimer();
        Object info = timer.getInfo();

        logger.info("TIMER FIRED: info: '" + info
                + "', firedAt: " + LocalDate.now()
                + ", nextTimeout: " + timer.getNextTimeout());

        try {
            return ctx.proceed();
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    "TIMER EXECUTION FAILED: info: '" + info + "'", e);
            throw e;
        }
    }
}
