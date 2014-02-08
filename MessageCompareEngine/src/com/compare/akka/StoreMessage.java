package com.compare.akka;

import com.compare.model.Message;

public class StoreMessage {
	private final Message message;
	
	public StoreMessage(Message message)
	{
		this.message = message;
	}
	
	public Message getMessage()
	{
		return message;
	}

	@Override
	public String toString() {
		return "StoreMessage [message=" + message + "]";
	}
	
	
}
