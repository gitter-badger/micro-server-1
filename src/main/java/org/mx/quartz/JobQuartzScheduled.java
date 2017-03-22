package org.mx.quartz;

import org.apache.log4j.Logger;
import org.mx.action.MicroActionBean;
import org.mx.repo.MicroRepositoryBean;
import org.mx.server.ScriptGateway;
import org.mx.playbook.Task;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.IOException;

/**
 * Created by fsbsilva on 12/28/16.
 */
public class JobQuartzScheduled implements Job {

    private final static Logger logger = Logger.getLogger(JobQuartzScheduled.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.debug("Executing Schedule quartz for micro-action "+context.getJobDetail().getKey().getName());

        MicroActionBean microAction = (MicroActionBean)context.getJobDetail().getJobDataMap().get("microAction");
        MicroRepositoryBean microRepository = (MicroRepositoryBean)context.getJobDetail().getJobDataMap().get("microRepository");

        try {
            Task task = new Task(microRepository);
            task.setSource(microAction.getSrc());
            (new ScriptGateway()).execute(task);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}