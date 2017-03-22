package org.mx.playbook;

import org.mx.job.*;
import org.mx.repo.MicroRepositoryBean;
import org.mx.ssh.SSHConnect;
import org.mx.ssh.SSHShell;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fsbsilva on 3/15/17.
 */
public class Task {

    private SSHConnect sshConnect;
    private SSHShell sshShell;
    private MicroRepositoryBean microRepository;
    private String source;
    private boolean type;
    private boolean stopping = false;
    private String logLevel = "INFO";
    private boolean first = true;
    private boolean hasError = false;
    private boolean complete = false;
    private boolean running = false;
    private Map<String, Object> data = new HashMap<String,Object>();
    private String[] args;

    public Task(MicroRepositoryBean microRepository){
        this.microRepository = microRepository;
    }

    public MicroRepositoryBean getMicroRepository() {
        return microRepository;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
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

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public boolean isStopping() {
        return stopping;
    }

    public void setStopping(boolean stopping) {
        this.stopping = stopping;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean hasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Map<String, MicroJobVariableBean> getVariables(){
        return this.microRepository.getVariables();
    }

    public Map<String, MicroJobArgumentBean> getArguments(){
        return this.microRepository.getArguments();
    }

    public Map<String, MicroJobProxyBean> getProxies(){
        return this.microRepository.getProxies();
    }

    public Map<String, MicroJobDomainBean> getDomains(){
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

	public void stopJob() {
        this.running = false;
        this.complete = true;
        setStopping(true);
        if ( this.ssh().isConnected() ){
            this.ssh().disconnect();
        }
    }

}
