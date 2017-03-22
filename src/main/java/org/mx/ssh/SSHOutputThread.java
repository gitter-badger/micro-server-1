package org.mx.ssh;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by fsbsilva on 3/3/17.
 */
public class SSHOutputThread extends Thread {

    private String output;
    private boolean live = true;
    private boolean running = true;
    private String password = null;
    private String lastOutput;
    private SSHConnect connect;
    private String success_key;
    private boolean isBecomingUser = false;

    public SSHOutputThread(SSHConnect connect, String success_key){
        this.connect = connect;
        this.success_key = success_key;
    }

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

    public void setRunning(boolean value) {
        this.running = value;
    }

    public boolean isBecomingUser() {
        return isBecomingUser;
    }

    public void setBecomingUser(boolean becomingUser) {
        isBecomingUser = becomingUser;
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

            if (connect.getSessionError().available() >  0) {
                i = connect.getSessionError().read(tmp, 0, tmp.length);
                if (i < 0) {
                    System.err.println("input stream closed earlier than expected");
                    System.exit(1);
                }
                stdErr += new String(tmp, 0, i);
            }

            if (connect.getSessionOutput().available() > 0) {
                i = connect.getSessionOutput().read(tmp, 0, tmp.length);
                if (i < 0) {
                    System.err.println("input stream closed earlier than expected");
                    System.exit(1);
                }
                stdOut += new String(tmp, 0, i);

                connect.getLogger().debug("================================STDOUT_BEGIN=================================" );
                connect.getLogger().debug(stdOut);
                connect.getLogger().debug("================================STDOUT_END===================================" );

                this.setLastOutput(stdOut);
            }

            String test = null;
            if(stdOut.trim().length()>6){
                test = stdOut.trim().substring(stdOut.trim().length()-5).trim();
            } else {
                test = stdOut.trim();
            }

            if( test.trim().endsWith(expect) ) {
                this.output = stdOut;
                stdOut = "";
                connect.getLogger().debug("***********************  READY TO NEXT COMMAND  *****************************" );
                this.running = false;
            }

            if ( stdOut.contains("connecting (yes/no)") || stdErr.contains("connecting (yes/no)") ){
                sendpass = false;
                String cmd = "yes \n";
                stdOut = "";
                connect.getCommandIO().write(cmd.getBytes());
                connect.getCommandIO().flush();
                Thread.sleep(30);
            }

            String redhatSudo = stdOut.replace("[sudo]", "");
            redhatSudo = redhatSudo.replace("sudo su -\r\n", "");

            if( stdOut.contains("assword:") || stdErr.contains("assword:")  ||
                    ( redhatSudo.contains("password for") && redhatSudo. matches("(.+?)password for(.+?)[:].$") ) ) {

                String cmd = this.password + "\n";
                if( connect.getExternalPassword() != null && !connect.getExternalPassword().trim().isEmpty() ){
                    cmd = connect.getExternalPassword()+ "\n";
                }

                stdOut = "";
                connect.getCommandIO().write(cmd.getBytes());
                connect.getCommandIO().flush();
                Thread.sleep(1000);

                if ( isBecomingUser ) {
                    String prompt = "export PS1=\" " + success_key + " \"";
                    String command = "echo '<mx-cmd>'; " + prompt + "; echo '<mx-cmd/>'; \n";

                    connect.getCommandIO().write(command.getBytes());
                    connect.getCommandIO().flush();
                    isBecomingUser = false;
                }

            } else if ( isBecomingUser && stdOut.trim().length() > 0) {
                String prompt = "export PS1=\" "+success_key+" \"";
                String command = "echo '<mx-cmd>'; "+prompt+"; echo '<mx-cmd/>'; \n";

                connect.getCommandIO().write(command.getBytes() );
                connect.getCommandIO().flush();
                Thread.sleep(1000);
                isBecomingUser = false;
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

    public boolean isItAlive() {
        return live;
    }

}
