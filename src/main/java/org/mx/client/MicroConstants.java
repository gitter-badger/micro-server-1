package org.mx.client;

/**
 * Created by fsbsilva on 11/29/16.
 */
public class MicroConstants {

    /**
     * Chunk type marker for the end of stdin
     */
    public static final byte CHUNKTYPE_STDIN_EOF = '.';
    /**
     * Chunk type to register token
     */
    public static final int CHUNKTYPE_ERROR = 404;
    /**
     * Default size for thread pool
     */
    public static final int DEFAULT_SESSIONPOOLSIZE = 10;

    public static final int CHUNKTYPE_STOPSERVER = 0;

    public static final int CHUNKTYPE_RUNSCRIPT = 1;

    public static final int CHUNKTYPE_LISTTHREAD = 2;

    public static final int CHUNKTYPE_STOPTHREAD = 3;

    /**
     * Chunk type marker for stdin
     */
    public static final byte CHUNKTYPE_STDIN = '0';
    /**
     * Chunk type marker for stdout
     */
    public static final byte CHUNKTYPE_STDOUT = '1';
    /**
     * Chunk type marker for stderr
     */
    public static final byte CHUNKTYPE_STDERR = '2';
    /**
     * Chunk type marker for client exit chunks
     */
    public static final byte CHUNKTYPE_EXIT = 'X';
    /**
     * Expected interval between heartbeats in milliseconds.
     */
    public static final short HEARTBEAT_INTERVAL_MILLIS = 1000;

    /**
     * Maximum interval to wait between heartbeats before considering client to have disconnected.
     */
    public static final short HEARTBEAT_TIMEOUT_MILLIS = 10000;

    /**
     * Maximum chunk len sent from client.
     */
    public static final short MAXIMUM_CHUNK_LENGTH = 2048;

    /**
     * Chunk type marker for a "startinput" chunk. This chunk type is sent from
     * the server to the client and indicates that the client should begin
     * sending stdin to the server. It is automatically sent the first time the
     * client's inputstream is read.
     */
    public static final byte CHUNKTYPE_SENDINPUT = 'S';

}
