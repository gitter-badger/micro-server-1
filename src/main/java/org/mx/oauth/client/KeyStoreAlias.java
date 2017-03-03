package org.mx.oauth.client;

/**
 * Created by fsbsilva on 11/1/16.
 */
public class KeyStoreAlias {

    private String alias;
    private String username;
    private String authType;
    private String sshKeyPath;

    public static String TYPE_SSH_KEYAGENT="ssh-keyagent";
    public static String TYPE_PASSWORD="password";

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getSshKeyPath() {
        return sshKeyPath;
    }

    public void setSshKeyPath(String sshKeyPath) {
        this.sshKeyPath = sshKeyPath;
    }
}
