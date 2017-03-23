package org.mx.server;

import org.mx.repo.MicroRepositoryBean;
import org.mx.var.MicroVariableMap;
import org.quartz.Scheduler;

import java.util.HashMap;

/**
 * Created by fsbsilva on 1/5/17.
 */
public class GlobalVariableService {

    private static MicroVariableMap globalVariable;
    private static MicroVariableMap actionVariableScope;
    private static MicroServer microServer;
    private static MicroRepositoryBean microRepository;
    private static HashMap<String,Scheduler> threads = new HashMap<String,Scheduler>();

    public static MicroVariableMap getGlobalVariable() {
        return globalVariable;
    }

    public static void setGlobalVariable(MicroVariableMap globalVariable) {
        GlobalVariableService.globalVariable = globalVariable;
    }

    public static MicroServer getMicroServer() {
        return microServer;
    }

    public static void setMicroServer(MicroServer microServer) {
        GlobalVariableService.microServer = microServer;
    }

    public static MicroVariableMap getActionVariableScope() {
        return actionVariableScope;
    }

    public static void setActionVariableScope(MicroVariableMap actionVariableScope) {
        GlobalVariableService.actionVariableScope = actionVariableScope;
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
