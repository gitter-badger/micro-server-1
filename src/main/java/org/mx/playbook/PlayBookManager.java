package org.mx.playbook;

import org.mx.job.MicroJobInventoryBean;
import org.mx.job.MicroJobThreadBean;
import org.mx.quartz.JobQuartzThreadProvider;
import org.mx.repo.MicroRepositoryBean;
import org.mx.repo.MicroRepositoryFactory;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.io.IOException;
import java.util.List;

/**
 * Created by fsbsilva on 3/20/17.
 */
public class PlayBookManager {

    private MicroRepositoryBean playbookRepo;
    private Task task;

    public PlayBookManager(Task task){
        this.task = task;
        init();
    }

    private void init(){
        try {
            playbookRepo = MicroRepositoryFactory.parser(task.getMicroRepository(), task.getSource());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("+---------------------------------------------------------------------+");
        System.out.println("+ "+playbookRepo.getName());
        System.out.println("+---------------------------------------------------------------------+");
    }

    public void execute() throws JobExecutionException {
        MicroJobThreadBean microJobThread = getMicroJobThread();

        if( microJobThread != null ){
            executeThread(microJobThread);
        } else {
            executeJobs();
        }
    }

    public void executeThread(MicroJobThreadBean microJobThread)  {
        try {
            List<String> list = this.getMicroJobInventoryBean(microJobThread);

            JobQuartzThreadProvider jobQuartzThreadProvider = new JobQuartzThreadProvider();
            jobQuartzThreadProvider.initProvider(microJobThread.getNumber());
            Scheduler scheduler = jobQuartzThreadProvider.getScheduler();

            for( String value : list ) {
                 Task playbookTask = this.task.clone();
                 playbookTask.setThread(microJobThread);
                 playbookTask.setValue(value);

                 jobQuartzThreadProvider.execute(playbookTask);
            }

            if( this.task.isAsync() ) {
                System.out.println("");
                System.out.println("  Playbook is been running in background");
            } else {
                jobQuartzThreadProvider.getWaitAllJobsHasFinish().take();
                System.out.println("");
                System.out.println("  Playbook has finished");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SchedulerException e) {
            e.printStackTrace();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

//    public void waitJobFinish(){
//        List list = new ArrayBlockingQueue();
//    }

    public void executeJobs(){
        try {
            JobQuartzThreadProvider jobQuartzThreadProvider = new JobQuartzThreadProvider();
            jobQuartzThreadProvider.initProvider(1);

            jobQuartzThreadProvider.execute(this.task);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private List<String> getMicroJobInventoryBean(MicroJobThreadBean microJobThread) throws JobExecutionException {
        MicroJobInventoryBean microJobInventory = null;
        if( (microJobThread.getHosts() == null || microJobThread.getHosts().isEmpty()) && microJobThread.getList().isEmpty() ){
            throw new JobExecutionException("Thread Group's fields hosts or array can't be Null or empty");
        } else {
            if( microJobThread.getHosts() != null && !microJobThread.getHosts().isEmpty() ) {
                String inventory = microJobThread.getHosts().replace("${", "");
                inventory = inventory.replace("}", "");
                microJobInventory = playbookRepo.getInventories().get(inventory);

                if( microJobInventory == null ) {
                    throw new JobExecutionException("Thread Group can't find inventory " + microJobThread.getHosts());
                }

                return microJobInventory.getList();
            } else if( ! microJobThread.getList().isEmpty() ){
                return microJobThread.getList();
            }
        }

        return null;
    }

    private MicroJobThreadBean getMicroJobThread() throws JobExecutionException {
        String threadName = playbookRepo.getThreadName();
        MicroJobThreadBean microJobThreadBean = null;
        if( playbookRepo.getThreadName() != null && !playbookRepo.getThreadName().isEmpty() ){
            microJobThreadBean = playbookRepo.getThreads().get(threadName);
            if( microJobThreadBean == null ){
                throw new JobExecutionException("Thread Group "+threadName+" doesn't exist");
            }
        }

        return microJobThreadBean;
    }
}
