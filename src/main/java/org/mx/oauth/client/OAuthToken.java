package org.mx.oauth.client;

import org.joda.time.DateTime;

/**
 * Created by fsbsilva on 9/26/16.
 */
public class OAuthToken {

    private String token;
    private String accessCode;
    private DateTime tokeExpireTimestamp;
    private DateTime accessCodeExpireTimestamp;
    private boolean isTokenExpire=true;
    private boolean isTokenValid=true;
    private boolean isAccessCodeValid=true;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public DateTime getTokeExpireTimestamp() {
        return tokeExpireTimestamp;
    }

    public void setTokeExpireTimestamp(DateTime tokeExpireTimestamp) {
        this.tokeExpireTimestamp = tokeExpireTimestamp;
    }

    public DateTime getAccessCodeExpireTimestamp() {
        return accessCodeExpireTimestamp;
    }

    public void setAccessCodeExpireTimestamp(DateTime accessCodeExpireTimestamp) {
        this.accessCodeExpireTimestamp = accessCodeExpireTimestamp;
    }
    public boolean isTokenExpire() {
        return isTokenExpire;
    }

    public void setTokenExpire(boolean tokenExpire) {
        isTokenExpire = tokenExpire;
    }

    public boolean isTokenValid() {
        return isTokenValid;
    }

    public void setTokenValid(boolean tokenValid) {
        isTokenValid = tokenValid;
    }

    public boolean isAccessCodeValid() {
        return isAccessCodeValid;
    }

    public void setAccessCodeValid(boolean accessCodeValid) {
        isAccessCodeValid = accessCodeValid;
    }
}
