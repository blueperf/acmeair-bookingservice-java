package com.acmeair.faultTolerance;

import java.util.logging.Logger;

import javax.enterprise.context.Dependent;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;

@Dependent
public class LongFallbackHandler implements FallbackHandler<Long> {
	  protected static Logger logger =  Logger.getLogger(LongFallbackHandler.class.getName());


	@Override
    public Long handle(ExecutionContext context) {
		logger.info("fallback for " + context.getMethod().getName());		
        return new Long(-1);
    }

}
