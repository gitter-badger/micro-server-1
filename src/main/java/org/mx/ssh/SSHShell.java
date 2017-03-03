package org.mx.ssh;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxySOCKS5;
import org.mx.oauth.client.Credential;

import java.io.IOException;

/**
 * Created by fsbsilva on 2/28/17.
 */
public class SSHShell {

    private SSHConnect connect;
    private SSHChannel channel;

    public SSHShell(SSHConnect connect){
        this.connect = connect;
        this.channel = new SSHChannel(connect);
    }

    private String execute(String command) throws IOException, SSHMXException, InterruptedException {
        String msg = connect.execute(command);
        String[] lines = msg.split("\r\n");
        String result = "";
        for(int i = 1; i < lines.length-1; i++) {
            String m = lines[i];
            result += m;
            if( (i+1) != (lines.length-1) ){
                result += "\r\n";
            }
        }

        return result;
    }

    public String sessionExec(String command) throws IOException, SSHMXException, InterruptedException {
        if (!this.connect.isConnected()) {
            throw new SSHMXException();
        }

        return this.connect.execute(command);
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

}
