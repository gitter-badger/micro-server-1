package org.mx.quartz;

import org.mx.playbook.Task;
import org.quartz.*;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.JobStore;
import org.quartz.spi.ThreadPool;

import java.util.UUID;

/**
 * Created by fsbsilva on 3/20/17.
 */
public class JobQuartzThreadProvider {

    private Scheduler scheduler;
    private UUID instanceId;

    /**
     * Initialises the scheduler.
     */
    public void initProvider(int threadCound ) {
        try {
            instanceId = UUID.randomUUID();
            JobStore jobStore = new RAMJobStore();
            ThreadPool threadPool = new SimpleThreadPool(threadCound, Thread.NORM_PRIORITY);
            threadPool.initialize();
            final DirectSchedulerFactory schedulerFactory = DirectSchedulerFactory.getInstance();
            String schedulerName = instanceId.toString();
            schedulerFactory.createScheduler(schedulerName, instanceId.toString(), threadPool, jobStore);
            scheduler = schedulerFactory.getScheduler(schedulerName);
            scheduler.start();
        } catch (SchedulerException ex) {
            ex.printStackTrace();
        }
    }

    public void execute(Task task) throws SchedulerException {
        final String identityID = UUID.randomUUID().toString();
        final String playbookName = task.getMicroRepository().getName();

        JobDetail jobDetail = JobBuilder.newJob(JobQuartzThread.class)
                .withIdentity(identityID, "PlayBookBean["+playbookName+"]").build();

        jobDetail.getJobDataMap().put("task", task);


        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(identityID, "PlayBookBean["+playbookName+"]")
                .startNow()
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * Shutdown the scheduler on application close.
     */
    public void destroyProvider() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException ex) {
            ex.printStackTrace();
        }
    }

    public void start() {
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    public Scheduler getScheduler() {
        return scheduler;
    }

    public UUID getInstanceId() {
        return instanceId;
    }

}
