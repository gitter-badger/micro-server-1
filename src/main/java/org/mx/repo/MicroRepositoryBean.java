package org.mx.repo;

import org.jruby.ir.operands.Hash;
import org.mx.action.MicroActionBean;
import org.mx.job.*;
import org.mx.module.MicroModule;

import java.util.*;

/**
 * Created by fsbsilva on 12/31/16.
 */
public class MicroRepositoryBean implements Cloneable {

    private String name;
    private String scope = "root"; // root, module, playbook
    private String threadName;

    private Map<String, MicroActionBean> actions = new HashMap<String, MicroActionBean>();

    private Map<String, MicroModule> modules = new HashMap<String, MicroModule>();

    private Map<String, MicroJobProxyBean> proxies = new HashMap<String, MicroJobProxyBean>();

    private Map<String, MicroJobInventoryBean> inventories = new HashMap<String, MicroJobInventoryBean>();

    private String logLevel = "ERROR";

    private Map<String, MicroJobVariableBean> variables = new HashMap<String, MicroJobVariableBean>();

    private Map<String, MicroJobArgumentBean> arguments = new HashMap<String, MicroJobArgumentBean>();

    private List<MicroJobBean> jobs = new ArrayList<MicroJobBean>();

    private Map<String, MicroJobThreadBean> threads = new HashMap<String, MicroJobThreadBean>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Map<String, MicroActionBean> getActions() {
        return actions;
    }

    public void addAction(String name, MicroActionBean microAction){
        this.actions.put(name,microAction);
    }

    public Map<String, MicroModule> getModules() {
        return modules;
    }

    public void addModule(String name, MicroModule microModule){
        this.modules.put(name,microModule);
    }

    public Map<String, MicroJobProxyBean> getProxies() {
        return proxies;
    }

    public void addProxy(String name, MicroJobProxyBean jobProxy) {
        this.proxies.put(name, jobProxy);
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public Map<String, MicroJobInventoryBean> getInventories() {
        return inventories;
    }

    public void addInventory(String name, MicroJobInventoryBean jobInventory) {
        this.inventories.put(name, jobInventory);
    }

    public Map<String, MicroJobVariableBean> getVariables() {
        return variables;
    }

    public void addVariable(String name, MicroJobVariableBean jobVariable) {
        this.variables.put(name,jobVariable);
    }

    public Map<String, MicroJobArgumentBean> getArguments() {
        return arguments;
    }

    public void addArgument(String name, MicroJobArgumentBean jobArgument) {
        this.arguments.put(name,jobArgument);
    }


    public List<MicroJobBean> getJobs() {
        return jobs;
    }

    public void addJob(MicroJobBean microJob){
        this.jobs.add(microJob);
    }

    public Map<String, MicroJobThreadBean> getThreads() {
        return threads;
    }

    public void addThreads(String name, MicroJobThreadBean microJobThread){
        this.threads.put(name,microJobThread);
    }

    public MicroRepositoryBean clone() throws CloneNotSupportedException {
        return (MicroRepositoryBean)super.clone();
    }
}
