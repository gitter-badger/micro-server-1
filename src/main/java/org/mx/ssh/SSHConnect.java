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
package org.mx.ssh;

import com.jcraft.jsch.*;
import org.mx.oauth.client.Credential;

import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SSHConnect {
    public static final String VERSION = "0.0.2";

    private JSch jsch = new JSch();
    private com.jcraft.jsch.Session session;
    private ChannelShell channel;
    private Integer STATUS = 0;

    private PipedOutputStream commandIO = null;
    private InputStream sessionInput = null;
    private InputStream sessionOutput = null;
    private InputStream sessionError = null;

    private SSHOutputThread thread = null;
    protected int promptLines = 0;

    protected int sessionTimeOut = 300000;
    protected int connectionTimeOut = 0;
    private ProxySOCKS5 proxySOCKS5=null;
    private String externalPassword = null;

    private Credential credential;
    private String hostname;
    private int port = 22;
    private String success_key="mx>";

    private SSHLogger sshLogger;

    public SSHLogger getLogger() {
        return sshLogger;
    }

    private Credential getCredential(){
        return this.credential;
    }

    public String getExternalPassword() {
        return externalPassword;
    }

    public void setExternalPassword(String externalPassword) {
        this.externalPassword = externalPassword;
    }

    public ProxySOCKS5 getProxySOCKS5() {
        return this.proxySOCKS5;
    }

    public void setProxySOCKS5(ProxySOCKS5 proxySOCKS5) {
        this.proxySOCKS5 = proxySOCKS5;
    }

    public int getSessionTimeOut() {
        return sessionTimeOut;
    }

    public void setSessionTimeOut(int sessionTimeOut) {
        this.sessionTimeOut = sessionTimeOut;
    }

    public int getConnectionTimeOut() {
        return connectionTimeOut;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public Session getSession() {
        return session;
    }

    public boolean connect(String hostname, Credential credential) throws UnknownHostException, SocketException, IOException, JSchException, InterruptedException {
        STATUS =1;
        boolean isConnected = false;
        this.hostname = hostname;
        this.credential = credential;

        int idx = this.hostname.indexOf(':');
        if (idx > -1) {
            this.port = Integer.parseInt(hostname.substring(idx + 1));
            this.hostname = this.hostname.substring(0, idx);
        }

        String MX_HOME = System.getProperty("parent.basedir");

        sshLogger = new SSHLogger();
        sshLogger.createLoggers(hostname, MX_HOME);

        sshLogger.getLogger().debug("opening session channel");

        isConnected = openSessionChannel();

        sshLogger.getLogger().debug("isConnected: " + isConnected);
        if(isConnected){
            STATUS=2;


            thread = new SSHOutputThread(this,success_key);
            if( this.credential.getPassword().isEncrypted() ){
                this.credential.getPassword().decrypt();
            }
            thread.setPassword(this.credential.getPassword().toString());
            thread.start();


            String command = "export PS1=\""+success_key+" \"";
            SSHShell sshSession =  new SSHShell(this);
            sshSession.sessionExec(command);

        }

        return isConnected;
    }

    public void disconnect(){
        try{
            sshLogger.getLogger().debug("DISCONNECTING !!!");
            if ( thread!= null ){

                this.thread.kill();
                sshLogger.getLogger().debug(thread.getName()+" was thread killed !!!");
            }
            if(channel!=null && channel.isConnected()){
                channel.disconnect();
            }
            if( session !=null ){
                this.session.setTimeout(1);
                this.session.disconnect();
            }
            sshLogger.getLogger().removeAllAppenders();
//            sshLogger.getLoggerSSH().removeAllAppenders();

        } catch (JSchException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            STATUS=3;
        }
    }

    private boolean openSessionChannel() throws UnknownHostException, IOException, JSchException, InterruptedException {
        boolean result = true;
        this.session = jsch.getSession(this.credential.getUserName(), this.hostname, this.port);
        this.session.setTimeout(sessionTimeOut);
        this.session.setConfig("StrictHostKeyChecking", "no");

        if( proxySOCKS5 != null){
            this.session.setProxy(proxySOCKS5);
        }

        if( this.credential.getPrivateKeyPath() != null ){
            sshLogger.getLogger().debug("Using Credential Private Key "+credential.getPrivateKeyPath());
            if( this.credential.getPassPhrase()!=null && this.credential.getPassPhrase().isEncrypted()){
                this.credential.getPassPhrase().decrypt();
                this.jsch.addIdentity(credential.getPrivateKeyPath(), credential.getPassPhrase().toString().getBytes());
            } else {
                this.jsch.addIdentity("", credential.getPassPhrase().toString().getBytes());
            }

            UserInfo userinfo = null;
            this.session.setUserInfo(userinfo);
        } else {
            sshLogger.getLogger().debug("Using Credential Password");
            if( this.credential.getPassword().isEncrypted() ){
                this.credential.getPassword().decrypt();
            }
            this.session.setPassword(credential.getPassword().toString());
        }


        if( connectionTimeOut > 0){
            this.session.connect(connectionTimeOut);
            sshLogger.getLogger().debug("ConnectionTimeOut = "+connectionTimeOut);
        } else {
            this.session.connect();
        }

        this.channel = (ChannelShell)session.openChannel("shell");
        int tcol=1023;
        int trow=24;
        int twp=640;
        int thp=480;
        this.channel.setPtySize(tcol, trow, twp, thp);
        this.channel.setPty(true);
        this.commandIO = new PipedOutputStream();
        this.sessionInput = new PipedInputStream(this.commandIO);
        // this set's the InputStream the remote server will read from.
        this.channel.setInputStream(sessionInput);

        // this will have the STDOUT from server.
        this.sessionOutput = channel.getInputStream();

        // this will have the STDERR from server
        this.sessionError = channel.getExtInputStream();
        this.channel.connect();

        return result;
    }

    protected SSHOutputThread getThread(){
        return this.thread;
    }

    public boolean isConnected() {
        if(session == null) return false;
        return session.isConnected();
    }

    public File getFile(String sourceDir, String sourceFile) {
        SCPClient scp = new SCPClient();
        return scp.getFile(this, sourceDir, sourceFile);
    }

    public boolean isConnecting(){
        synchronized (STATUS) {
            if(STATUS==1){
                return true;
            }
            return false;
        }
    }

    public InputStream getSessionInput() {
        return sessionInput;
    }

    public InputStream getSessionOutput() {
        return sessionOutput;
    }

    public InputStream getSessionError() {
        return sessionError;
    }

    public PipedOutputStream getCommandIO() {
        return commandIO;
    }

}

