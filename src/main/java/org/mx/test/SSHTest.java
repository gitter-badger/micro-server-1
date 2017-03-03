package org.mx.test;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxySOCKS5;
import org.mx.oauth.client.Credential;
import org.mx.playbook.Job;
import org.mx.ssh.SSHMXException;
import org.mx.ssh.SSHConnect;

import java.io.IOException;

/**
 * Created by fsbsilva on 2/25/17.
 */
public class SSHTest  {

    public static void main(String[] args) {
        SSHConnect ssh = new SSHConnect();
        try {
            Credential credential = new Credential();
            credential.setUserName(args[0]);
            credential.setPassword(args[1],false);
//            credential.setPrivateKeyPath("/Users/fsbsilva/.ssh/id_rsa");

            ssh.setProxySOCKS5(new ProxySOCKS5("localhost",8889));
            ssh.connect(args[2],credential);
            Job job = new Job();
            job.setSSHConnect(ssh);

            if( ssh.isConnected()) {
                String result = job.ssh().sessionExec("ls -ltr ");
                System.out.println("##################################");
                System.out.println(result);
                System.out.println("##################################");

                result = job.ssh().sessionExec("cat .profile | awk '{ split($0,a,\"root=\"); print a[2];}' | xargs");

                System.out.println("##################################");
                System.out.println(result);
                System.out.println("##################################");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SSHMXException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            ssh.disconnect();
        }

    }

}
