package org.mx.server;

import org.apache.log4j.Logger;
import org.jruby.embed.ScriptingContainer;
import org.mx.playbook.Task;
import org.mx.playbook.PlayBookManager;
import org.mx.var.MicroVariableMap;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import java.io.*;

import groovy.lang.GroovyShell;
import org.quartz.JobExecutionException;

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


//    public void execute(MicroRepositoryBean microRepository, String scriptPath, String[] args) throws IOException {
//        Task jobWrapper = new Task(microRepository);
//        if( scriptPath.endsWith(".py")) {
//            executeJython(jobWrapper, scriptPath,args);
//        } else if( scriptPath.endsWith(".groovy")) {
//            executeGroovy(jobWrapper, scriptPath,args);
//        } else if( scriptPath.endsWith(".rb")) {
//            executeJRuby(jobWrapper, scriptPath,args);
//        } else if( scriptPath.endsWith(".yml")) {
//            executePlayBook(microRepository, scriptPath,args);
//        }
//    }

    public void execute(Task task) throws IOException, JobExecutionException {
        String scriptPath = task.getSource();
        if( scriptPath.endsWith(".py")) {
            executeJython(task);
        } else if( scriptPath.endsWith(".groovy")) {
            executeGroovy(task);
        } else if( scriptPath.endsWith(".rb")) {
            executeJRuby(task);
        } else if( scriptPath.endsWith(".yml")) {
            (new PlayBookManager(task)).execute();
        }
    }

    private void executeJRuby(Task task) throws JobExecutionException {
        try{

            ScriptingContainer container = new ScriptingContainer();
            container.setClassLoader(this.getClass().getClassLoader());
            container.setArgv(task.getArgs());
            container.setAttribute("task",task);
            container.setAttribute("data",task.getData().get("data"));
            container.runScriptlet(task.getSource());

//            Ruby runtime;
//            RubyInstanceConfig instanceConfig = new RubyInstanceConfig();
//            instanceConfig.setLoader(this.getClass().getClassLoader());
//
//            instanceConfig.setArgv(args);
//            runtime = Ruby.newInstance(instanceConfig);
//
//            runtime.evalScriptlet(getFileContents(scriptPath));
//            JavaEmbedUtils.terminate(runtime);
        } catch (Throwable t){
            t.printStackTrace();
            throw new JobExecutionException(t.getMessage());
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

    private void executeGroovy(Task task) throws JobExecutionException {
        GroovyShell shell = new GroovyShell(ScriptGateway.class.getClassLoader());
        shell.setVariable("task",task);
        shell.setVariable("data",task.getData().get("data"));
        try {
            shell.run(new File(task.getSource()), task.getArgs());
        } catch (IOException e) {
            e.printStackTrace();
            throw new JobExecutionException(e.getMessage());
        }
    }
    
    private void executeJython(Task task) throws JobExecutionException {
        try {
            PythonInterpreter.initialize(System.getProperties(), System.getProperties(), new String[0]);
            PySystemState state = new PySystemState();
            PythonInterpreter interpreter = new PythonInterpreter(null, state);
            interpreter.set("task",task);
            interpreter.set("data", task.getData().get("data"));

            PySystemState systemState = interpreter.getSystemState();

            if (task.getArgs() != null && task.getArgs().length > 0) {
                for (int i = 0; i < task.getArgs().length; i++) {
                    systemState.argv.append(new PyString(task.getArgs()[i]));
                    logger.debug("args[" + i + "]= " + task.getArgs()[i]);
                }
            }

            if (MX_JAVA_CLASSPATH != null) {
                systemState.path.append(new PyString(MX_JAVA_CLASSPATH));
            }

            if (MX_JYTHON_MODULES_PATH != null) {
                systemState.path.append(new PyString(MX_JYTHON_MODULES_PATH));
            }

            interpreter.execfile(task.getSource());
            interpreter.close();
        }catch (Throwable t){
            t.printStackTrace();
            throw new JobExecutionException(t.getMessage());
        }
    }



}
