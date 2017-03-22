package org.mx.playbook;

import org.mx.job.MicroJobBean;
import org.mx.job.MicroJobProxyBean;
import org.mx.job.MicroJobThreadBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fsbsilva on 1/15/17.
 */
public class PlayBookBean {

    private String name;
    private String logLevel;
    private String threadName;
    private List<String> moduleList;
    private Map<String, Object> variableMap = new HashMap<String, Object>();

    private List<MicroJobBean> jobList = new ArrayList<MicroJobBean>();
    private List<MicroJobProxyBean> proxies = new ArrayList<MicroJobProxyBean>();
    private MicroJobThreadBean jobThread = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Map<String, Object> getVariableMap() {
        return variableMap;
    }

    public void putVariable(String key, Object value) {
        this.variableMap.put(key, value);
    }

    public List<MicroJobBean> getJobs() {
        return jobList;
    }

    public void addJob(MicroJobBean job) {
        this.jobList.add(job);
    }

    public List<String> getModuleList() {
        return moduleList;
    }

    public void addModule(String moduleName) {
        this.moduleList.add(moduleName);
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public List<MicroJobProxyBean> getProxies() {
        return proxies;
    }

    public void addProxy(MicroJobProxyBean jobProxy) {
        this.proxies.add(jobProxy);
    }

    public MicroJobThreadBean getJobThread() {
        return jobThread;
    }

    public void setJobThread(MicroJobThreadBean jobThread) {
        this.jobThread = jobThread;
    }
}
