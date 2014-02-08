package com.compare.akka;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.SupervisorStrategy.Directive;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.dispatch.OnFailure;
import akka.japi.Function;
import akka.pattern.Patterns;
import akka.routing.Broadcast;
import akka.routing.RoundRobinRouter;

import com.compare.base.Repository;
import com.compare.model.Message;
import com.typesafe.config.Config;

public class Executor extends UntypedActor {
	
	
	private boolean gracefulStopping = false;
	
	private List<Message> activeMessages = new ArrayList<Message>();
	
	private final Repository repository;
	
	private final ActorRef workerRouter;

	public Executor(Repository repository)
	{
	
		this.repository = repository;
		
		this.workerRouter = createWorkerRouter();
		
		
	}
	
	  private ActorRef createWorkerRouter() {
		  
		    SupervisorStrategy strategy = new OneForOneStrategy(config().getInt(
		        "com.compare.message.job.config.maxHandlerRetries"), Duration.Inf(),
		        new Function<Throwable, Directive>() {
		          @Override
		          public Directive apply(Throwable t) {
		            if (t instanceof Exception) {
		              System.out.println("received an exception"+(Exception)t);
		              // restart worker on normal Exception
		              return SupervisorStrategy.restart();
		            } else {
		              // escalate Errors
		              return SupervisorStrategy.escalate();
		            }
		          }
		        });
		    int workerPoolSize = config().getInt("com.compare.message.job.config.workerPoolSize");
		    return this.getContext().actorOf(
		        Props.create(Worker.class,repository).withRouter(new RoundRobinRouter(workerPoolSize).withSupervisorStrategy(strategy))
		            .withDispatcher("akka.actor.compare-dispatcher"), "workerRouter");
	  }
	
  @Override
  public void preRestart(Throwable reason, scala.Option<Object> message) throws Exception {
      if (message.isDefined()) {
    	  System.out.println("Restarting " + self() + " with " + activeMessages.size() + " active jobs because of " + reason + " while handling " + message.get()+" for reason "+reason);
      } else {
    	  System.out.println("Restarting " + self() + " with " + activeMessages.size() + " active jobs because of " + reason);
      }
      super.preRestart(reason, message);
  } 
	
	public static class DoExecutePendingJobs{}
	
	@Override
	public void onReceive(Object message) throws Exception {
		if(message instanceof DoExecutePendingJobs)
		{
			System.out.println("executing pending jobs "+repository.getPendingMessages().size());
			executeAll(repository.getPendingMessages());
			
		} else if(message instanceof WorkFinished)
		{
			WorkFinished finished = (WorkFinished)message;
			activeMessages.remove(finished.getMessage());
			
		} else if(message instanceof GracefulStop)
		{
			gracefulStopping = true;
			System.out.println("Gracefully stopping executor");
			if (activeMessages.isEmpty()) {
		          System.out.println("No more active events, stopping Executor...");
		          //send a poison pill to all the routees to stop
		          workerRouter.tell(new Broadcast(PoisonPill.getInstance()), getSelf());
		      }			
		} else if (message instanceof Terminated) {
		      //On recieving the poison pill from listener stop the actor system
		      Terminated terminated = (Terminated)message;
		      if(terminated.getActor().equals(workerRouter)) {
		        System.out.println("Terminated "+((Terminated)message).getActor().path());
		        getContext().stop(self());
		      }
		} else {
	        unhandled(message);
	    }
		
	}

	  private void executeAll(List<Message> pendingJobs) {
		for(Message message : pendingJobs)
		{
			execute(message);
		}
		
	}

	private void execute(Message message) {
		if(activeMessages.contains(message))
		{
			System.out.println("Message is already been executed "+message);
			return;
		}
		
		activeMessages.add(message);
		
	    long timeoutMillis = getDefaultTimeoutMillis();
	    
	    final ExecutionContext ec = context().system().dispatcher();
	    Future<Object> f = Patterns.ask(workerRouter, message, timeoutMillis);
	    f.onFailure(
	        afterTimeout(self(), message, timeoutMillis),ec);
	    Patterns.pipe(f,ec).to(self());//pipeTo$default$2(self());

		
	}

	  private long getDefaultTimeoutMillis() {
		    return config().getMilliseconds("com.compare.message.job.config.defaultJobTimeout");
		  }
	  
	private Config config() {
		    return context().system().settings().config();
		  }  
	
	  private static OnFailure afterTimeout(final ActorRef executor, final Message message, final long timeoutMillis) {
		    
		    return new OnFailure() {
		      @Override
		      public void onFailure(Throwable throwable) throws Throwable {
		          //Except for the timeout errors all the errors will be received as part of the Result 
		          TimeoutException timeoutException = new TimeoutException("Job did not finish within " + timeoutMillis + "ms");
		          System.out.println(timeoutException.getMessage());
		          executor.tell(new WorkFinished(message, timeoutException),ActorRef.noSender());          
		      }
		    };
		  }	
}
