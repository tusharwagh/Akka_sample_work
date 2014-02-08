package com.compare.akka;

import com.compare.base.Job;

public class Work {
	
	private final Job storedJob;
	
	private final Object handler;
	
	private final long timeOutMillis;
	
	public Work(Job storedJob, Object handler, long timeOutMillis)
	{
		this.storedJob = storedJob;
		
		this.handler = handler;
		
		this.timeOutMillis = timeOutMillis;
	}
	
	public Job getStoredJob()
	{
		return storedJob;
	}
	
	public Object handler()
	{
		return handler;
	}
	
	public long getJobTimeout()
	{
		return this.timeOutMillis;
	}

}
