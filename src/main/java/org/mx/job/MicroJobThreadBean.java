package org.mx.job;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fsbsilva on 3/7/17.
 */
public class MicroJobThreadBean {

    private String name;
    private int number;
    private String hosts;
    private String logLevel;
    private String username;
    private String password;
    private List<String> array = new ArrayList<String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public List<String> getList() {
        return array;
    }

    public void add(String inventory) {
        this.array.add(inventory);
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
}
