package org.mx.ssh;

import com.jcraft.jsch.JSchException;

import java.io.IOException;

/**
 * Created by fsbsilva on 2/28/17.
 */
public class SSHBecomeUser {

    private SSHShell ssh;
    private String success_key;

    public SSHBecomeUser(SSHShell ssh){
        this.ssh = ssh;
    }

    private boolean isID(String id) throws IOException, InterruptedException, JSchException {
        boolean valid = true;
        String msg = ssh.execute("finger " + id, false);
        if(msg != null && (msg.contains("ser not found") || msg.contains("o such user")) ) {
            valid = false;
        }
        return valid;
    }

    public String pbrun(String userId) throws  IOException, InterruptedException, JSchException {
        if( userId.length() > 0 && !isID(userId)) {
            throw new JSchException("User " + userId + " doesn't exit");
        }

        return ssh.execute("pbrun /bin/su - "+userId, true);
    }

    public String pbrun() throws JSchException, InterruptedException, IOException, SSHMXException {
        return ssh.execute("pbrun /bin/su - ", true);
    }

    public String sudo(String userId) throws IOException, InterruptedException, JSchException {
        if( userId.length() > 0 && !isID(userId)) {
            throw new JSchException("User " + userId + " doesn't exit");
        }

        return ssh.execute("sudo su - "+userId, true);
    }

    public String sudo() throws IOException, InterruptedException, JSchException {
        return ssh.execute("sudo su - ", true);
    }

}
