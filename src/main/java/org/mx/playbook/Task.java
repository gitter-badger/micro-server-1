package org.mx.playbook;

import org.mx.job.*;
import org.mx.repo.MicroRepositoryBean;
import org.mx.ssh.SSHConnect;
import org.mx.ssh.SSHShell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fsbsilva on 3/15/17.
 */
public class Task implements Cloneable {

    private SSHConnect sshConnect;
    private SSHShell sshShell;
    private MicroRepositoryBean microRepository;
    private String source;
    private boolean type;
    private String logLevel = "INFO";
    private Map<String, Object> data = new HashMap<String,Object>();
    private String[] args;
    private boolean async;
    private String actionName;
    private String moduleName;
    private String value;
    private MicroJobThreadBean thread;
    private String username;
    private String password;

    public Task(MicroRepositoryBean microRepository){
        this.microRepository = microRepository;
    }

    public MicroRepositoryBean getMicroRepository() {
        return microRepository;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void addData(String name, Object data) {
        this.data.put(name,data);
    }

    public String getSource() {
        return source;
    }

    public MicroJobThreadBean getThread() {
        return thread;
    }

    public void setThread(MicroJobThreadBean thread) {
        this.thread = thread;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public Map<String, MicroJobVariableBean> getVariables(){
        return this.microRepository.getVariables();
    }

    public Map<String, MicroJobArgumentBean> getArguments(){
        return this.microRepository.getArguments();
    }

    public List<MicroJobProxyBean> getProxies(){
        return this.microRepository.getProxies();
    }

    public List<MicroJobDomainBean> getDomains(){
        return this.microRepository.getDomains();
    }

    public void setSSHConnect(SSHConnect sshConnect){
        this.sshConnect = sshConnect;
    }

    public SSHShell ssh() {
        if( this.sshConnect != null && this.sshConnect.isConnected() ) {
            if( this.sshShell == null){
                this.sshShell = new SSHShell(sshConnect);
            }
            return sshShell;
        }
        return null;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public void error(String value){
        if( logLevel.equals("ERROR")  )
            System.out.println("    "+value);
    }

    public void warn(String value){
        if( logLevel.equals("ERROR") || logLevel.equals("WARN")  )
            System.out.println("    "+value);
    }

    public void info(String value){
        if( logLevel.equals("ERROR") || logLevel.equals("WARN") || logLevel.equals("INFO")  )
            System.out.println("    "+value);
    }

    public void debug(String value){
        if( logLevel.equals("ERROR") || logLevel.equals("INFO") || logLevel.equals("WARN") || logLevel.equals("DEBUG") )
            System.out.println("    "+value);
    }

    public Task clone() throws CloneNotSupportedException {
        return (Task)super.clone();
    }

}
