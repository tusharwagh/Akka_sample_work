package com.compare.akka;

import com.compare.base.JobContext;
import com.compare.base.Repository;
import com.typesafe.config.Config;

public class WorkerJobContext implements JobContext {

	public WorkerJobContext()
	{
		
	}
	
	@Override
	public Config config() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Repository repository() {
		// TODO Auto-generated method stub
		return null;
	}

}
