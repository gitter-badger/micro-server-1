package org.mx.mserver;

/**
 * Created by fsbsilva on 1/1/17.
 */
public class MicroServer {

    private String name;
    private String host;
    private int port;
    private String repositoryPath;
    private String keystorePath;
    private String keystorePassword;
    private String keystorePkgs;
    private String log4jPath;

    public String getLog4jPath() {
        return log4jPath;
    }

    public void setLog4jPath(String log4jPath) {
        this.log4jPath = log4jPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
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

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getKeystorePkgs() {
        return keystorePkgs;
    }

    public void setKeystorePkgs(String keystorePkgs) {
        this.keystorePkgs = keystorePkgs;
    }
}
