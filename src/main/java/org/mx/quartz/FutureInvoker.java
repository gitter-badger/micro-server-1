package org.mx.quartz;

import org.mx.playbook.PlayBookManager;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * Created by fsbsilva on 3/20/17.
 */
public class FutureInvoker implements Callable<Boolean> {

    private PlayBookManager executor;
    private final BlockingQueue queue = new ArrayBlockingQueue(1);
    // can't add null to a BlockingQueue, so we might have to add a marker instead
    private static final Boolean RESULT_AS_NULL = true;

    public FutureInvoker(final PlayBookManager executor) {
        this.executor = executor;
    }

    public void executeJobs() throws Exception {
        executor.executeJobs();
        queue.put(true);
    }

    public Boolean call() throws Exception {
        executor.executeJobs();
        return true;
//        return (Boolean) queue.take();
    }
}
