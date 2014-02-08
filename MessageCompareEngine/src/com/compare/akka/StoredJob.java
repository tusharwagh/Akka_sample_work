package com.compare.akka;

import com.compare.base.Job;
import com.compare.base.JobContext;

public class StoredJob implements Job {

	@Override
	public boolean isAppropriateHandler(Object jobHandler) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void executeWith(Object jobHandler, JobContext context) {
		// TODO Auto-generated method stub

	}
	
	

}
