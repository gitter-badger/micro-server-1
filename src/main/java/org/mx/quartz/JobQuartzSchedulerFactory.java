package org.mx.quartz;

import org.mx.action.MicroActionBean;
import org.mx.repo.MicroRepositoryBean;
import org.quartz.*;
import org.quartz.impl.DirectSchedulerFactory;

/**
 * Created by fsbsilva on 12/30/16.
 */
public class JobQuartzSchedulerFactory {

    private static Scheduler scheduler = null;

    public static void scheduleJobs(MicroRepositoryBean microRepository){

        microRepository.getActions().keySet().forEach(microActionName -> {
            MicroActionBean microAction = microRepository.getActions().get(microActionName);
            if( microAction.getCron() != null ){
                scheduleJob(microRepository, microAction);
            }

        });

        if( scheduler != null){
            start();
        }
    }

    private static void scheduleJob(MicroRepositoryBean microRepository, MicroActionBean microAction) {
        try {
            DirectSchedulerFactory schedulerFactory = DirectSchedulerFactory.getInstance();
            schedulerFactory.createVolatileScheduler(10);

            if (scheduler == null)
                scheduler = schedulerFactory.getScheduler();


            String name = microAction.getName();
            String cronSchedule = microAction.getCron();

            JobDetail jobDetail = JobBuilder.newJob(JobQuartzScheduled.class)
                    .withIdentity(name, "MicroJobGroup[" + name + "]").build();

            jobDetail.getJobDataMap().put("microAction", microAction);
            jobDetail.getJobDataMap().put("microRepository", microRepository);

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
