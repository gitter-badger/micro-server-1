package org.mx.mrepo;

import com.jcraft.jsch.ProxySOCKS5;
import org.mx.maction.MicroAction;
import org.mx.mmodule.MicroModule;

import java.util.*;

/**
 * Created by fsbsilva on 12/31/16.
 */
public class MicroRepository {

    private String inventoryPath;

    private Map<String, MicroAction> microActionMap = new HashMap<String, MicroAction>();

    private Map<String, MicroModule> microModuleMap = new HashMap<String, MicroModule>();

    private List<ProxySOCKS5> proxyList = new ArrayList<ProxySOCKS5>();

    public String getInventoryPath() {
        return inventoryPath;
    }

    public void setInventoryPath(String inventoryPath) {
        this.inventoryPath = inventoryPath;
    }

    public Map<String, MicroAction> getMicroActionMap() {
        return microActionMap;
    }

    public Map<String, MicroModule> getMicroModuleMap() {
        return microModuleMap;
    }

    public List<ProxySOCKS5> getProxyList() {
        return proxyList;
    }

}
