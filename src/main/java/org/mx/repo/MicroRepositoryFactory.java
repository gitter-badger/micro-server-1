package org.mx.repo;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import org.mx.action.MicroActionBean;
import org.mx.job.*;
import org.mx.module.MicroModule;
import org.mx.server.GlobalVariableService;
import org.mx.var.MicroVariableFactory;
import org.mx.var.MicroVariableMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fsbsilva on 12/29/16.
 */
public class MicroRepositoryFactory {

    public static void main(String[] args){
        try {
            MicroVariableMap varActionMap = MicroVariableFactory.parser("/opt/mxdeploy/micro-repository/variables.yml");
            GlobalVariableService.setActionVariableScope(varActionMap);

            MicroRepositoryBean map = MicroRepositoryFactory.parser(null,"/opt/mxdeploy/micro-repository/micro-repository.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MicroRepositoryBean parser(MicroRepositoryBean microRepositoryParam, String ymlPath) throws IOException {
        MicroRepositoryBean microRepository = MicroRepositoryFactory.loadYaml(microRepositoryParam, ymlPath, false);
        GlobalVariableService.setMicroRepository(microRepository);

        List<String> list = new ArrayList<String>();
        microRepository.getInventories().forEach( (name, inventory) -> {
            if( inventory.getSrc()!=null && inventory.isLoaded()==false) {
                list.add(inventory.getSrc());
                inventory.setLoaded(true);
            }
        });

        list.forEach( value -> {
            try {
                MicroRepositoryFactory.loadYaml(microRepository, value, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return microRepository;
    }

    private static MicroRepositoryBean loadYaml(MicroRepositoryBean microRepository, String ymlPath, boolean isLoaded) throws IOException {
        YAMLFactory factory = new YAMLFactory();
        YAMLParser parser = factory.createParser(new File(ymlPath));

        if( microRepository == null)
            microRepository = new MicroRepositoryBean();

        MicroActionBean action = null;

        MicroVariableMap variableMap = GlobalVariableService.getActionVariableScope();

        MicroModule module = null;
        MicroJobProxyBean jobProxy = null;
        MicroJobInventoryBean jobInventory = null;
        MicroJobDomainBean jobDomain = null;
        MicroJobVariableBean jobVariable = null;
        MicroJobArgumentBean jobArgument = null;
        MicroJobBean job = null;
        MicroJobThreadBean jobThread = null;

        int status = 0;
        String objectName="None" ;

        boolean hasNext = parser.nextToken() != null? true: false;
        while ( hasNext ) {
            String nextTokenName = "";
            String value = "";
            String currFieldName = parser.getCurrentName();
            String currTokenName = parser.getCurrentToken().name();

            debug("---");
            debug("currTokenName: "+currTokenName);
            debug("currFieldName: " + currFieldName);
            debug("objectName:"+objectName);
            debug("status: "+status);

            hasNext = parser.nextToken() != null? true: false;

            if( currTokenName.equals("FIELD_NAME") ) {
                nextTokenName = parser.getCurrentToken().name();
                value = parser.getValueAsString();

                debug("nextTokenName: " + nextTokenName);
                debug("getValueAsString: " + value);

                switch (status) {
                    case 0: {
                        if (currFieldName.equals("name")) {
                            microRepository.setName(value);
                        } else if (currFieldName.equals("jobs")) {
                            status = 1;
                            objectName = "jobs";
                        } else if (currFieldName.equals("thread-name")) {
                            microRepository.setThreadName(value);
                        } else if (currFieldName.equals("log-level")) {
                            microRepository.setLogLevel(value);
                        } else if (currFieldName.equals("micro-actions")) {
                            objectName = "micro-actions";
                            status=1;
                        } else if (currFieldName.equals("micro-modules")) {
                            objectName = "micro-modules";
                            status=1;
                        } else if (currFieldName.equals("inventories")) {
                            objectName = "inventories";
                            status=1;
                        } else if (currFieldName.equals("proxies")) {
                            status = 1;
                            objectName = "proxies";
                        } else if (currFieldName.equals("domains")) {
                            status = 1;
                            objectName = "domains";
                        } else if (currFieldName.equals("variables")) {
                            status = 1;
                            objectName = "variables";
                        } else if (currFieldName.equals("arguments")) {
                            status = 1;
                            objectName = "arguments";
                        } else if (currFieldName.equals("thread-groups")) {
                            status = 1;
                            objectName = "thread-groups";
                        }
                        break;
                    }
                    case 2: {
                        if (objectName.equals("jobs")) {
                            if (currFieldName.equals("name")) {
                                job.setName(value);
                                microRepository.addJob(job);
                            } else {
                                if (currFieldName.equals("src")) {
                                    value = variableMap.replace(value);
                                }
                                job.setField(currFieldName);
                                job.setValue(value);
                            }
                        } else if (objectName.equals("micro-actions")) {
                            if (currFieldName.equals("name")) {
                                action.setName(value);
                                microRepository.addAction(value, action);
                            } else if (currFieldName.equals("src")) {
                                value = variableMap.replace(value);
                                action.setSrc(value);
                            } else if (currFieldName.equals("async")) {
                                action.setAsync(Boolean.valueOf(value));
                            } else if (currFieldName.equals("cron")) {
                                action.setCron(value);
                            }
                        } else if (objectName.equals("micro-modules")) {
                            if (currFieldName.equals("name")) {
                                module.setName(value);
                                microRepository.addModule(value,module);
                            } else if (currFieldName.equals("src")) {
                                value = variableMap.replace(value);
                                module.setSrc(value);
                            }
                        } else if (objectName.equals("proxies")) {
                            if (currFieldName.equals("name")) {
                                jobProxy.setName(value);
                                microRepository.addProxy(jobProxy);
                            } else if (currFieldName.equals("host")) {
                                jobProxy.setHost(value);
                            } else if (currFieldName.equals("port")) {
                                jobProxy.setPort(Integer.valueOf(value));
                            }
                        } else if (objectName.equals("inventories")) {
                            if (currFieldName.equals("name")) {
                                jobInventory.setName(value);
                                jobInventory.setLoaded(isLoaded);
                                microRepository.addInventory(value, jobInventory);
                            } else if (currFieldName.equals("src")) {
                                value = variableMap.replace(value);
                                jobInventory.setSrc(value);
                            } else if (currFieldName.equals("array")) {
                                status=3;
                            }
                        } else if (objectName.equals("domains")) {
                            if (currFieldName.equals("name")) {
                                jobDomain.setName(value);
                                microRepository.addDomain(jobDomain);
                            } else if (currFieldName.equals("regexp")) {
                                jobDomain.setRegexp(value);
                            }
                        } else if (objectName.equals("arguments")) {
                            if (currFieldName.equals("name")) {
                                jobArgument.setName(value);
                                microRepository.addArgument(value, jobArgument);
                            } else if (currFieldName.equals("value")) {
                                jobArgument.setValue(value);
                            } else if (currFieldName.equals("encrypt")) {
                                jobArgument.setEncrypt(Boolean.valueOf(value));
                            } else if (currFieldName.equals("required")) {
                                jobArgument.setRequired(Boolean.valueOf(value));
                            }
                        } else if (objectName.equals("variables")) {
                            if (currFieldName.equals("name")) {
                                jobVariable.setName(value);
                                microRepository.addVariable(value, jobVariable);
                            } else if (currFieldName.equals("value")) {
                                jobVariable.setValue(value);
                            } else if (currFieldName.equals("array")) {
                                status=3;
                            }
                        } else if (objectName.equals("thread-groups")) {
                            if (currFieldName.equals("name")) {
                                jobThread.setName(value);
                                jobThread.setNumber(1);
                                jobThread.setLogLevel("DEBUG");
                                microRepository.addThreads(value,jobThread);
                            } else if (currFieldName.equals("number")) {
                                jobThread.setNumber(Integer.valueOf(value));
                            } else if (currFieldName.equals("hosts")) {
                                jobThread.setHosts(value);
                            } else if (currFieldName.equals("log-level")) {
                                jobThread.setLogLevel(value);
                            } else if (currFieldName.equals("username")) {
                                jobThread.setUsername(value);
                            } else if (currFieldName.equals("password")) {
                                jobThread.setPassword(value);
                            } else if (currFieldName.equals("array")) {
                                status=3;
                            }
                        }
                        break;
                    }
                }
            } else if ( status == 1 && currTokenName.equals("START_OBJECT") ){
                if( objectName.equals("inventories") ){
                    jobInventory = new MicroJobInventoryBean();
                } else if ( objectName.equals("proxies") ){
                    jobProxy = new MicroJobProxyBean();
                } else if ( objectName.equals("micro-actions") ) {
                    action = new MicroActionBean();
                } else if (objectName.equals("micro-modules") ) {
                    module = new MicroModule();
                } else if (objectName.equals("domains") ) {
                    jobDomain = new MicroJobDomainBean();
                } else if (objectName.equals("arguments") ) {
                    jobArgument = new MicroJobArgumentBean();
                } else if (objectName.equals("variables") ) {
                    jobVariable = new MicroJobVariableBean();
                } else if( objectName.equals("jobs") ){
                    job = new MicroJobBean();
                } else if ( objectName.equals("thread-groups") ){
                    jobThread = new MicroJobThreadBean();
                }
                status = 2;
            } else if ( status == 2 && currTokenName.equals("END_OBJECT") ){
                status = 1;
            } else if ( status == 1 && currTokenName.equals("END_ARRAY") ){
                objectName = "None";
                status = 0;
            } else if ( status == 3 && currTokenName.equals("END_ARRAY") ){
                status = 2;
            } else {
                if ( status == 3 ){
                    if( parser.getValueAsString() != null) {
                        if (objectName.equals("variables")) {
                            jobVariable.add(parser.getValueAsString());
                        } else if (objectName.equals("inventories")) {
                            jobInventory.add(parser.getValueAsString());
                        } else if (objectName.equals("thread-groups")) {
                            jobThread.add(parser.getValueAsString());
                        }
                    }
                }
            }
        }

        return microRepository;

    }

    private static void debug(String msg){
//        System.out.println(msg);
    }
}
