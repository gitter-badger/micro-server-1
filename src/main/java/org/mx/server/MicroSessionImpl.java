package org.mx.server;


import javax.net.ssl.SSLSocket;

/**
 * Created by fsbsilva on 9/26/16.
 */
public abstract class MicroSessionImpl implements Runnable {

    /**
     * Synchronization object
     */
    private Object lock = new Object();

    /**
     * The next socket this MicroSession has been tasked with processing (by
     * MicroServerGateway)
     */
    private SSLSocket nextSocket = null;

    /**
     * True if the server has been shutdown and this MicroSession should terminate
     * completely
     */
    private boolean done = false;

    /**
     * Curr Thread Object
     */
    private Thread currThread = null;

    /**
     * The instance number of this MicroSession. That is, if this is the Nth
     * MXSession to be created, then this is the value for N.
     */
    private long instanceNumber = 0;


    /**
     * A lock shared among all MicroSession
     */
    private static Object sharedLock = new Object();
    /**
     * The instance counter shared among all MicroSession
     */
    private static long instanceCounter = 0;

    private String UUID;

    /**
     * The pool this MicroSession came from, and to which it will return itself
     */
    protected MicroSessionPool sessionPool = null;

    protected SSLSocketThread sslSocketThread = null;

    protected boolean isIdle = true;
    /**
     * Creates a new MicroSession running for the specified MicroSessionPool and
     * MXServer.
     *
     */
    MicroSessionImpl(MicroSessionPool sessionPool, SSLSocketThread sslSocketThread) {
        this.sessionPool = sessionPool;
        this.sslSocketThread = sslSocketThread;

        synchronized (sharedLock) {
            this.instanceNumber = ++instanceCounter;
        }
    }


    /**
     * Returns the next socket to process. This will block the MicroSession thread
     * until there's a socket to process or the MicroSession has been shut down.
     *
     * @return the next socket to process, or
     * <code>null</code> if the MicroSession has been shut down.
     */
    protected SSLSocket nextSocket() {
        SSLSocket result = null;
        synchronized (lock) {
            result = nextSocket;
            while (!done && result == null) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    done = true;
                }
                result = nextSocket;
            }
            nextSocket = null;
            isIdle = false;
        }
        return (result);
    }

    /**
     * Shuts down this MicroSession gracefully
     */
    public void shutdown() {
        done = true;
        synchronized (lock) {
            nextSocket = null;
            lock.notifyAll();
        }
    }

    /**
     * Instructs this MicroSession to process the specified socket, after which
     * this MicroSession will return itself to the pool from which it came.
     *
     * @param socket the socket (connected to a client) to process
     */
    public void run(SSLSocket socket) {
        synchronized (lock) {
            nextSocket = socket;
            lock.notify();
        }
        Thread.yield();
    }

    public void setCurrThread(Thread currThread) {
        this.currThread = currThread;
    }

    /**
     * Updates the current thread name (useful for debugging).
     */
    protected void updateThreadName(String detail ) {
        currThread.setName("MicroSession " + instanceNumber + ": " + ((detail == null) ? "(idle)" : detail));
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public void setIdle(boolean isIdle){
        this.isIdle = isIdle;
    }

    public boolean isIdle(){
        return this.isIdle;
    }

}
