package org.mx.job;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fsbsilva on 3/10/17.
 */
public class MicroJobInventoryBean {

    private String name;
    private String src;
    private List<String> array = new ArrayList<String>();
    private boolean loaded = false;

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

    public List<String> getList() {
        return array;
    }

    public void add(String inventory) {
        this.array.add(inventory);
        this.loaded = true;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
}
