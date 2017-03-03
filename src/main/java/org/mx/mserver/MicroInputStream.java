package org.mx.mserver;

import org.mx.mclient.MicroConstants;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

public class MicroInputStream extends FilterInputStream implements Closeable {

    private final ExecutorService executor;
    private final DataInputStream din;
    private InputStream stdin = null;
    private boolean eof = false;
    private long remaining = 0;
    private byte[] oneByteBuffer = null;
    private final DataOutputStream out;
    private boolean started = false;
    private long lastReadTime = System.currentTimeMillis();
    private final Future readFuture;
    private final Set clientListeners = new HashSet();
    private final Set heartbeatListeners = new HashSet();
    private final int heartbeatTimeoutMillis;

    public MicroInputStream(
            InputStream in,
            DataOutputStream out,
            final PrintStream serverLog,
            final int heartbeatTimeoutMillis) {
        super(in);
        din = (DataInputStream) this.in;
        this.out = out;
        this.heartbeatTimeoutMillis = heartbeatTimeoutMillis;
        final int threadCount = 2; // One to loop reading chunks by executing Runnables on a second with a timeout.
        this.executor = Executors.newFixedThreadPool(threadCount);

        final Thread mainThread = Thread.currentThread();
        readFuture = executor.submit(new Runnable(){
            public void run() {
                try {
                    Thread.currentThread().setName(mainThread.getName() + " read stream thread (MicroInputStream pool)");
                    while(true) {
                        Future readHeaderFuture = executor.submit(new Runnable(){
                            public void run() {
                                Thread.currentThread().setName(mainThread.getName() + " read chunk thread (MicroInputStream pool)");
                                try {
                                    readChunk();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                } finally {
                                    Thread.currentThread().setName(Thread.currentThread().getName() + " (idle)");
                                }
                            }
                        });
                        readHeaderFuture.get(heartbeatTimeoutMillis, TimeUnit.MILLISECONDS);
                    }
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                } catch (TimeoutException e) {
                } finally {
                    notifyClientListeners(serverLog, mainThread);
                    readEof();
                    Thread.currentThread().setName(Thread.currentThread().getName() + " (idle)");
                }
            }
        });
    }

    private synchronized void notifyClientListener(MicroClientListener listener) throws InterruptedException {
        try {
            listener.clientDisconnected();
        } catch (ExitException e) {
            throw new InterruptedException(e.getMessage());
        }
    }

    private synchronized void notifyClientListener(MicroClientListener listener, Thread mainThread) {
        try {
            notifyClientListener(listener);
        } catch (InterruptedException e) {
            mainThread.interrupt();
        }
    }

    private synchronized void notifyClientListeners(PrintStream serverLog, Thread mainThread) {
        if (! eof) {
            serverLog.println(mainThread.getName() + " disconnected");
            for (Iterator i = clientListeners.iterator(); i.hasNext(); ) {
                notifyClientListener((MicroClientListener) i.next(), mainThread);
            }
        }
        clientListeners.clear();
    }

    public synchronized void close() {
        readEof();
        readFuture.cancel(true);
        executor.shutdownNow();
    }

    private InputStream readPayload(InputStream in, int len) throws IOException {

        byte[] receiveBuffer = new byte[len];
        int totalRead = 0;
        while (totalRead < len) {
            int currentRead = in.read(receiveBuffer, totalRead, len - totalRead);
            if (currentRead < 0) {
                throw new IOException("stdin EOF before payload read.");
            }
            totalRead += currentRead;
        }
        return new ByteArrayInputStream(receiveBuffer);
    }

    private void readChunk() throws IOException {

        // Synchronize on the input stream to avoid blocking other threads while waiting for chunk headers.
        synchronized (this.din) {
            int hlen = din.readInt();
            byte chunkType = din.readByte();
            long readTime = System.currentTimeMillis();
            long intervalMillis = readTime - lastReadTime;

            // Synchronize the remainder of the method on this object as it accesses internal state.
            synchronized (this) {
                lastReadTime = readTime;
                switch(chunkType) {
                    case MicroConstants.CHUNKTYPE_STDIN:
                        if (remaining != 0) throw new IOException("Data received before stdin stream was emptied.");
                        remaining = hlen;
                        stdin = readPayload(in, hlen);
                        notify();
                        break;

                    case MicroConstants.CHUNKTYPE_STDIN_EOF:
                        readEof();
                        break;

                    default:
                        throw(new IOException("Unknown stream type: " + (char) chunkType));
                }
            }
        }
    }

    private synchronized void readEof() {
        eof = true;
        notifyAll();
    }

    public int available() throws IOException {
        if (eof) return(0);
        if (stdin == null) return(0);
        return stdin.available();
    }

    public boolean markSupported() {
        return (false);
    }

    public synchronized int read() throws IOException {
        if (oneByteBuffer == null) oneByteBuffer = new byte[1];
        return((read(oneByteBuffer, 0, 1) == -1) ? -1 : (int) oneByteBuffer[0]);
    }

    public int read(byte[] b) throws IOException {
        return (read(b, 0, b.length));
    }

    public synchronized int read(byte[] b, int offset, int length) throws IOException {
        if (!started) {
            sendSendInput();
        }

        waitForChunk();
        if (eof) return(-1);

        int bytesToRead = Math.min((int) remaining, length);
        int result = stdin.read(b, offset, bytesToRead);
        remaining -= result;
        if (remaining == 0) sendSendInput();
        return (result);
    }

    private synchronized void waitForChunk() throws IOException {
        try {
            if((! eof) && (remaining == 0)) wait();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private synchronized void sendSendInput() throws IOException {
        out.writeInt(0);
        out.writeByte(MicroConstants.CHUNKTYPE_SENDINPUT);
        out.flush();
        started = true;
    }

    public boolean isClientConnected() {
        long intervalMillis = System.currentTimeMillis() - lastReadTime;
        return intervalMillis < heartbeatTimeoutMillis;
    }

    public synchronized void addClientListener(MicroClientListener listener) {
        if (! readFuture.isDone()) {
            clientListeners.add(listener);
        } else {
            try {
                notifyClientListener(listener);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized void removeClientListener(MicroClientListener listener) {
        clientListeners.remove(listener);
    }

}
