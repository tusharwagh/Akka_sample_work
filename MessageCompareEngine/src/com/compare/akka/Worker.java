package com.compare.akka;

import scala.concurrent.duration.Duration;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.SupervisorStrategy.Directive;
import akka.actor.UntypedActor;
import akka.japi.Function;

import com.compare.base.Repository;
import com.compare.model.Message;

public class Worker extends UntypedActor {

	//private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Executor.class);
	
	private final Repository repository;
	
	public Worker(Repository repository)
	{
		this.repository = repository;
	}
	
	  @Override
	  public void preRestart(Throwable reason, scala.Option<Object> message) throws Exception {
	      if (message.isDefined()) {
	    	  System.out.println("Restarting " + self() + " because of " + reason + " while handling " + message.get() +" with reason "+reason.getStackTrace());
	      } else {
	    	  System.out.println("Restarting " + self() + " because of " + reason.getStackTrace());
	      }
	      super.preRestart(reason, message);
	  } 
	
	  private static final SupervisorStrategy supervisorStrategy = new OneForOneStrategy(0, Duration.Inf(),
		      new Function<Throwable, Directive>() {
		        @Override
		        public Directive apply(Throwable t) {
		          if (t instanceof Exception) {
		            // Stop user created worker actors in case of errors
		            return SupervisorStrategy.stop();
		          } else {
		            // escalate Errors
		            return SupervisorStrategy.escalate();
		          }
		        }
		      });

		  @Override
		  public SupervisorStrategy supervisorStrategy() {
		    return supervisorStrategy;
		  }	  
	  
	@Override
	public void onReceive(Object message) throws Exception {
		if(message instanceof Message)
		{
			//Execute message.. I need to have a root handler which will pass to the right handler based on the message type
			new CompareHandler().compareTargetWithSource(message, repository);
			sender().tell(new WorkFinished((Message)message), self());
		}

	}

}
