package com.compare.queue;

import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.compare.akka.CompareJobService;
import com.compare.akka.QueueType;
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

public class TestQueueReceive implements MessageListener {
	public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
	//public final static String JMS_FACTORY = "jms/SourceConnectionFactory";
	//public final static String QUEUE = "jms/MySourceQueue";
	private QueueConnectionFactory qconFactory;
	private QueueConnection qcon;
	private QueueSession qsession;
	private QueueReceiver qreceiver;
	private Queue queue;
	public boolean quit = false;
	
	private QueueType type;
	
	private final String schemaName = "MessageSchema";
	private Database database;
	
	private CompareJobService jobService;
	
	public TestQueueReceive(Database database)
	{
		this.database = database;
	}

	public void onMessage(Message msg) {
		try {
			String msgText;
			if (msg instanceof TextMessage) {
				msgText = ((TextMessage) msg).getText();
			} else {
				msgText = msg.toString();
			}
			String corelationId = ((TextMessage)msg).getJMSCorrelationID();
			
			sendAkkaServiceThe(msgText, corelationId);
			System.out.println("\n\t&lt;Msg_Receiver&gt; " + msgText +" with corelation id : "+corelationId);
			if (msgText.equalsIgnoreCase("quit")) {
				synchronized (this) {
					quit = true;
					this.notifyAll(); // Notify main thread to quit
				}
			}
		} catch (JMSException jmse) {
			jmse.printStackTrace();
		}
	}

	private void sendAkkaServiceThe(String msgText, String corelationId) {
		com.compare.model.Message message = new com.compare.model.Message(UUIDGenerator.next(),msgText,corelationId);
		jobService.recieveMessage(message);
	}

	public void init(Context ctx, String jmsConnectionFactory, String queueName, String queueType) throws NamingException,
			JMSException {
		qconFactory = (QueueConnectionFactory) ctx.lookup(jmsConnectionFactory);
		qcon = qconFactory.createQueueConnection();
		qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		queue = (Queue) ctx.lookup(queueName);
		qreceiver = qsession.createReceiver(queue);
		qreceiver.setMessageListener(this);
		qcon.start();

		//set queue type
		type = QueueType.valueOf(queueType);
		
		Table table = setBaseTable();

		
		//Job Service Initalization
		Repository repository = new InMemoryRepository(table, type);
		jobService = new CompareJobService(repository);
		jobService.startProcessing();
		
		System.out
		.println("JMS "+ type.name() +" Ready To Receive Messages (To quit, send a \"quit\" message from QueueSender.class).");
		
		synchronized (this) {
			while (!this.quit) {
				try {
					this.wait();
				} catch (InterruptedException ie) {
				}
			}
		}
		this.close();
	}

	private Table setBaseTable() {
		
		Schema schema = database.connect(schemaName);
		Table sourceTable = schema.getTable("SOURCEMESSAGE_TBL");
		Table targetTable = schema.getTable("TARGETMESSAGE_TBL");
		
		//RETURN TABLE
		if(type.equals(QueueType.SOURCE))
		{
			return sourceTable;
		} else if(type.equals(QueueType.TARGET))
		{
			return targetTable;
		}
		
		return null;
	}

	public void close() throws JMSException {
		qreceiver.close();
		qsession.close();
		qcon.close();
		
		//Stop job service
		jobService.shutDown();
	}

/*	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.out.println("Usage: java QueueReceive WebLogicURL JMSCONNECTIONFACTORY QUEUENAME QUEUETYPE(SOURCE,TARGET)");
			return;
		}
		InitialContext ic = getInitialContext(args[0]);
		TestQueueReceive qr = new TestQueueReceive();
		qr.init(ic, args[1], args[2], args[3]);
		System.out
				.println("JMS Ready To Receive Messages (To quit, send a \"quit\" message from QueueSender.class).");
		// Wait until a "quit" message has been received.
		synchronized (qr) {
			while (!qr.quit) {
				try {
					qr.wait();
				} catch (InterruptedException ie) {
				}
			}
		}
		qr.close();
	}*/

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static InitialContext getInitialContext(String url)
			throws NamingException {
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
		env.put(Context.PROVIDER_URL, url);
		return new InitialContext(env);
	}
}