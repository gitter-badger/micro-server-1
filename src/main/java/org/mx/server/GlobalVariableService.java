package org.mx.server;

import org.mx.repo.MicroRepositoryBean;
import org.mx.var.MicroVariableMap;
import org.quartz.Scheduler;

import java.util.HashMap;

/**
 * Created by fsbsilva on 1/5/17.
 */
public class GlobalVariableService {

    private static MicroVariableMap microServerEnvironmetVariable;
    private static MicroVariableMap repositoryEnvironmentVariable;
    private static MicroServer microServer;
    private static MicroRepositoryBean microRepository;
    private static HashMap<String,Scheduler> threads = new HashMap<String,Scheduler>();

    public static MicroVariableMap getMicroServerEnvironmetVariable() {
        return microServerEnvironmetVariable;
    }

    public static void setMicroServerEnvironmetVariable(MicroVariableMap microServerEnvironmetVariable) {
        GlobalVariableService.microServerEnvironmetVariable = microServerEnvironmetVariable;
    }

    public static MicroServer getMicroServer() {
        return microServer;
    }

    public static void setMicroServer(MicroServer microServer) {
        GlobalVariableService.microServer = microServer;
    }

    public static MicroVariableMap getRepositoryEnvironmentVariable() {
        return repositoryEnvironmentVariable;
    }

    public static void setRepositoryEnvironmentVariable(MicroVariableMap repositoryEnvironmentVariable) {
        GlobalVariableService.repositoryEnvironmentVariable = repositoryEnvironmentVariable;
    }

    public static MicroRepositoryBean getMicroRepository() {
        return microRepository;
    }

    public static void setMicroRepository(MicroRepositoryBean microRepository) {
        GlobalVariableService.microRepository = microRepository;
    }

    public static HashMap<String, Scheduler> getThreads() {
        return threads;
    }

    public static void addThread(String uuid, Scheduler scheduler) {
        threads.put(uuid,scheduler);
    }
}
