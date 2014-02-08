package com.compare.akka;

import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;

public class SystemShutdownOverWatch extends UntypedActor {
	  
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SystemShutdownOverWatch.class);

	  Map<ActorRef, Boolean> stopResults = new HashMap<ActorRef, Boolean>();
	  ActorRef wJobController;
	  
	  public SystemShutdownOverWatch(ActorRef jobController)
	  {
	    this.wJobController = jobController;
	    getContext().watch(wJobController);
	    stopResults.put(wJobController, false);
	  }
	  
	  @Override
	  public void preRestart(Throwable reason, scala.Option<Object> message) throws Exception {
	      if (message.isDefined()) {
	    	  LOG.warn("Restarting " + self() + " because of " + reason + " while handling " + message.get(),reason);
	      } else {
	    	  LOG.warn("Restarting " + self() + " because of " + reason,reason);
	      }
	      super.preRestart(reason, message);
	  } 
	  
	  @Override
	  public void onReceive(Object message) throws Exception {
	    // TODO Auto-generated method stub
	    if(message instanceof Terminated)
	    {
	      //On recieving the poison pill from listener stop the actor system
	      Terminated terminated = (Terminated)message;
	      if(terminated.getActor().equals(wJobController))
	      {
	        LOG.info("Terminated "+((Terminated)message).getActor().path());
	        stopResults.put(wJobController, true);
	      }
	      
	      if(!stopResults.values().contains(false))
	      {
	        System.out.println("System terminated");
	        getContext().system().shutdown();
	      }
	    }
	  }
	}
