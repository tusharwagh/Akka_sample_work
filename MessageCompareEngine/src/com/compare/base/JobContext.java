package com.compare.base;

import com.typesafe.config.Config;

public interface JobContext {
	
	  /**
	   * @return current config
	   */
	  Config config();
	  
	  /**
	   * @return current config
	   */
	  Repository repository();

}
