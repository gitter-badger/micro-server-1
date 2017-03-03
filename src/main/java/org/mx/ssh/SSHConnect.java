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
import org.apache.log4j.*;
import org.apache.log4j.Logger;
import org.mx.oauth.client.Credential;

import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    protected OutputThread thread = null;
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

    public boolean connect(String hostname, Credential credential) throws UnknownHostException, SocketException, IOException, JSchException, SSHMXException, InterruptedException {
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


            thread = new OutputThread();
            if( this.credential.getPassword().isEncrypted() ){
                this.credential.getPassword().decrypt();
            }
            thread.setPassword(this.credential.getPassword().toString());
            thread.start();

            String command = "export PS1='"+success_key+" '";
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

    protected String execute(String command) throws IOException, SSHMXException, InterruptedException {
        String msg = "";
        command = command + "\n";
        commandIO.write(command.getBytes());
        commandIO.flush();
        thread.read();
        while( thread.isRunning() ){
            Thread.sleep(30);
            if(this.thread!=null && !this.thread.live){
                thread.running=false;
                throw new SSHMXException("Thread Stopped");
            }
        }

        msg = thread.getOutput();
//        loggerSSH.debug("sessionExec command response: " + msg);
        return msg;
    }

    public String getLastOutput() {
        return thread.getLastOutput();
    }

    private class OutputThread extends Thread {
        private String output;
        private boolean live = true;
        private boolean running = true;
        private String password = null;
        private String lastOutput;

        public String getLastOutput() {
            return this.lastOutput;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void kill(){
            live = false;
        }

        public boolean isRunning() {
            return running;
        }

        public synchronized void read() {
            output = "";
            running = true;
        }

        public String getOutput() {
            return this.output;
        }

        public void run() {
            try {
                readBuffer(success_key);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void readBuffer(String expect) throws IOException, InterruptedException {
            byte[] tmp = new byte[2048];
            String stdOut = "";
            String stdErr = "";

            int i;

            boolean sendpass = true;

            while(true){
                if(!live){
                    break;
                }

                if (sessionError.available() >  0) {
                    i = sessionError.read(tmp, 0, tmp.length);
                    if (i < 0) {
                        System.err.println("input stream closed earlier than expected");
                        System.exit(1);
                    }
                    stdErr += new String(tmp, 0, i);
                }

                if (sessionOutput.available() > 0) {
                    i = sessionOutput.read(tmp, 0, tmp.length);
                    if (i < 0) {
                        System.err.println("input stream closed earlier than expected");
                        System.exit(1);
                    }
                    //stdOut += new String(tmp, 0, i);
                    for(int j=0;j<i;j++){
                        if( tmp[j] == 27){
                            continue;
                        } else if( j>0 && ( tmp[j-1] == 27 && tmp[j] == 91) ){
                            j = j + 3;
                        } else {
                            stdOut+= new String(tmp, j, 1);
                        }
                    }

                    sshLogger.getLogger().debug("stdOut :"+stdOut);
                    this.setLastOutput(stdOut);
                }

                if(stdOut.contains("'mx>")){
                    stdOut = stdOut.replace("'mx>", "");
                    sshLogger.getLogger().debug("globalVar Replaced ="+stdOut);
                }


                String test = null;
                if(stdOut.trim().length()>4){
                    test = stdOut.trim().substring(stdOut.trim().length()-3).trim();
                } else {
                    test = stdOut.trim();
                }

//                if( test.trim().endsWith(expect) || test.trim().endsWith("#") || test.trim().endsWith("$")
//                        || test.trim().endsWith(">") || ( !stdOut.contains("if [") && test.trim().endsWith("]")) ) {
                if( test.trim().endsWith(expect) ) {

                    this.output = stdOut;
                    output = output.replaceAll("\b", "");
                    stdOut = "";
                    sshLogger.getLogger().debug("========STDOUT\n" + output + "\n===========");
                    sshLogger.getLogger().debug("SETTING RUNNING = FALSE => if expect");
                    this.running = false;
                }

                if ( stdOut.contains("connecting (yes/no)") || stdErr.contains("connecting (yes/no)") ){
                    sendpass = false;
                    String cmd = "yes \n";
                    stdOut = "";
                    commandIO.write(cmd.getBytes());
                    commandIO.flush();
                    Thread.sleep(30);
                }

	    		/* N�o sei pq isso est� aqui, e estava dando problema. Estou comentando at� que precise habilitar
	    		 * cuidado com
	    		if ( stdOut.contains("ermission denied") || stdErr.contains("ermission denied") ){
	            	this.output = stdOut;
	    			output = output.replaceAll("\b", "");
	    			stdOut = "";
	    			logger.debug("========STDOUT\n" + output + "\n===========");
	    			logger.debug("SETTING RUNNING = FALSE => if permission denied");
	            	this.running = false;
	    		}
	    		*/

                String redhatSudo = stdOut.replace("[sudo]", "");
                redhatSudo = redhatSudo.replace("sudo su -\r\n", "");

                if ( stdOut.contains("assword:") || stdErr.contains("assword:")  ||
                        ( redhatSudo.contains("password for") && redhatSudo. matches("(.+?)password for(.+?)[:].$") ) ) {

                    //String cmd = this.password + "\n";
                    String cmd = this.password + "\n";
                    if( externalPassword != null && !externalPassword.trim().isEmpty() ){
                        cmd = externalPassword+ "\n";
                    }

                    stdOut = "";
                    commandIO.write(cmd.getBytes());
                    commandIO.flush();
                    Thread.sleep(30);
                }


                if( ( (stdOut.contains("(current) UNIX password")) || (stdOut.contains("New UNIX password")) || (stdOut.contains("Retype new UNIX password")) || (stdOut.contains("Old password") || stdOut.contains("ew password") || stdOut.contains("Enter the new password again")) ) ) {
                    this.output = stdOut;
                    output = output.replaceAll("\b", "");
                    stdOut = "";
                    sshLogger.getLogger().debug("========STDOUT\n" + output + "\n===========");
                    sshLogger.getLogger().debug("SETTING RUNNING = FALSE => change password");
                    this.running = false;
                }
                Thread.sleep(300);
            }

        }

        private void setLastOutput(String stdOut) {
            this.lastOutput += stdOut;
            if(this.lastOutput.length() > 5000) {
                this.lastOutput = this.lastOutput.substring(this.lastOutput.length()-5000);
            }
        }
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


}

