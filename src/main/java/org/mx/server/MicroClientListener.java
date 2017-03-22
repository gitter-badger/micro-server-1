package org.mx.server;


public interface MicroClientListener {

    public void clientDisconnected() throws InterruptedException;
}
