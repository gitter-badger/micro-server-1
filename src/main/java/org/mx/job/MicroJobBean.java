package org.mx.job;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxySOCKS5;
import org.mx.oauth.client.Credential;
import org.mx.ssh.*;

import java.io.IOException;
import java.net.SocketException;

/**
 * Created by fsbsilva on 2/24/17.
 */
public class MicroJobBean {
    private String name;
    private String field;
    private String value;
    private String type;

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

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getArgs() {
        return value.split(value);
    }

}
