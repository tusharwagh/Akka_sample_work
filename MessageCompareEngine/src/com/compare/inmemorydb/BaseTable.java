package com.compare.inmemorydb;

public abstract class BaseTable<K,V> implements Table<K, V>{

	protected String tableName;
	
	private Schema schema;

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getTableName()
	{
		return this.tableName;
	}
	

	@Override
	public void setSchema(Schema schema)
	{
		this.schema = schema;
	}
	
	@Override
	public Schema getSchema() {
		return this.schema;
	}	
}
