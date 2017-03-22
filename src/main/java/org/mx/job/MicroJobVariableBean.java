package org.mx.job;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fsbsilva on 3/12/17.
 */
public class MicroJobVariableBean {
    private String name;
    private String value;
    private ArrayList<String> array = new ArrayList<String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ArrayList<String> getArrayList() {
        return array;
    }

    public void add(String variable) {
        this.array.add(variable);
    }
}
