package org.mx.mclient;

import java.util.Date;

/**
 * Created by fsbsilva on 11/29/16.
 */
public class MicroClientSession {

    private String scriptPath;
    private String[] args;
    private String actionName;
    private String moduleName;
    private Date callDate;
    private boolean isASync = false;

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public String[] getArgs() {
        return this.args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public Date getCallDate() {
        return callDate;
    }

    public void setCallDate(Date callDate) {
        this.callDate = callDate;
    }

    public boolean isASync() {
        return isASync;
    }

    public void setASync(boolean ASync) {
        isASync = ASync;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
}
