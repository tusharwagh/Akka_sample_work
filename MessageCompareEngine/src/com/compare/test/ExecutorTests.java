package com.compare.test;

import org.junit.Before;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.compare.akka.Executor;
import com.compare.base.InMemoryRepository;
import com.compare.base.Repository;
import com.compare.base.UUIDGenerator;
import com.compare.inmemorydb.Database;
import com.compare.inmemorydb.DatabaseImpl;
import com.compare.inmemorydb.Schema;
import com.compare.inmemorydb.SchemaImpl;
import com.compare.inmemorydb.SourceMessageTable;
import com.compare.inmemorydb.Table;
import com.compare.inmemorydb.TargetMessageTable;
import com.compare.model.Message;
import com.compare.model.QueueType;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ExecutorTests {
	
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
	public void testExecutor()
	{
		Config akkaConfig = ConfigFactory.load();
	    String jobProcessingSystem = akkaConfig.getString("com.compare.message.job.config.processingSystem");
	    ActorSystem actorSystem = ActorSystem.create(jobProcessingSystem, akkaConfig);
		Table table = initializeDatabase(QueueType.SOURCE);

		
		//Job Service Initalization
		Repository repository = new InMemoryRepository(table, QueueType.SOURCE);
		
		ActorRef executor = actorSystem.actorOf(Props.create(Executor.class,repository),"executor");

		executor.tell(new Executor.DoExecutePendingJobs(), ActorRef.noSender());
		
		try {
			Thread.currentThread().sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private Table initializeDatabase(QueueType type) {
		
		String schemaName = "MessageSchema";
		//Initialize Database
		//Create database
		Schema schema = new SchemaImpl(schemaName);		
		database = new DatabaseImpl();
		//Create Schema
		database.createSchema(schema);
		schema.setDatabase(database);
		database.connect(schemaName);
		//Define Table
		Table<String, com.compare.model.Message> sourceTable = new SourceMessageTable("SOURCEMESSAGE_TBL");
		Table<String, com.compare.model.Message> targetTable = new TargetMessageTable("TARGETMESSAGE_TBL");		
		//Create table
		schema.createTable(sourceTable);
		schema.createTable(targetTable);
		
		//RETURN TABLE
		if(type.equals(QueueType.SOURCE))
		{
			return sourceTable;
		} else if(type.equals(QueueType.SOURCE))
		{
			return targetTable;
		}
		
		return null;
	}
}
