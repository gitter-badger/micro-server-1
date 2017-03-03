package org.mx.mserver;

import org.mx.mrepo.MicroRepository;
import org.mx.var.MicroVariableMap;

/**
 * Created by fsbsilva on 1/5/17.
 */
public class GlobalVariableService {

    private static MicroVariableMap globalVariable;
    private static MicroVariableMap actionVariableScope;
    private static MicroServer microServer;
    private static MicroRepository microRepository;

    public static MicroVariableMap getGlobalVariable() {
        return globalVariable;
    }

    public static void setGlobalVariable(MicroVariableMap globalVariable) {
        GlobalVariableService.globalVariable = globalVariable;
    }

    public static MicroServer getMicroServer() {
        return microServer;
    }

    public static void setMicroServer(MicroServer microServer) {
        GlobalVariableService.microServer = microServer;
    }

    public static MicroVariableMap getActionVariableScope() {
        return actionVariableScope;
    }

    public static void setActionVariableScope(MicroVariableMap actionVariableScope) {
        GlobalVariableService.actionVariableScope = actionVariableScope;
    }

    public static MicroRepository getMicroRepository() {
        return microRepository;
    }

    public static void setMicroRepository(MicroRepository microRepository) {
        GlobalVariableService.microRepository = microRepository;
    }
}
