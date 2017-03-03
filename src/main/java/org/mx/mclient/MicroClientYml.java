package org.mx.mclient;

/**
 * Created by fsbsilva on 1/3/17.
 */
public class MicroClientYml {

    private String name;
    private String host;
    private int port;
    private String truststorePath;
    private String log4jPath;

    public String getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTruststorePath() {
        return truststorePath;
    }

    public void setTruststorePath(String truststorePath) {
        this.truststorePath = truststorePath;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getLog4jPath() {
        return log4jPath;
    }

    public void setLog4jPath(String log4jPath) {
        this.log4jPath = log4jPath;
    }
}
