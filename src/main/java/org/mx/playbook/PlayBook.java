package org.mx.playbook;

import com.jcraft.jsch.ProxySOCKS5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fsbsilva on 1/15/17.
 */
public class PlayBook {

    private String name;
    private List<String> moduleList;
    private Map<String, Object> variableMap = new HashMap<String, Object>();

    private List<Task> taskList = new ArrayList<Task>();
    private List<ProxySOCKS5> proxyList = new ArrayList<ProxySOCKS5>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getVariableMap() {
        return variableMap;
    }

    public void putVariable(String key, Object value) {
        this.variableMap.put(key, value);
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void addTask(Task task) {
        this.taskList.add(task);
    }

    public List<String> getModuleList() {
        return moduleList;
    }

    public void addModule(String moduleName) {
        this.moduleList.add(moduleName);
    }

    public List<ProxySOCKS5> getProxyList() {
        return proxyList;
    }

}
