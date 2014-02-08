package com.compare.akka;

import com.compare.base.InMemoryRepository;
import com.compare.base.Repository;
import com.compare.inmemorydb.Schema;
import com.compare.inmemorydb.Table;
import com.compare.model.Message;
import com.compare.model.QueueType;

public class CompareHandler{
	
	public void compareTargetWithSource(Object message, Repository baseRepository)
	{
		Schema schema = baseRepository.getSchema();
		Message baseMessage = (Message)message;
		
		if(baseRepository.type().equals(QueueType.TARGET))
		{
			Table sourceTable = schema.getTable("SOURCEMESSAGE_TBL");
			Repository sourceRepository = new InMemoryRepository(sourceTable,QueueType.SOURCE);
			
			Message compareMessage = sourceRepository.getMessageUsing(baseMessage.getCorelationId());
			boolean result = compare(baseMessage,compareMessage);
			Message newSourceMessage = new Message(compareMessage.getUuid(),compareMessage.getMessage(),compareMessage.getCorelationId(),result,true);
			sourceRepository.updateMessage(newSourceMessage);
			Message newBaseMessage = new Message(baseMessage.getUuid(),baseMessage.getMessage(),baseMessage.getCorelationId(),result,true);
			baseRepository.updateMessage(newBaseMessage);
		}
	}
	
	private boolean compare(Message base, Message target)
	{
		String baseMessage = base.getMessage();
		String compareMessage = target.getMessage();
		
		if(baseMessage.equalsIgnoreCase(compareMessage))
		{
			return true;
		}
		return false;
	}

}
