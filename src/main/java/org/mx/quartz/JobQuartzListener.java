package org.mx.quartz;

import org.mx.server.GlobalVariableService;
import org.quartz.*;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by fsbsilva on 3/22/17.
 */
public class JobQuartzListener implements JobListener {

    public static final String LISTENER_NAME = "JobQuartzListener";
    private ArrayBlockingQueue blockingQueue;

    public JobQuartzListener(ArrayBlockingQueue blockingQueue){
        this.blockingQueue = blockingQueue;
    }

    @Override
    public String getName() {
        return LISTENER_NAME;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {

    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {

    }

    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        Scheduler scheduler = jobExecutionContext.getScheduler();
        try {
            if( scheduler.getCurrentlyExecutingJobs().size() == 1){
                scheduler.clear();
                scheduler.shutdown();
                GlobalVariableService.getThreads().remove(scheduler.getSchedulerName());
                blockingQueue.put("notify");
            }
        } catch (SchedulerException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }
}
