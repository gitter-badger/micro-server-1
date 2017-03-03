package org.mx.playbook;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxySOCKS5;
import org.mx.oauth.client.Credential;
import org.mx.ssh.*;

import java.io.IOException;
import java.net.SocketException;

/**
 * Created by fsbsilva on 2/24/17.
 */
public class Job {
    private String name;
    private String value;
    private SSHConnect sshConnect;
    private SSHShell sshShell;

    public void setSSHConnect(SSHConnect sshConnect){
        this.sshConnect = sshConnect;
    }

    public SSHShell ssh() {
        if( this.sshConnect != null && this.sshConnect.isConnected() ) {
            if( this.sshShell == null){
                this.sshShell = new SSHShell(sshConnect);
            }
            return sshShell;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
