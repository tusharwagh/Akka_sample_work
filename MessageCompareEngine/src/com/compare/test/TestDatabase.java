package com.compare.test;

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.compare.base.UUIDGenerator;
import com.compare.inmemorydb.Database;
import com.compare.inmemorydb.DatabaseImpl;
import com.compare.inmemorydb.Schema;
import com.compare.inmemorydb.SchemaImpl;
import com.compare.inmemorydb.Table;
import com.compare.inmemorydb.SourceMessageTable;
import com.compare.model.Message;

public class TestDatabase {

	Database database;
	
	@Before
	public void setupDatabase()
	{
		//Define Table
		Table<String,Message> sourceTable = new SourceMessageTable("SOURCEMESSAGE_TBL");
		Table<String,Message> targetTable = new SourceMessageTable("TARGETMESSAGE_TBL");

		//Define Schema
		Schema schema = new SchemaImpl("MessageSchema");
		
		//Create database
		database = new DatabaseImpl();
		//Create Schema
		database.createSchema(schema);
		schema.setDatabase(database);
		database.connect("MessageSchema");
		//Create table
		schema.createTable(sourceTable);
		schema.createTable(targetTable);
		
		//Setup message
		Message sourceMessage = new Message(UUIDGenerator.next(),"This is a sample text",UUIDGenerator.next(),false,false);
		Message targetMessage = new Message(UUIDGenerator.next(),"This is a sample text",UUIDGenerator.next(),false,false);		
		//Insert message
		sourceTable.insert(sourceMessage);
		sourceTable.setSchema(schema);
		targetTable.insert(targetMessage);
		targetTable.setSchema(schema);
		
		database.disconnect("MessageSchema");
	}
	
	@Test
	public void testDatabaseCreation()
	{
		
		Schema schema = database.connect("MessageSchema");
		List<Message> sourceRecords = schema.getTable("SOURCEMESSAGE_TBL").findAll();
		System.out.println("Records of source table "+sourceRecords);
		
		List<Message> targetRecords = schema.getTable("TARGETMESSAGE_TBL").findAll();
		System.out.println("Records of target table "+targetRecords);
		
		database.disconnect("MessageSchema");
	}
	
	@After
	public void tearDown()
	{
		Schema schema = database.connect("MessageSchema");
		schema.drop("SOURCEMESSAGE_TBL");
		schema.drop("TARGETMESSAGE_TBL");
		
		database.dropSchema("MessageSchema");
		
		Assert.assertTrue(!database.isAvailable("MessageSchema"));
		
		
	}
}
