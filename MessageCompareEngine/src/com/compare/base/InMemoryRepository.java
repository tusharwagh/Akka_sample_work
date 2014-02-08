package com.compare.base;

import java.util.ArrayList;
import java.util.List;

import com.compare.inmemorydb.Schema;
import com.compare.inmemorydb.Table;
import com.compare.model.Message;
import com.compare.model.QueueType;

public class InMemoryRepository implements Repository {
	
	private final Table table;
	
	private final QueueType type;
	
	public InMemoryRepository(Table table,QueueType type)
	{
		this.table = table;
		this.type = type;
	}

	@Override
	public boolean insertMessage(Message message) {
		table.insert(message);
		return true;
	}
	
	@Override
	public boolean updateMessage(Message message) {
		table.update(message);
		return true;
	}	

	@Override
	public Schema getSchema() {
		return table.getSchema();
	}
	
	@Override
	public Message getMessage(String uuid) {
		return (Message)table.find(uuid);
	}

	@Override
	public List<Message> getAll() {
		return table.findAll();
	}

	@Override
	public List<Message> getPendingMessages() {
		List<Message> allMessages = getAll();
		List<Message> pendingMessages = new ArrayList<Message>();
		for(Message message : allMessages)
		{
			if(!message.isCompared())
			{
				pendingMessages.add(message);
			}
		}
		return pendingMessages;
	}

	@Override
	public String toString() {
		return "InMemoryRepository [table=" + table + ", type=" + type + "]";
	}

	@Override
	public QueueType type() {
		return type;
	}

	@Override
	public Message getMessageUsing(String corelationId) {
		List<Message> pendingMessages = getPendingMessages();
		for(Message message : pendingMessages)
		{
			if(message.getCorelationId().equals(corelationId))
			{
				return message;
			}
		}
		return new Message();
	}
	
	
}
