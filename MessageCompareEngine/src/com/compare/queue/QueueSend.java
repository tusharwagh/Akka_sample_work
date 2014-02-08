package com.compare.queue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.compare.base.UUIDGenerator;
import com.compare.model.QueueType;

public class QueueSend {
	public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
	//public final static String JMS_FACTORY = "jms/SourceConnectionFactory";
	//public final static String QUEUE = "jms/MySourceQueue";

	private QueueConnectionFactory qconFactory;
	private QueueConnection qcon;
	private QueueSession qsession;
	private QueueSender qsender;
	private Queue queue;
	private TextMessage msg;
	private QueueType type; 

	public void init(Context ctx, String jmsConnectionFactory, String queueName, String queueType ) throws NamingException,
			JMSException {
		qconFactory = (QueueConnectionFactory) ctx.lookup(jmsConnectionFactory);
		qcon = qconFactory.createQueueConnection();
		qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		queue = (Queue) ctx.lookup(queueName);
		qsender = qsession.createSender(queue);
		msg = qsession.createTextMessage();
		type = QueueType.valueOf(queueType);
		qcon.start();
	}

	public void send(String message) throws JMSException {
		msg.setText(message);
		String corelationId = UUIDGenerator.next();
		msg.setJMSCorrelationID(corelationId);
		qsender.send(msg);
	}

	public void close() throws JMSException {
		qsender.close();
		qsession.close();
		qcon.close();
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.out.println("Usage: java QueueSend WebLogicURL JMSCONNECTIONFACTORY QUEUENAME QUEUETYPE(SOURCE,TARGET)");
			return;
		}
		InitialContext ic = getInitialContext(args[0]);
		QueueSend qs = new QueueSend();
		qs.init(ic, args[1], args[2], args[3]);
		readAndSend(qs);
		qs.close();
	}

	private static void readAndSend(QueueSend qs) throws IOException,
			JMSException {
		String line = "Test Message Body with counter = ";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		boolean readFlag = true;
		System.out
				.println("\n\tStart Sending Messages (Enter QUIT to Stop):\n");
		while (readFlag) {
			System.out.print("&lt;Msg_Sender&gt; ");
			String msg = br.readLine();
			if (msg.equals("QUIT") || msg.equals("quit")) {
				qs.send(msg);
				System.exit(0);
			}
			qs.send(msg);
			System.out.println();
		}
		br.close();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static InitialContext getInitialContext(String url)
			throws NamingException {
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
		env.put(Context.PROVIDER_URL, url);
		return new InitialContext(env);
	}
}