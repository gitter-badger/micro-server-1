package org.mx.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fsbsilva on 3/7/17.
 */
public class MicroJobProxyBean {

    private String name;
    private String host;
    private int port;
    private Map<String, MicroJobDomainBean> domains = new HashMap<String, MicroJobDomainBean>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, MicroJobDomainBean> getDomains() {
        return domains;
    }

    public void addDomain(String name, MicroJobDomainBean domain) {
        this.domains.put(name, domain);
    }
}
