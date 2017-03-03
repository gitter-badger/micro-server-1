package org.mx.quartz;

import org.mx.maction.MicroAction;
import org.mx.mrepo.MicroRepository;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Created by fsbsilva on 12/30/16.
 */
public class MicroSchedulerFactory {

    private static Scheduler scheduler = null;

    public static void scheduleJobs(MicroRepository microActionMap){

        microActionMap.getMicroActionMap().keySet().forEach(microActionName -> {
            MicroAction microAction = microActionMap.getMicroActionMap().get(microActionName);
            if( microAction.getCron() != null ){
                scheduleJob(microAction);
            }

        });

        if( scheduler != null){
            start();
        }
    }

    private static void scheduleJob(MicroAction microAction) {
        try {
            if (scheduler == null)
                scheduler = new StdSchedulerFactory().getScheduler();

            String name = microAction.getName();
            String cronSchedule = microAction.getCron();

            JobDetail jobDetail = JobBuilder.newJob(MicroJob.class)
                    .withIdentity(name, "MicroJobGroup[" + name + "]").build();

            jobDetail.getJobDataMap().put("microAction", microAction);

            Trigger trigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("MicroTrigger[" + name + "]", "MicroTriggerGroup[" + name + "]")
                    .withSchedule(
                            CronScheduleBuilder.cronSchedule(cronSchedule))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private static void start() {
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}
