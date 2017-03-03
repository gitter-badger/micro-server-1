/*

 Copyright 2004-2015, MXDeploy Software, Inc.

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
package org.mx.oauth.client;

public class Credential {
    
    private String id;
    private String alias;
    private String userName;
    private Password password;
    private String privateKeyPath;
    private Password passPhrase;
    private String authType="SSH-PASSWORD";
    private Password keyStorePassword;
    private String keyStoreLocation;
    private Password keyEntryPassword;
    private String regExpInclude;
    private String regExpExclude;
    private boolean passPhraseIsRequired = false;
    
    public Credential(){ }
    
    public Credential(String userName, String passwordText, boolean isPasswordEncrypted){
        this.userName = userName;
        Password password = new Password(passwordText, isPasswordEncrypted);
        this.password = password;
    }
    
    public void setId(String id){
        this.id = id;
    }
    
    public String getId(){
        return this.id;
    }
    
    public void setAlias(String alias){
        this.alias = alias;
    }
    
    public String getAlias(){
        return this.alias;
    }
    
    public void setUserName(String userName){
        this.userName = userName;
    }
    
    public String getUserName(){
        return this.userName;
    }
    
    public void setPassword(String passwordText, boolean isEncrypted){
        Password password = new Password(passwordText, isEncrypted);
        this.password = password;
    }

    public Password getPassword(){
        return this.password;
    }
    
    public void setPrivateKeyPath(String privateKeyPath){
        this.privateKeyPath = privateKeyPath;
    }
    
    public String getPrivateKeyPath(){
        return this.privateKeyPath;
    }
    
    public void setPassPhrase(String passPhraseText, boolean isEncrypted){
        Password password = new Password(passPhraseText, isEncrypted);
        this.passPhrase = password;
    }
    
    public Password getPassPhrase(){
        return this.passPhrase;
    }
    
    public void setAuthType(String authType){
        this.authType = authType;
    }
    
    public String getAuthType(){
        return this.authType;
    }
    
    public void setRegExpInclude(String regExpInclude){
        this.regExpInclude = regExpInclude;
    }
    
    public String getRegExpInclude(){
        return this.regExpInclude;
    }
    
    public void setRegExpExclude(String regExpExclude){
        this.regExpExclude = regExpExclude;
    }
    
    public String getRegExpExclude(){
        return this.regExpExclude;
    }    

    public void setKeyStorePassword(String keyStorePasswordText, boolean isEncrypted){
        Password password = new Password(keyStorePasswordText, isEncrypted);
        this.keyStorePassword = password;
    }
    
    public Password getKeyStorePassword(){
        return this.keyStorePassword;
    }

    public void setKeyStoreLocation(String keyStoreLocation){
        this.keyStoreLocation = keyStoreLocation;
    }

    public String getKeyStoreLocation(){
        return this.keyStoreLocation;
    }

    public void setKeyEntryPassword(String keyEntryPassword, boolean isEncrypted){
        Password password = new Password(keyEntryPassword, isEncrypted);
        this.keyEntryPassword = password;
    }   

    public Password getKeyEntryPassword(){
        return this.keyEntryPassword;
    }   
    
    public boolean isPassPhraseRequired(){
        return this.passPhraseIsRequired;
    }
    
    public void setIsPassPhraseRequired(boolean passPhraseIsRequired){
        this.passPhraseIsRequired = passPhraseIsRequired;
    }
}
    