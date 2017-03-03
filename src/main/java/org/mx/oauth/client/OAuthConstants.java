package org.mx.oauth.client;

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

/**
 * Just a simple holder for various MXDeploy contants.
 *
 * @author <a href="http://www.mxdeploy.com">Fabio Santos</a>
 */
public class OAuthConstants {

    /**
     * Default size for thread pool
     */
    public static final int DEFAULT_SESSIONPOOLSIZE = 10;
    /**
     * The default mxoauth public port (25000)
     */
    public static final int PUBLIC_PORT = 25100;

    /**
     * The default mxoauth private port (25001)
     */
    public static final int PRIVATE_PORT = 25101;
    /**
     * Chunk type to register token
     */
    public static final int CHUNKTYPE_REGISTER_TOKEN = 100;
    /**
     * Chunk type to register token
     */
    public static final int CHUNKTYPE_SEND_DATA = 101;
    /**
     * Chunk type send data
     */
    public static final int CHUNKTYPE_STDOUT = 101;
    /**
     * Chunk type to validate token
     */
    public static final int CHUNKTYPE_VALIDATE_TOKEN = 110;
    /**
     * Chunk type to validate token
     */
    public static final int CHUNKTYPE_GET_NEW_ACCESS_CODE = 111;
    /**
     * Chunk type end of data
     */
    public static final int CHUNKTYPE_STDIN_EOF = -1;
    /**
     * Chunk type to register token
     */
    public static final int CHUNKTYPE_ERROR = 404;

    /**
     * Chunk type to stop JVM
     */
    public static final int CHUNKTYPE_PRIVATE_STOPJVM = 88235612;

    /**
     * Chunk type to register token
     */
    public static int SESSION_TOKEN_EXPIRE = 600;
    /**
     * Chunk type to register token
     */
    public static int ACCESS_TOKEN_EXPIRE = 60;

    /**
     * Chunk type to register token
     */
    public static boolean IS_TOKEN_EXPIRE = true;

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


}

