package org.mx.oauth.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fsbsilva on 10/10/16.
 */
public class KeyStore {

    private String keyStorePath;
    private String keyStorePassword;
    private boolean keyStoreAutoLogin;
    private String keyStorePasswordMD5;
    private List<KeyStoreAlias> aliasList;

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public boolean isKeyStoreAutoLogin() {
        return keyStoreAutoLogin;
    }

    public void setKeyStoreAutoLogin(boolean keyStoreAutoLogin) {
        this.keyStoreAutoLogin = keyStoreAutoLogin;
    }

    public String getKeyStorePasswordMD5() {
        return keyStorePasswordMD5;
    }

    public void setKeyStorePasswordMD5(String keyStorePasswordMD5) {
        this.keyStorePasswordMD5 = keyStorePasswordMD5;
    }

    public List<KeyStoreAlias> getAliasList() {
        return aliasList;
    }

    public void addAlias(KeyStoreAlias keyStoreAlias) {
        if( this.aliasList == null ){
            this.aliasList = new ArrayList<KeyStoreAlias>();
        }
        this.aliasList.add(keyStoreAlias);
    }
}
