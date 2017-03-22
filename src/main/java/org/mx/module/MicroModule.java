package org.mx.module;

import com.jcraft.jsch.ProxySOCKS5;
import org.mx.action.MicroActionBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fsbsilva on 2/14/17.
 */
public class MicroModule {

    private String name;
    private String src;
    private String defaultMicroAction;
    private List<ProxySOCKS5> proxyList = new ArrayList<ProxySOCKS5>();


    private Map<String,MicroActionBean> microActionMap = new HashMap<String, MicroActionBean>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public Map<String, MicroActionBean> getMicroActionMap() {
        return microActionMap;
    }

    public String getDefaultMicroAction() {
        return defaultMicroAction;
    }

    public void setDefaultMicroAction(String defaultMicroAction) {
        this.defaultMicroAction = defaultMicroAction;
    }

    public List<ProxySOCKS5> getProxyList() {
        return proxyList;
    }

}
