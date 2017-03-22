package org.mx.playbook;

import org.mx.job.MicroJobBean;
import org.mx.job.MicroJobInventoryBean;
import org.mx.job.MicroJobThreadBean;
import org.mx.quartz.JobQuartzThreadProvider;
import org.mx.repo.MicroRepositoryBean;
import org.mx.repo.MicroRepositoryFactory;
import org.mx.server.ScriptGateway;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.io.IOException;
import java.util.ArrayList;
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
            MicroJobInventoryBean microJobInventory = this.getMicroJobInventoryBean(microJobThread);

            JobQuartzThreadProvider jobQuartzThreadProvider = new JobQuartzThreadProvider();
            jobQuartzThreadProvider.initProvider(microJobThread.getNumber());
            Scheduler scheduler = jobQuartzThreadProvider.getScheduler();

            for ( String value : microJobInventory.getArrayList() ) {
                Task task = new Task(playbookRepo);
                task.setArgs(task.getArgs());
                task.addData("data",value);
                task.addData("microJobThread",microJobThread);
                task.setLogLevel(playbookRepo.getLogLevel());
                jobQuartzThreadProvider.execute(task);
            }

            Thread.sleep(2000);
            int count = scheduler.getCurrentlyExecutingJobs().size();
//            System.out.println(count);
            while( count > 0 ){
                Thread.sleep(500);
                count = jobQuartzThreadProvider.getScheduler().getCurrentlyExecutingJobs().size();
//                System.out.println(count);
            }
            System.out.println("Playbook has finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void executeJobs(){
        try {
            JobQuartzThreadProvider jobQuartzThreadProvider = new JobQuartzThreadProvider();
            jobQuartzThreadProvider.initProvider(1);

            Task task = new Task(playbookRepo);
            task.setArgs(task.getArgs());
            task.setLogLevel(playbookRepo.getLogLevel());

            jobQuartzThreadProvider.execute(task);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private MicroJobInventoryBean getMicroJobInventoryBean(MicroJobThreadBean microJobThread) throws JobExecutionException {
        MicroJobInventoryBean microJobInventory = null;
        if( microJobThread.getData() == null ){
            throw new JobExecutionException("Thread Group can't find inventory");
        } else {
            String inventory = microJobThread.getData().replace("${","");
            inventory = inventory.replace("}","");
            microJobInventory = playbookRepo.getInventories().get(inventory);
        }

        if( microJobInventory == null ) {
            throw new JobExecutionException("Thread Group can't find inventory " + microJobThread.getData());
        }
        return microJobInventory;

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
