package org.mx.quartz;

import org.apache.log4j.Logger;
import org.mx.action.MicroActionBean;
import org.mx.job.MicroJobBean;
import org.mx.playbook.Task;
import org.mx.repo.MicroRepositoryBean;
import org.mx.server.ScriptGateway;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.IOException;

/**
 * Created by fsbsilva on 3/20/17.
 */
public class JobQuartzThread implements Job {

    private final static Logger logger = Logger.getLogger(JobQuartzThread.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Task task = (Task)context.getJobDetail().getJobDataMap().get("task");
            MicroRepositoryBean playbookRepo = task.getMicroRepository();

            String inventory = (String)task.getData().get("data");

            for(String name : playbookRepo.getJobs().keySet() ){
                MicroJobBean microJobBean = playbookRepo.getJobs().get(name);
                if( inventory != null ) {
                    System.out.println("==> " + name);
                    logger.debug("Starting Quartz Job with inventory "+inventory);
                }

                if (microJobBean.getField().equals("src")) {
                    Task jobTask = new Task(task.getMicroRepository());
                    jobTask.setLogLevel(task.getLogLevel());
                    jobTask.setSource(microJobBean.getValue());
                    jobTask.setArgs(task.getArgs());

                    (new ScriptGateway()).execute(jobTask);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JobExecutionException e) {
            e.printStackTrace();
        }
    }
}
