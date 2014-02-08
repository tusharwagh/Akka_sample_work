package com.compare.inmemorydb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.compare.model.Message;

public class SourceMessageTable extends BaseTable<String,Message> {

	public Map<String,Message> messages = new ConcurrentHashMap<String,Message>();
	
	public SourceMessageTable(String tableName)
	{
		this.tableName = tableName;
	}

	@Override
	public void load(Map<String,Message> model) {
		this.messages.putAll(model);
		
	}

	@Override
	public void delete(String id) {
		this.messages.remove(id);
	}

	@Override
	public void insert(Message model) {
		this.messages.put(((Message)model).getUuid(), model);
	}

	@Override
	public Message find(String id) {
		return this.messages.get(id);
	}	

	@Override
	public List<Message> findAll() {
		return Collections.unmodifiableList(new ArrayList<Message>(this.messages.values()));
	}

	@Override
	public void update(Message model) {
		this.messages.put(model.getUuid(), model);
		
	}	
	
	@Override
	public void deleteAll() {
		this.messages.clear();
		
	}

	@Override
	public String toString() {
		return "SourceMessageTable [messages=" + messages + "]";
	}
}
