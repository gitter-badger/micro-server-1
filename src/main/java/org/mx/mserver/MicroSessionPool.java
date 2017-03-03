package org.mx.mserver;

/*   

Copyright 2016, MXDeploy, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
* Provides MicroSessionPool pooling functionality.  One parameter, "maxIdle",
* governs its behavior by setting the maximum number of idle MicroSession
* threads it will allow.  It creates a pool of size maxIdle - 1, because
* one MicroSession is kept "on deck" by the MicroServerGateway in order to eke out
* a little extra responsiveness.
* 
* @author <a href="http://www.mxdeploy.com">Fabio S B da Silva</a>
*/
class MicroSessionPool {

	/**
	 * number of sessions to store in the pool
	 */
	int poolSize = 0;

	/**
	 * the pool itself
	 */
	MicroSession[] pool = null;
	
	/**
	 * The number of sessions currently in the pool
	 */
	int poolEntries = 0;

	/**
	 * reference to server we're working for
	 */
	//MicroServerGateway server = null;
	
	/**
	 * have we been shut down?
	 */
	boolean done = false;
	
	/**
	 * MicroClientSession Type ( private or public )
	 */	
	String type = "private";
	
	/**
	 * synchronization object
	 */
	private Object lock = new Object();

	private SSLSocketThread sslSocketThread = null;

	/**
	 * Creates a new OAuthSessionRunner operating for the specified server, with
	 * the specified number of threads
	 * @param poolsize the maximum number of idle threads to allow
	 */
	MicroSessionPool(int poolsize, SSLSocketThread sslSocketThread) {
		this.poolSize = Math.min(0, poolsize);
		this.sslSocketThread = sslSocketThread;

		if( type.equals("private") ){
			pool = new MicroSession[poolSize];
		}
		poolEntries = 0;
	}

	/**
	 * Returns an MicroSession from the pool, or creates one if necessary
	 * @return an MicroSession ready to work
	 */
	MicroSession take() {
		MicroSession result = null;
		synchronized(lock) {
			if (poolEntries == 0) {
				result = new MicroSession(this, sslSocketThread);
				Thread t = new Thread(result);
				result.setCurrThread(t);
                String uuID = UUID.randomUUID().toString();
                result.setUUID(uuID);
                sslSocketThread.addMicroSession(result,t);
				t.start();
			} else {
				--poolEntries;
				result = pool[poolEntries];
			}
		}
		return (result);
	}
	
	/**
	 * Returns an MicroSession to the pool.  The pool may choose to shutdown
	 * the thread if the pool is full
	 * @param session the MicroSession to return to the pool
	 */
	void give(MicroSession session) {
          boolean shutdown = false;
          synchronized(lock) {
				if (done || poolEntries == poolSize) {
		          shutdown = true;
		        } else {
		          pool[poolEntries] = session;
		          ++poolEntries;
				}
          }
          if (shutdown)session.shutdown();
	}
	
	/**
	 * Shuts down the pool.  Running nails are allowed to finish.
	 */
	void shutdown() {
		done = true;
		synchronized(lock) {
			while (poolEntries > 0) {
				take().shutdown();
			}
		}
	}

}
