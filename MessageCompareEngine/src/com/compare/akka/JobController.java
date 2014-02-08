package com.compare.akka;

import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.compare.base.Repository;
import com.compare.model.Message;
import com.compare.model.QueueType;
import com.typesafe.config.Config;

public class JobController extends UntypedActor {

	 //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JobController.class);
	 
	private final Repository repository;
	
	private boolean active = false;
	
	private final ActorRef executor;
	
	public JobController(Repository repository)
	{
		this.repository = repository;
		
	    this.executor = createExecutor();
	}


	private ActorRef createExecutor() {
		return this.getContext().actorOf(Props.create(Executor.class,repository),"job-executor");
	}


	  @Override
	  public void preRestart(Throwable reason, scala.Option<Object> message) throws Exception {
	      if (message.isDefined()) {
	    	  System.out.println("Restarting " + self() + " because of " + reason + " while handling " + message.get()+" with reason "+reason);
	      } else {
	    	  System.out.println("Restarting " + self() + " because of " + reason);
	      }
	      super.preRestart(reason, message);
	  } 	
	
	public static class StartProcessing{}
	
	@Override
	public void onReceive(Object message) throws Exception {
		if(message instanceof StoreMessage)
		{
			Message toBeStoredMessage = ((StoreMessage)message).getMessage();
			repository.insertMessage(toBeStoredMessage);
			System.out.println(repository.type()+" : going to store the message "+toBeStoredMessage);
		} else if(message instanceof StartProcessing)
		{
			System.out.println("Started job processing");
			//Only activate if this is a TARGET QUEUE execution
			//as we want to compare the target with the source to get the results
			if(repository.type().equals(QueueType.TARGET))
			{
				active=true;
				sendNewTriggerToMyselfIn(config().getMilliseconds("com.compare.message.job.config.startupDelay"));
			}
			//start executing the messages
		} else if (message instanceof PeriodicTrigger) {
		      if (active) {
		    	  System.out.println("PeriodicTrigger received, waking up monitor and executor");
		          executor.tell(new Executor.DoExecutePendingJobs(),self());
		          long scheduleRate = config().getMilliseconds("com.compare.message.job.config.scheduleRate");
		          sendNewTriggerToMyselfIn(scheduleRate);
		        } else {
		          System.out.println("PeriodicTrigger received while inactive");
		        }
		} else if(message instanceof GracefulStop)
		{
			active=false;
			executor.tell(new GracefulStop(),self());
		} else {
	        unhandled(message);
	    }
	}

	  private Config config() {
		    return context().system().settings().config();
		  }  
	  
	  private void sendNewTriggerToMyselfIn(long milliSeconds) {
		    getContext().system().scheduler().scheduleOnce(
		        Duration.create(milliSeconds, TimeUnit.MILLISECONDS),
		        new Runnable() {
		          @Override
		          public void run() {
		            getSelf().tell(new PeriodicTrigger(), ActorRef.noSender());
		           }
		         }, getContext().system().dispatcher());
		}
	
}
