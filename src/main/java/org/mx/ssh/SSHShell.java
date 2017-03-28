package org.mx.ssh;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxySOCKS5;

import java.io.IOException;

/**
 * Created by fsbsilva on 2/28/17.
 */
public class SSHShell {

    private SSHConnect connect;
    private SSHChannel channel;
    private SSHBecomeUser becomeUser;

    public SSHShell(SSHConnect connect){
        this.connect = connect;
        this.channel = new SSHChannel(connect);
        this.becomeUser = new SSHBecomeUser(this);
    }

    public boolean isConnected(){
        return this.connect.isConnected();
    }

    protected String execute(String command, boolean isBecomingUser) throws IOException, InterruptedException {
        command = "echo '<mx-cmd>'; "+command+"; echo '<mx-cmd/>'; \n";
        if( isBecomingUser ){
            command = command+" \n";
            connect.getThread().setBecomingUser(true);
        }

        connect.getCommandIO().write(command.getBytes());
        connect.getCommandIO().flush();
        connect.getThread().read();
        while( connect.getThread().isRunning() ){
            Thread.sleep(30);
            if(connect.getThread() != null && !connect.getThread().isItAlive()){
                connect.getThread().setRunning(false);
                throw new InterruptedException("Thread Stopped");
            }
        }

        String result = connect.getThread().getOutput();
        result = splitter(result,"<mx-cmd>","<mx-cmd/>");
//        System.out.println(result);

        return result;
    }

    private String splitter(String value, String start, String end){
        String[] splitter = value.split(start);
        String result = "";
        if( splitter.length > 1){
            if( splitter[1].indexOf(start) != -1 ){
                result = splitter(splitter[2],start, end);
            } else {
                result = splitter[2].split(end)[0];
            }
        }
        return result;
    }

    public String sessionExec(String command) throws IOException, InterruptedException {
        if (!this.connect.isConnected()) {
            throw new InterruptedException();
        }
        return this.execute(command, false);
    }

    public String channelExec(String command) throws IOException,InterruptedException, JSchException {
        if ( !this.connect.isConnected()) {
            throw new InterruptedException();
        }

        return  this.channel.channelExec(command);
    }

    public void disconnect(){
        if( this.connect !=null && this.connect.isConnected() )
            this.connect.disconnect();
    }

    public int getConnectionTimeOut() {
        return connect.getConnectionTimeOut();
    }

    public ProxySOCKS5 getProxySOCKS5() {
        return connect.getProxySOCKS5();
    }

    public SSHBecomeUser getBecomeUser() {
        return becomeUser;
    }

}
