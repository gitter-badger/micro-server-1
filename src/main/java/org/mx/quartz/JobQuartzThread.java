package org.mx.quartz;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxySOCKS5;
import org.apache.log4j.Logger;
import org.mx.action.MicroActionBean;
import org.mx.job.MicroJobBean;
import org.mx.job.MicroJobDomainBean;
import org.mx.job.MicroJobProxyBean;
import org.mx.playbook.Task;
import org.mx.repo.MicroRepositoryBean;
import org.mx.server.ScriptGateway;
import org.mx.ssh.SSHConnect;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.IOException;
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
            e.printStackTrace();
        } catch (JobExecutionException e) {
            e.printStackTrace();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private void connectSSH(Task playbookTask){
        if( playbookTask.getProxies().size() > 0) {
            for (MicroJobProxyBean proxy : playbookTask.getProxies()) {
                ProxySOCKS5 proxySOCKS5 = new ProxySOCKS5(proxy.getHost(), proxy.getPort());
                SSHConnect sshConnect = new SSHConnect();
                sshConnect.setProxySOCKS5(proxySOCKS5);
                playbookTask.setSSHConnect(sshConnect);
            }
        } else {

        }
    }

	private String getDomainRule(Task playbookTask) throws JobExecutionException {
        ProxySOCKS5 proxy = playbookTask.ssh().getProxySOCKS5();
        try {
            String serverAddrStr = playbookTask.getValue();
            logger.debug("MXTerminal is trying first to connect, directly using "+serverAddrStr);

            proxy.connect(null, playbookTask.getValue(), 22, 0);
            proxy.getSocket().close();
            return serverAddrStr;
        } catch (Throwable e) {
            if (playbookTask.getDomains() != null && playbookTask.getDomains().size() > 0) {
                return connectDomainRuleGateway(playbookTask, proxy);
            } else {
                throw new JobExecutionException(e.getMessage());
            }
        }
    }

    private String connectDomainRuleGateway(Task playbookTask, ProxySOCKS5 proxy) {
        logger.debug("Trying first, to connect directly without the DOMINIO");

        String serverAddrStr = playbookTask.getValue();

        String regexIP = "\\d?\\d?\\d[.]\\d?\\d?\\d[.]\\d?\\d?\\d[.]\\d?\\d?\\d";
        Pattern patternIP = Pattern.compile( regexIP );
        Matcher matcherIP = patternIP.matcher( serverAddrStr );

        if( serverAddrStr.matches( regexIP ) && matcherIP.matches() ) {
            return null;
        }

        for(MicroJobDomainBean microJobDomainBean : playbookTask.getDomains()) {
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
                    try {
                        proxy.connect(null, serverName, 22, 8000);
                        proxy.getSocket().close();
                        return serverName;
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error("ConnectException"+e.getMessage());
                    } catch (JSchException e) {
                        e.printStackTrace();
                        logger.error("ConnectException"+e.getMessage());
                    }
                }
            }
        }
        return null;
    }

}
