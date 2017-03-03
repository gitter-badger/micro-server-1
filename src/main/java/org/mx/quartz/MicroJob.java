package org.mx.quartz;

import org.apache.log4j.Logger;
import org.mx.maction.MicroAction;
import org.mx.mserver.ScriptGateway;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by fsbsilva on 12/28/16.
 */
public class MicroJob implements Job {

    private final static Logger logger = Logger.getLogger(MicroJob.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.debug("Executing Schedule quartz for micro-action "+context.getJobDetail().getKey().getName());

        MicroAction microAction = (MicroAction)context.getJobDetail().getJobDataMap().get("microAction");

        ScriptGateway scritpGatway = new ScriptGateway();
        scritpGatway.execute(microAction.getSrc(), null);

    }

}