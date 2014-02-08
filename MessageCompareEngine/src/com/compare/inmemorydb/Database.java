package com.compare.inmemorydb;

import java.util.List;


public interface Database {
	
	public Schema connect(String schemaName);
	
	public void disconnect(String schemaName);
	
	public void createSchema(Schema table);
	
	public void dropSchema(String schemaName);
	
	public List<String> getConnectedSchemas();
	
	public boolean isConnected(String schemaName);
	
	public boolean isAvailable(String schemaName);

}
