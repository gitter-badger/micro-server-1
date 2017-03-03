package org.mx.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Created by fsbsilva on 2/28/17.
 */
public class BecomeUser {

    private SSHConnect sshConnect;
    private String success_key;

    public BecomeUser(SSHConnect ssh, String success_key){
        this.sshConnect = ssh;
    }

    public String execute(String command) throws IOException, InterruptedException, JSchException {
        ChannelExec channelExec = null;
        try{
            if ( !sshConnect.isConnected() ){
                return "";
            }

            channelExec = (ChannelExec)sshConnect.getSession().openChannel("sessionExec");

            BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
            StringBuilder localStringBuilder = new StringBuilder();
            channelExec.setCommand(command);

            channelExec.connect();

            for (String str = localBufferedReader.readLine(); str != null; str = localBufferedReader.readLine())
            {
                if(localStringBuilder.length()==0){
                    localStringBuilder.append(str);
                } else {
                    localStringBuilder.append("\n");
                    localStringBuilder.append(str);
                }
            }
            sshConnect.getLogger().debug("["+(new Date())+"] - ChannelExec Command  :\n"+command);
            sshConnect.getLogger().debug("["+(new Date())+"] - ChannelExec Response :\n"+ localStringBuilder.toString());

            return localStringBuilder.toString();
        } finally {
            if ( channelExec.isConnected() ){
                channelExec.disconnect();
                channelExec = null;
            }
        }
    }

    private boolean isID(String id) throws IOException, SSHMXException, InterruptedException, JSchException {
        boolean valid = true;
        String msg = execute("finger " + id);
        if(msg != null && (msg.contains("ser not found") || msg.contains("o such user")) ) {
            valid = false;
        }
        return valid;
    }

    public String pbrun(String userId) throws CannotSudoException, IOException, SSHMXException, InterruptedException, JSchException {
        if( userId.length() > 0 && !isID(userId)) {
            throw new CannotSudoException("User " + userId + " doesn't exit");
        }

        return becomeUser("pbrun /bin/su - "+userId);
    }

    public String pbrun() throws CannotSudoException, IOException, SSHMXException, InterruptedException, JSchException {
        return becomeUser("pbrun /bin/su - ");
    }

    public String sudo(String userId) throws CannotSudoException, IOException, SSHMXException, InterruptedException, JSchException {
        if( userId.length() > 0 && !isID(userId)) {
            throw new CannotSudoException("User " + userId + " doesn't exit");
        }

        return becomeUser("sudo su - "+userId);
    }

    public String sudo() throws CannotSudoException, IOException, SSHMXException, InterruptedException, JSchException {
        return becomeUser("sudo su - ");
    }

    private String becomeUser(String command) throws CannotSudoException, IOException, SSHMXException, InterruptedException, JSchException {

        String msg = execute(command);
        boolean gotit = true;
        if(msg != null && msg.contains("Sorry") ) {
            msg = execute(command);
            if(msg != null && msg.contains("Sorry")) {
                gotit = false;
            }
        }

        if(gotit) {
            String prompt = "export PS1='"+success_key+" '";
            execute(prompt);
        }else {
            throw new CannotSudoException(msg);
        }

        return msg;
    }
}
