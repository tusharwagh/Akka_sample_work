package com.compare.queue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.compare.base.UUIDGenerator;
import com.compare.inmemorydb.Database;
import com.compare.inmemorydb.DatabaseImpl;
import com.compare.inmemorydb.Schema;
import com.compare.inmemorydb.SchemaImpl;
import com.compare.inmemorydb.SourceMessageTable;
import com.compare.inmemorydb.Table;
import com.compare.inmemorydb.TargetMessageTable;

public class CompareMain {

	public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";

	private final String schemaName = "MessageSchema";
	private Database database;

	public static void main(String[] args) throws Exception {

		String synch = "";
		if (args.length != 3) {
			System.out
					.println("Usage: java QueueReceive WebLogicURL SOURCEJMSCONNECTIONFACTORY,SOURCEQUEUENAME,QUEUETYPE(SOURCE) TARGETJMSCONNECTIONFACTORY,TARGETQUEUENAME,QUEUETYPE(TARGET)");
			return;
		}
		final InitialContext ic = getInitialContext(args[0]);
		final String[] sourceParam = args[1].split(",");
		final String[] targetParam = args[2].split(",");

		CompareMain multiQueue = new CompareMain();
		InputStream stream = multiQueue.getClass().getResourceAsStream("inputMessage.txt");
		Database database = multiQueue.initializeDatabase();
		final MultiQueueReceive qrSource = new MultiQueueReceive(database);
		final MultiQueueReceive qrTarget = new MultiQueueReceive(database);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					qrSource.init(ic, sourceParam[0], sourceParam[1],
							sourceParam[2]);
				} catch (NamingException e) {
					e.printStackTrace();
				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					qrTarget.init(ic, targetParam[0], targetParam[1],
							targetParam[2]);
				} catch (NamingException e) {
					e.printStackTrace();
				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();

		final MultiQueueSend sendSource = new MultiQueueSend();
		sendSource.init(ic, sourceParam[0], sourceParam[1], sourceParam[2]);
		final MultiQueueSend sendTarget = new MultiQueueSend();
		sendTarget.init(ic, targetParam[0], targetParam[1], targetParam[2]);

		
		readAndSend(sendSource, sendTarget,stream);
		
		try {
			Thread.currentThread().sleep(10000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static InitialContext getInitialContext(String url)
			throws NamingException {
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
		env.put(Context.PROVIDER_URL, url);
		return new InitialContext(env);
	}

	private Database initializeDatabase() {
		// Initialize Database
		// Create database
		String schemaName = "MessageSchema";
		Schema schema = new SchemaImpl(schemaName);
		Database database = new DatabaseImpl();
		// Create Schema
		database.createSchema(schema);
		schema.setDatabase(database);
		database.connect(schemaName);
		// Define Table
		Table<String, com.compare.model.Message> sourceTable = new SourceMessageTable(
				"SOURCEMESSAGE_TBL");
		Table<String, com.compare.model.Message> targetTable = new TargetMessageTable(
				"TARGETMESSAGE_TBL");
		// Create table
		schema.createTable(sourceTable);
		schema.createTable(targetTable);
		sourceTable.setSchema(schema);
		targetTable.setSchema(schema);

		return database;
	}

/*	private static void readAndSend(MultiQueueSend qsSource,
			MultiQueueSend qsTarget) throws IOException, JMSException {
		String th = " : Test Message Body with counter = ";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		boolean readFlag = true;
		System.out
				.println("\n\tStart Sending Messages (Enter QUIT to Stop):\n");
		while (readFlag) {
			System.out.print("&lt;Msg_Sender&gt; ");
			String msg = br.readLine();
			String corelationId = UUIDGenerator.next();
			if (msg.equals("QUIT") || msg.equals("quit")) {
				qsSource.send(msg, corelationId);
				qsTarget.send(msg, corelationId);
				System.exit(0);
			}
			qsSource.send(msg, corelationId);
			qsTarget.send(msg, corelationId);
			System.out.println();
		}
		br.close();
	}*/

	private static void readAndSend(MultiQueueSend qsSource,
			MultiQueueSend qsTarget, InputStream stream) throws IOException, JMSException {		
		
		//Wait for akka service to get initialized
		try {
			Thread.currentThread().sleep(10000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String th = " : Test Message Body with counter = ";
		//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		boolean readFlag = true;
		System.out
				.println("\n\tStart Sending Messages (Enter QUIT to Stop):\n");
		while (readFlag) {
			String msg = br.readLine();			
			System.out.print("&lt;Msg_Sender&gt; "+msg);

			String corelationId = UUIDGenerator.next();
			if (msg.equals("QUIT") || msg.equals("quit")) {
				qsSource.send(msg, corelationId);
				qsTarget.send(msg, corelationId);
				readFlag = false;
				//System.exit(0);
			}
			qsSource.send(msg, corelationId);
			qsTarget.send(msg, corelationId);
			System.out.println();
		}
		br.close();
	}
}
