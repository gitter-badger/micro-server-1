package org.mx.mserver;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.UUID;

public class SSLSocketThread implements Runnable {

    /**
     * Default size for thread pool
     */
    public static final int DEFAULT_SESSIONPOOLSIZE = 10;
    
    /**
     * The address on which to listen
     */
    private ListeningAddress listeningAddress = null;
    
    /**
     * The socket doing the listening
     */
    private SSLServerSocket serversocket;

    /**
     * The socket doing the listening
     */
    private SSLServerSocket publicSocket;

    /**
     * True if this OAuthPublicSSLSocketThread has received instructions to shut down
     */
    private boolean shutdown = false;
    
    /**
     * A pool of NGSessions ready to handle client connections
     */
    private MicroSessionPool sessionPool = null;
      
    /**
     * True if this OAuthPublicSSLSocketThread has been started and is accepting connections
     */
    private boolean running = false;
    
    /**
     * <code>System.out</code> at the time of the MXServer's creation
     */
    public final PrintStream out = System.out;
    
    /**
     * <code>System.err</code> at the time of the MXServer's creation
     */
    public final PrintStream err = System.err;
    
    /**
     * <code>System.in</code> at the time of the MXServer's creation
     */
    public final InputStream in = System.in;

    /**
     * Group of Threads with socket
     */
    private HashMap<MicroSession, Thread> microSessionHashMap = new HashMap<MicroSession, Thread>();



    public SSLSocketThread(ListeningAddress listeningAddress, int sessionPoolSize, int timeoutMillis) {
        init(listeningAddress, sessionPoolSize, timeoutMillis);
    }

    /**
     * Sets up the OAuthPublicSSLSocketThread internals
     *
     * @param listeningAddress the address to bind to
     * @param sessionPoolSize the max number of idle sessions allowed by the
     * pool
     */
    private void init(ListeningAddress listeningAddress, int sessionPoolSize, int timeoutMillis) {
        this.listeningAddress = listeningAddress;

        sessionPool = new MicroSessionPool(sessionPoolSize, this);
    }
    
    /**
     * Listens for new connections and launches NGSession threads to process
     * them.
     */
    public void run() {
        running = true;
        MicroSession sessionOnDeck = null;

        synchronized (System.in) {
            if (!(System.in instanceof ThreadLocalInputStream)) {
                System.setIn(new ThreadLocalInputStream(in));
                System.setOut(new ThreadLocalPrintStream(out));
                System.setErr(new ThreadLocalPrintStream(err));
            }
        }

        try {
        	SSLServerSocketFactory factory=(SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        	serversocket = (SSLServerSocket) factory.createServerSocket(listeningAddress.getPort(),0, listeningAddress.getPublicInetAddress());
        	
            while (!shutdown) {
                sessionOnDeck = sessionPool.take();
                SSLSocket socket = (SSLSocket)serversocket.accept();
                sessionOnDeck.run(socket);
            }

        } catch (Throwable t) {
            // if shutdown is called while the accept() method is blocking,
            // an exception will be thrown that we don't care about.  filter
            // those out.
            if (!shutdown) {
                t.printStackTrace();
            }
        }
        if (sessionOnDeck != null) {
            sessionOnDeck.shutdown();
        }
        running = false;
    }

    /**
     * <p>Shuts down the server. The server will stop listening and its thread
     * will finish.</p>
     *
     */
    public void shutdown() {
        synchronized (this) {
            if (shutdown) {
                return;
            }
            shutdown = true;
        }

        try {
            serversocket.close();
        } catch (Throwable toDiscard) {
        }

        sessionPool.shutdown();

    }

    public boolean isRunning() {
        return running;
    }

    public void addMicroSession(MicroSession microSession, Thread t){
        microSessionHashMap.put(microSession, t);
    }

    public HashMap<MicroSession, Thread> getMicroSessionHashMap(){
        return microSessionHashMap;
    }

}
