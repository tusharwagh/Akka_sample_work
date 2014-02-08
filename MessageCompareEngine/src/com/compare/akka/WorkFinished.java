package com.compare.akka;

import com.compare.model.Message;

public class WorkFinished {
	
	private final Message message;
	
	private final Exception failure;
	
	public WorkFinished(Message message)
	{
		this.message = message;
		
		this.failure = null;
	}
	
	public WorkFinished(Message message, Exception failure)
	{
		this.failure = failure;
		
		this.message = message;
	}
	
	public Message getMessage()
	{
		return message;
	}

	public Exception getFailure()
	{
		return failure;
	}
	
	public boolean isFailure()
	{
		return failure!=null?true:false;
	}
}
