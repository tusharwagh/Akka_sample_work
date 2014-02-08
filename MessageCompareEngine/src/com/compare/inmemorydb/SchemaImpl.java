package com.compare.inmemorydb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SchemaImpl implements Schema {

	Map<String, Table> table = new ConcurrentHashMap<String, Table>();
	
	private String schemaName;
	
	private Database database;
	
	public SchemaImpl(String schemaName)
	{
		this.schemaName = schemaName;
	}

	@Override
	public void setDatabase(Database database)
	{
		this.database = database;
	}
	
	@Override
	public void createTable(Table table) {
		if(database.isConnected(this.getSchemaName()))
		{
			this.table.put(table.getTableName(), table);	
		}
		else
		{
			throw new RuntimeException("Please connect to the schema before creating a table "+table.getTableName()+" within schema "+this.getSchemaName());
		}
	}

	@Override
	public String getSchemaName()
	{
		return this.schemaName;
	}
	
	@Override
	public Table getTable(String tableName)
	{
		if(database.isConnected(this.getSchemaName()))
		{
			return this.table.get(tableName);	
		}
		else
		{
			throw new RuntimeException("Please connect to the schema before getting the table "+tableName+" within schema "+this.getSchemaName());
		}
		
	}


	@Override
	public void drop(String tableName) {
		
		if(database.isConnected(this.getSchemaName()))
		{
			this.table.remove(tableName);	
		}
		else
		{
			throw new RuntimeException("Please connect to the schema before removing the table "+tableName+" within schema "+this.getSchemaName());
		}

		

	}

}
