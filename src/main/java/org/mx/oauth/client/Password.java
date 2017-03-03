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

public class Password {
    
    private String password;
    private PWSec pwsec = new PWSec();
    private boolean isEncrypted = false;
    private boolean isMD5 = false;
    
    public Password(String password, boolean isEncrypted){
        this.isEncrypted = isEncrypted;
        this.password = password;
        this.isMD5 = false;
    }

    public Password(String password, boolean isEncrypted, boolean isMD5){
        this.isEncrypted = isEncrypted;
        this.password = password;
        this.isMD5 = isMD5;
    }
    
    public String encrypt(){
        if( this.isMD5 ){
            this.password = pwsec.getMessageDigest(this.password);
        } else {
            this.password = pwsec.encrypt(this.password);
        }
        this.isEncrypted = true;
        return this.password;
    }
    
    public String decrypt(){
        if( ! this.isMD5 ){
            this.password = pwsec.decrypt(this.password);
        }
        this.isEncrypted = false;
        return this.password;
    }
    
    public String toString(){
        return this.password;
    }
    
    public boolean isEncrypted(){
        return this.isEncrypted;
    }
    
}