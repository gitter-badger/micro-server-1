package org.mx.ssh;

import org.apache.log4j.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by fsbsilva on 2/28/17.
 */
public class SSHLogger {

    private Logger logger = null;
//    private Logger loggerSSH = null;

    /**
     * Create loggers to be used in this class
     * logger is to manage debug messages
     * loggerSSH is to manage SSH interaction command/output
     * @param hostname
     */
    protected void createLoggers(String hostname, String path) {
        logger = Logger.getLogger("SSHService_Debug_" + hostname);
        //logger.removeAllAppenders();
        //logger.addAppender(new ConsoleAppender());
//        loggerSSH = Logger.getLogger("SSHService_SSH_" + hostname);
        //loggerSSH.removeAllAppenders();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        String date = sdf.format( Calendar.getInstance().getTime() );
        String logdir = path + "/logs/" + hostname + "/" + date + "/";
        try {
            org.apache.commons.io.FileUtils.forceMkdir(new File(logdir));
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");
        String datetime = sdf.format(Calendar.getInstance().getTime());
        String debuglogfile = logdir + "debug_" + datetime + ".log";
//        String sshlogfile = logdir + "ssh_" + datetime + ".log";

        FileAppender fileappender = null;
//        FileAppender fileappender2 = null;
        try {
            fileappender = new RollingFileAppender(new PatternLayout( "[%d{dd MMM yyyy HH:mm:ss,SSS}] %n %m %n %n" ), debuglogfile);
//            fileappender2 = new RollingFileAppender(new PatternLayout("[%d{dd MMM yyyy HH:mm:ss,SSS}] %n %m %n %n"), sshlogfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.addAppender(fileappender);
        logger.setLevel(Level.DEBUG);
//        loggerSSH.addAppender(fileappender2);
//        loggerSSH.setLevel(Level.DEBUG);
    }

//    public Logger getLoggerSSH() {
//        return loggerSSH;
//    }

    public Logger getLogger() {
        return this.logger;
    }

    public void debug(String msg){
        this.logger.debug(msg);
    }

    public void warn(String msg){
        this.logger.warn(msg);
    }

    public void info(String msg){
        this.logger.info(msg);
    }

    public void error(String msg){
        this.logger.error(msg);
    }

}
