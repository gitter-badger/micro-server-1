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
public class SSHChannel {

    private SSHConnect sshConnect;

    public SSHChannel(SSHConnect sshConnect){
        this.sshConnect = sshConnect;
    }

    public String channelExec(String command) throws IOException, InterruptedException, JSchException {
        ChannelExec channelExec = null;
        try{
            if ( !sshConnect.isConnected() ){
                return "";
            }

            channelExec = (ChannelExec)sshConnect.getSession().openChannel("exec");

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
}
