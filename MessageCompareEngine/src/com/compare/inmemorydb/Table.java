package com.compare.inmemorydb;

import java.util.List;
import java.util.Map;

public interface Table<K,V> {
	
	public void setSchema(Schema schema);

	public String getTableName();
	
	public void initialize();
	
	public void load(Map<K,V> model);
	
	public void delete(K id);
	
	public void insert(V model);
	
	public void update(V model);
	
	public V find(K id);
	
	public List<V> findAll();
	
	public void deleteAll();
	
	public Schema getSchema();
	
}
