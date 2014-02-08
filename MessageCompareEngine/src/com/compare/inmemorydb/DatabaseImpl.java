package com.compare.inmemorydb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseImpl implements Database {

	Map<String, Schema> schema = new ConcurrentHashMap<String, Schema>();
	
	Map<String, Schema> connectedSchemas = new ConcurrentHashMap<String, Schema>();
	
	private static DatabaseImpl instance = new DatabaseImpl();

	public static DatabaseImpl getInstance() {
		return instance;
	}
	
	@Override
	public Schema connect(String schemaName) {
		Schema schema = this.schema.get(schemaName);
		connectedSchemas.put(schemaName, schema);
		return schema;
	}

	@Override
	public void disconnect(String schemaName) {
		this.connectedSchemas.remove(schemaName);
	}

	@Override
	public List<String> getConnectedSchemas()
	{
		return Collections.unmodifiableList(new ArrayList<String>(this.connectedSchemas.keySet()));
	}

	@Override
	public boolean isConnected(String schemaName)
	{
		return this.connectedSchemas.containsKey(schemaName);
	}
	
	@Override
	public boolean isAvailable(String schemaName)
	{
		return this.schema.containsKey(schemaName);
	}
	
	@Override
	public void createSchema(Schema schema) {
		this.schema.put(schema.getSchemaName(), schema);
	}

	@Override
	public void dropSchema(String schemaName) {
		if(this.connectedSchemas.containsKey(schemaName))
		{
			disconnect(schemaName);
		}
		this.schema.remove(schemaName);
	}

}
