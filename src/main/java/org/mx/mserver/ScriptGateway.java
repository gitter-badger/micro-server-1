package org.mx.mserver;

import org.apache.log4j.Logger;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.javasupport.JavaEmbedUtils;
import org.mx.playbook.PlayBook;
import org.mx.playbook.PlayBookFactory;
import org.mx.var.MicroVariableMap;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import java.io.*;
import java.util.Map;

import groovy.lang.GroovyShell;

/*

 Copyright 2004-2015, MXDeploy Software, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */

public class ScriptGateway {

    final static Logger logger = Logger.getLogger(ScriptGateway.class);

    public  String MX_HOME;
    public  String MX_JAVA_CLASSPATH;
    public  String MX_JYTHON_MODULES_PATH;

    private Map<String, Object> variableMap = null;

    public ScriptGateway(){
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {
        MX_HOME = System.getProperty("mx.mserver.home");
        if( MX_HOME == null ){
            new Throwable("The JVM option -Dmx.mserver.home must to be declared during start up!");
        }

        MicroVariableMap microVariableMap = GlobalVariableService.getGlobalVariable();
        MX_JAVA_CLASSPATH = microVariableMap.get("mserver.java.classpath");
        MX_JYTHON_MODULES_PATH = microVariableMap.get("mserver.jython.modules.path");
    }

    public void execute(String scriptPath, String[] args) {
        if( scriptPath.endsWith(".py")) {
            executeJython(scriptPath,args);
        } else if( scriptPath.endsWith(".groovy")) {
            executeGroovy(scriptPath,args);
        } else if( scriptPath.endsWith(".rb")) {
            executeJRuby(scriptPath,args);
        } else if( scriptPath.endsWith(".yml")) {
            executePlayBook(scriptPath,args);
        }
    }

    private void executePlayBook(String playbookPath, String[] args){
        try {
            PlayBook playbook = PlayBookFactory.parser(playbookPath);
            this.variableMap = playbook.getVariableMap();
            System.out.println("+---------------------------------------------------------------------+");
            System.out.println("+ "+playbook.getName());
            System.out.println("+---------------------------------------------------------------------+");
            playbook.getTaskList().forEach( task -> {
                        System.out.println("==> "+task.getName());
                        execute(task.getSrc(), args);
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeJRuby(String scriptPath, String[] args) {
        try{
            Ruby runtime;
            RubyInstanceConfig instanceConfig = new RubyInstanceConfig();
//            String jrubyHome = "/opt/mxdeploy";
//            if (!jrubyHome.isEmpty()) {
//                instanceConfig.setJRubyHome(jrubyHome);
//            }
            //instanceConfig.setArgv();

            instanceConfig.setLoader(this.getClass().getClassLoader());
            runtime = Ruby.newInstance(instanceConfig);
            runtime.evalScriptlet(getFileContents(scriptPath));
            JavaEmbedUtils.terminate(runtime);
//            List<String> loadPaths = new ArrayList<>();
//            loadPaths.add(".");
//            Ruby runtime = JavaEmbedUtils.initialize( loadPaths );
//            IRubyObject rootRubyObject = JavaEmbedUtils.newRuntimeAdapter().eval( runtime, this.getFileContents(scriptPath) );
//            JavaEmbedUtils.invokeMethod( runtime, rootRubyObject, "sessionExec", new Object[] {rootRubyObject}, null );

//            BSFManager.registerScriptingEngine("ruby", "org.jruby.javasupport.bsf.JRubyEngine", new String[]{"rb"});
//            BSFManager manager = new BSFManager();

            //--- Load a ruby file
//            manager.sessionExec("ruby", "call_java.rb", -1, -1, getFileContents(scriptPath));
        } catch (Throwable t){
            t.printStackTrace();
        }
    }

    private String getFileContents(String filename) {
        FileReader in = null;
        try {
            in = new FileReader(filename);
            return this.getStringFromReader(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getStringFromReader(Reader reader) throws IOException {
        BufferedReader bufIn = new BufferedReader(reader);
        StringWriter swOut = new StringWriter();
        PrintWriter pwOut = new PrintWriter(swOut);

        String tempLine;
        while((tempLine = bufIn.readLine()) != null) {
            pwOut.println(tempLine);
        }

        pwOut.flush();
        return swOut.toString();
    }

    private void executeGroovy(String scriptPath, String[] args){
        GroovyShell shell = new GroovyShell(ScriptGateway.class.getClassLoader());
        shell.setVariable("logger",logger);
        try {
            shell.run(new File(scriptPath), args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void executeJython(String scriptPath, String[] args){
        try {
            PythonInterpreter.initialize(System.getProperties(), System.getProperties(), new String[0]);
            PySystemState state = new PySystemState();
            PythonInterpreter interpreter = new PythonInterpreter(null, state);

            PySystemState systemState = interpreter.getSystemState();

            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    systemState.argv.append(new PyString(args[i]));
                    logger.debug("args[" + i + "]= " + args[i]);
                }
            }

            interpreter.set("logger", logger);
            interpreter.set("variableMap", variableMap);
            if (MX_JAVA_CLASSPATH != null) {
                systemState.path.append(new PyString(MX_JAVA_CLASSPATH));
            }

            if (MX_JYTHON_MODULES_PATH != null) {
                systemState.path.append(new PyString(MX_JYTHON_MODULES_PATH));
            }

            interpreter.execfile(scriptPath);
            interpreter.close();
        }catch (Throwable t){
            t.printStackTrace();
        }
    }



}
