package com.compare.base;

import java.util.List;

import com.compare.inmemorydb.Schema;
import com.compare.model.Message;
import com.compare.model.QueueType;

public interface Repository {

	public boolean insertMessage(Message message);
	
	public boolean updateMessage(Message message);
	
	public Message getMessage(String uuid);

	public Message getMessageUsing(String corelationId);
	
	public List<Message> getAll();
	
	public List<Message> getPendingMessages();	
	
	public QueueType type();
	
	public Schema getSchema();
	
}
