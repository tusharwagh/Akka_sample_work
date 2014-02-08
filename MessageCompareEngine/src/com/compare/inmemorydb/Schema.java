package com.compare.inmemorydb;


public interface Schema<T> {
	
	public void setDatabase(Database database);
	
	public String getSchemaName();
	//Table methods
	
	public void createTable(Table table);
	
	public Table getTable(String tableName);
	
	public void drop(String tableName);
	

}
