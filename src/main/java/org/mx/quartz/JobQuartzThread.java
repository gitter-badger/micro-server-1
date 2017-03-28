package org.mx.quartz;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxySOCKS5;
import org.apache.log4j.Logger;
import org.mx.job.MicroJobBean;
import org.mx.job.MicroJobDomainBean;
import org.mx.job.MicroJobProxyBean;
import org.mx.oauth.client.Credential;
import org.mx.playbook.Task;
import org.mx.repo.MicroRepositoryBean;
import org.mx.server.ScriptGateway;
import org.mx.ssh.SSHConnect;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fsbsilva on 3/20/17.
 */
public class JobQuartzThread implements Job {

    private final static Logger logger = Logger.getLogger(JobQuartzThread.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Task playbookTask = (Task)context.getJobDetail().getJobDataMap().get("playbookTask");
            MicroRepositoryBean playbookRepo = playbookTask.getMicroRepository();

            for(MicroJobBean microJobBean : playbookRepo.getJobs() ){

                if( playbookTask.getValue() == null ) {
                    System.out.println("==> " + microJobBean.getName());
                } else {
                    connectSSH(playbookTask);
                }

                if (microJobBean.getField().equals("src")) {
                    Task jobTask = playbookTask.clone();
                    jobTask.setSource(microJobBean.getValue());
                    jobTask.setValue(playbookTask.getValue());
                    (new ScriptGateway()).execute(jobTask);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (JobExecutionException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (CloneNotSupportedException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void connectSSH(Task playbookTask) throws JobExecutionException {
        SSHConnect sshConnect = new SSHConnect();
        playbookTask.setSSHConnect(sshConnect);
        String serverDomain = null;

        MicroJobProxyBean proxy = null;

        if( playbookTask.getThread()!= null ) {
            String proxyName = playbookTask.getThread().getProxyName();
            String username = playbookTask.getThread().getUsername();
            String password = playbookTask.getThread().getPassword();
            if (playbookTask.getProxies() != null && proxyName != null) {
                proxy = playbookTask.getProxies().get(proxyName);
                if (proxy == null) {
                    logger.error("Proxy name " + proxyName + " doesn't exist");
                    throw new JobExecutionException("Proxy name " + proxyName + " doesn't exist");
                }
            }

            if( username == null || username.isEmpty()){
                logger.error("Username is empty ");
                throw new JobExecutionException("Username is empty ");
            }

            if( password == null || password.isEmpty()){
                logger.error("Password is empty ");
                throw new JobExecutionException("Username is empty ");
            }

            if (proxy != null) {
                ProxySOCKS5 proxySOCKS5 = new ProxySOCKS5(proxy.getHost(), proxy.getPort());
                sshConnect.setProxySOCKS5(proxySOCKS5);

                serverDomain = getProxyServerDomain(playbookTask, proxySOCKS5);
                if (serverDomain != null) {
                    playbookTask.setValue(serverDomain);
                }
            }

            // It means that if it is using Proxy and got null, probably server doesn't exist or it is down
            if (serverDomain != null) {
                Credential credential = new Credential();
                credential.setUserName(username);
                credential.setPassword(password, false);

                sshConnect.setConnectionTimeOut(playbookTask.getThread().getConnectionTimeout());
                sshConnect.setSessionTimeOut(playbookTask.getThread().getSessionTimeout());
                try {
                    sshConnect.connect(serverDomain, credential);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                } catch (JSchException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            } else {
                logger.error("Server " + playbookTask.getValue() + " doesn't exist or it is down");
                throw new JobExecutionException("Server " + playbookTask.getValue() + " doesn't exist or it is down");
            }
        }


    }

	private String getProxyServerDomain(Task playbookTask, ProxySOCKS5 proxy) throws JobExecutionException {
        MicroJobProxyBean proxyBean = playbookTask.getProxies().get(playbookTask.getThread().getProxyName());

        int timeout = 5000;
        if( playbookTask.getThread().getConnectionTimeout() > 0 ){
            timeout = playbookTask.getThread().getConnectionTimeout();
        }
        try {
            String serverAddrStr = playbookTask.getValue();
            logger.debug("Micro-Server is trying to connect on "+serverAddrStr);


            proxy.connect(null, serverAddrStr, 22, timeout);
            proxy.getSocket().close();
            return serverAddrStr;
        } catch (Throwable e) {
            if (proxyBean.getDomains() != null && proxyBean.getDomains().size() > 0) {
                return connectDomainRuleGateway(playbookTask, proxy);
            } else {
                throw new JobExecutionException(e.getMessage());
            }
        }
    }

    private String connectDomainRuleGateway(Task playbookTask, ProxySOCKS5 proxy) throws JobExecutionException{
        MicroJobProxyBean proxyBean = playbookTask.getProxies().get(playbookTask.getThread().getProxyName());
        logger.debug("Trying first, to connect directly without the DOMINIO");

        String serverAddrStr = playbookTask.getValue();

        String regexIP = "\\d?\\d?\\d[.]\\d?\\d?\\d[.]\\d?\\d?\\d[.]\\d?\\d?\\d";
        Pattern patternIP = Pattern.compile( regexIP );
        Matcher matcherIP = patternIP.matcher( serverAddrStr );

        if( serverAddrStr.matches( regexIP ) && matcherIP.matches() ) {
            throw new JobExecutionException("Address IP "+serverAddrStr+" doesn't exist");
        }

        int timeout = 5000;
        if( playbookTask.getThread().getConnectionTimeout() > 0 ){
            timeout = playbookTask.getThread().getConnectionTimeout();
        }

        for(MicroJobDomainBean microJobDomainBean : proxyBean.getDomains().values() ) {
            StringTokenizer token = new StringTokenizer(serverAddrStr, ".");
            String serverName = token.nextToken();
            String regex = microJobDomainBean.getRegexp();
            Pattern pattern = Pattern.compile(regex);

            Matcher matcher = pattern.matcher(serverName);

            if (serverName.matches(regex)) {
                if (matcher.matches()) {

                    String domainName = microJobDomainBean.getName();
                    if( domainName.startsWith(".")) {
                        domainName = domainName.substring(1);
                    }
                    serverName = serverName + "." + domainName;
                    logger.debug("DOMINIO="+serverName);
					/*
					 * If the gateway TIMEOUT was expired, we must disconnect
					 * and connect again without TIMEOUT
					 */
					if( proxy != null ) {
                        try {

                            proxy.connect(null, serverName, 22, timeout);
                            proxy.getSocket().close();
                            return serverName;
                        } catch (Throwable e) {
                            logger.error("Application was trying to connect on " + serverName + " but doesn't exist");
                        }
                    }
                }
            }
        }
        return null;
    }

}
