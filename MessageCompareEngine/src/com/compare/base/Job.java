package com.compare.base;

public interface Job {
	
    /**
     * @param jobHandler  a handler
     * @return true if the given object is a proper handler for this job
     */
    public boolean isAppropriateHandler(Object jobHandler);

    /**
     * Execute this job by means of the given handler.
     * @param jobHandler   handler to execute this job with
     * @param context      context
     */
    public void executeWith(Object jobHandler, JobContext context);

}

