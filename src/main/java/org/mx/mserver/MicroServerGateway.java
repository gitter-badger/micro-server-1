package org.mx.mserver;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mx.mrepo.MicroRepository;
import org.mx.mclient.MicroConstants;
import org.mx.quartz.MicroSchedulerFactory;
import org.mx.maction.MicroAction;
import org.mx.mrepo.MicroRepositoryFactory;
import org.mx.var.MicroVariableMap;
import org.mx.var.MicroVariableFactory;

import java.io.IOException;
import java.util.Map;

/***
* @author <a href="http://www.mxdeploy.com/about">Fabio S B Silva</a>
*/
public class MicroServerGateway {

	private static Logger logger = Logger.getLogger(MicroServerGateway.class);

	private String log4jPath = null;

	/**
	 * True if this MicroServerGateway has received instructions to shut down
	 */
	private boolean shutdown = false;

	public static void main(String args[]) throws IOException {
		(new MicroServerGateway()).start();
	}

	private String PARENT_BASEDIR;
	private String BASEDIR;
    private String MSERVER_NAME=null;
    private MicroServer mserverYml;
	private int timeoutMillis = 10;

	public Map<String, MicroAction> microActionMap;

	public void load() throws IOException {
        PARENT_BASEDIR = System.getProperty("parent.basedir");
		String VAR_YML = PARENT_BASEDIR+"/variables.yml";
        MicroVariableMap variableMap = MicroVariableFactory.parser(VAR_YML);
        GlobalVariableService.setGlobalVariable(variableMap);

        BASEDIR = variableMap.get("basedir");

        // Grab default micro-server name
        MSERVER_NAME = variableMap.get("mserver.name");
        if( System.getProperty("mserver.name") != null ){
            MSERVER_NAME = System.getProperty("mserver.name");
        }

        if( MSERVER_NAME == null ){
            System.out.println("");
            System.out.println("==== Error: Variable or Property mserver.name can't be empty");
            System.out.println("");
            System.exit(0);
        }

        mserverYml = MicroServerFactory.parser(BASEDIR+"/micro-servers.yml").get(MSERVER_NAME);
        GlobalVariableService.setMicroServer(mserverYml);

        MicroVariableMap varActionMap = MicroVariableFactory.parser(mserverYml.getRepositoryPath()+"/variables.yml");
        GlobalVariableService.setActionVariableScope(varActionMap);

        System.setProperty("javax.net.ssl.keyStore",mserverYml.getKeystorePath());
        System.setProperty("javax.net.ssl.keyStorePassword",mserverYml.getKeystorePassword());
        System.setProperty("java.protocol.handler.pkgs",mserverYml.getKeystorePkgs());
        PropertyConfigurator.configure(mserverYml.getLog4jPath());

        MicroRepository microRepository = MicroRepositoryFactory.parser(mserverYml.getRepositoryPath()+"/micro-repository.yml");
        GlobalVariableService.setMicroRepository(microRepository);

	}

	public void start() throws IOException {

		load();

		ListeningAddress listeningAddress = new ListeningAddress(mserverYml.getHost(), mserverYml.getPort());

		SSLSocketThread sslSocketThread = new SSLSocketThread(listeningAddress, MicroConstants.DEFAULT_SESSIONPOOLSIZE, timeoutMillis);
		Thread t2 = new Thread(sslSocketThread);
		t2.setName("SSLSocketThread(" + listeningAddress.toString() + ")");
		t2.start();

		Runtime.getRuntime().addShutdownHook(new ServerShutdowner(sslSocketThread));

        MicroRepository microRepository = GlobalVariableService.getMicroRepository();
        MicroSchedulerFactory.scheduleJobs(microRepository);
	}


	/**
	 * A shutdown hook that will cleanly bring down the MicroServerGateway if it is
	 * interrupted.
	 *
	 * @author <a href="http://www.mxdeploy.com/about">Fabio Santos</a>
	 *
	 */
	private static class ServerShutdowner extends Thread {

		private SSLSocketThread sslSocketThread = null;

		ServerShutdowner(SSLSocketThread sslSocketThread) {
			this.sslSocketThread = sslSocketThread;
		}

		public void run() {

			int count = 0;
			sslSocketThread.shutdown();

			// give the server up to five seconds to stop.  is that enough?
			// remember that the shutdown will call nailShutdown in any
			// nails as well
			while ( sslSocketThread.isRunning() && (count < 50)) {

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				++count;
			}

			if ( sslSocketThread.isRunning() ) {
				System.err.println("Unable to cleanly shutdown server.  Exiting JVM Anyway.");
			} else {
				System.out.println("MXServer shut down.");
			}
		}
	}

	public void shutdown(){
		System.exit(0);
	}

}
