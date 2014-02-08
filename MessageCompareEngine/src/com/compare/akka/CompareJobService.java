package com.compare.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;

import com.compare.base.Repository;
import com.compare.model.Message;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Main job service
 * @author F297792
 *
 */
public class CompareJobService {
	
	boolean serviceStarted = false;
	
	private ActorSystem actorSystem;
	
	private ActorRef jobController;
	
	private ActorRef shutdownWatcher;
	
	private final Repository repository;
	
	private final Config config;
	
	public CompareJobService(Repository repository)
	{
		this.repository = repository;
		this.config = getDefaultConfiguration();
	}
	
	private void init()
	{
	    if (actorSystem != null) {
	        throw new IllegalStateException("already initialized");
	    }
	    String jobProcessingSystem = config.getString("com.compare.message.job.config.processingSystem");
	    actorSystem = ActorSystem.create(jobProcessingSystem, config);
	    
	    // Create Job Controller
	    jobController = createJobController();
	    
	    // Create Shutdown watcher
	    shutdownWatcher = createShutDownWatcher();
	    
	}
	
	  private static Config getDefaultConfiguration() {
		    return ConfigFactory.load();
		}
	
	  private ActorRef createJobController() {
		    return actorSystem.actorOf(Props.create(JobController.class,repository),"job-controller");
		}	  
	  
	  private ActorRef createShutDownWatcher() {    
		    return actorSystem.actorOf(Props.create(SystemShutdownOverWatch.class,jobController),"shutdown-watcher");
		  }

	  
	public void startProcessing()
	{
		init();
		jobController.tell(new JobController.StartProcessing(), ActorRef.noSender());
		serviceStarted = true;
	}
	
	private void stopProcessing()
	{
		jobController.tell(new GracefulStop(), ActorRef.noSender());
		serviceStarted = false;
	}
	
	public boolean isServiceStarted()
	{
		return serviceStarted;
	}
	public void recieveMessage(Message message)	
	{
		if(!serviceStarted)
		{
			throw new IllegalArgumentException("Service not yet initialized ");
		}
			
		StoreMessage storedMessage = new StoreMessage(message);
		jobController.tell(storedMessage, ActorRef.noSender());
	}
	
	public void shutDown()
	{
		stopProcessing();
		jobController.tell(PoisonPill.getInstance(), ActorRef.noSender());
	}

}
