package org.mx.mserver;


public interface MicroClientListener {

    public void clientDisconnected() throws InterruptedException;
}
