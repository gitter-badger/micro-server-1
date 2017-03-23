package org.mx.quartz;

import org.mx.playbook.Task;
import org.mx.server.GlobalVariableService;
import org.quartz.*;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.JobStore;
import org.quartz.spi.ThreadPool;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by fsbsilva on 3/20/17.
 */
public class JobQuartzThreadProvider {

    private Scheduler scheduler;
    private UUID instanceId;
    private ArrayBlockingQueue waitAllJobsHasFinish = new ArrayBlockingQueue(1);
//    private JobKey jobKey;

    /**
     * Initialises the scheduler.
     */
    public void initProvider(int threadCound ) {
        try {
            instanceId = UUID.randomUUID();
//            jobKey = new JobKey(instanceId.toString(), "Playbook-Group");

            JobStore jobStore = new RAMJobStore();
            ThreadPool threadPool = new SimpleThreadPool(threadCound, Thread.NORM_PRIORITY);
            threadPool.initialize();

            final DirectSchedulerFactory schedulerFactory = DirectSchedulerFactory.getInstance();
            String schedulerName = instanceId.toString();
            schedulerFactory.createScheduler(schedulerName, instanceId.toString(), threadPool, jobStore);

            scheduler = schedulerFactory.getScheduler(schedulerName);
            scheduler.getListenerManager().addJobListener(new JobQuartzListener(waitAllJobsHasFinish) );

            scheduler.start();
            GlobalVariableService.addThread(instanceId.toString(),scheduler);

        } catch (SchedulerException ex) {
            ex.printStackTrace();
        }
    }

    public void execute(Task playbookTask) throws SchedulerException {
        final String identityID = UUID.randomUUID().toString();
        final String playbookName = playbookTask.getActionName();

        JobDetail jobDetail = JobBuilder.newJob(JobQuartzThread.class)
                .withIdentity(identityID, "MicroAction["+playbookName+"]").build();

        jobDetail.getJobDataMap().put("playbookTask", playbookTask);


        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(identityID, "PlayBookBean["+playbookName+"]")
                .startNow()
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }


    public Scheduler getScheduler() {
        return scheduler;
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public ArrayBlockingQueue getWaitAllJobsHasFinish() {
        return waitAllJobsHasFinish;
    }

}
