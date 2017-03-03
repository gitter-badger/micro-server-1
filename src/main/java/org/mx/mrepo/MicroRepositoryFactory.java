package org.mx.mrepo;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.jcraft.jsch.ProxySOCKS5;
import org.mx.maction.MicroAction;
import org.mx.mmodule.MicroModule;
import org.mx.mserver.GlobalVariableService;
import org.mx.var.MicroVariableFactory;
import org.mx.var.MicroVariableMap;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by fsbsilva on 12/29/16.
 */
public class MicroRepositoryFactory {

    public static void main(String[] args){
        try {
            MicroVariableMap varActionMap = MicroVariableFactory.parser("/opt/mxdeploy/micro-repository/variables.yml");
            GlobalVariableService.setActionVariableScope(varActionMap);

            MicroRepository map = MicroRepositoryFactory.parser("/opt/mxdeploy/micro-repository/micro-repository.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static MicroRepository parser(String ymlPath) throws IOException {
        YAMLFactory factory = new YAMLFactory();
        YAMLParser parser = factory.createParser(new File(ymlPath));

        MicroAction action = null;
        MicroRepository microRepository = new MicroRepository();
        MicroVariableMap variableMap = GlobalVariableService.getActionVariableScope();

        MicroModule module = null;

        int status = 0;
        String objectName="None" ;
        List<String> valueStringList = null;

        while (parser.nextToken() != null) {
            String currTokenName = parser.getCurrentToken().name();
//            System.out.println(parser.getCurrentToken().name());

            if( currTokenName.equals("FIELD_NAME") ) {
//                System.out.println(parser.getCurrentName());
                String value = parser.nextTextValue();
//                System.out.println(value);
                String currField = parser.getCurrentName();

                switch (status) {
                    case 0: {
                        if (currField.equals("micro-actions")) {
                            objectName = "micro-actions";
                            status=1;
                        } else if (currField.equals("micro-modules")) {
                            objectName = "micro-modules";
                            status=1;
                        } else if (currField.equals("inventory")) {
                            value = parser.getText();
                            value = variableMap.replace(value);
                            microRepository.setInventoryPath(value);
                        } else if (parser.getCurrentName().equals("proxy")) {
                            objectName="proxy";
                            if( value != null ){
//                                System.out.println(value);
                                String[] proxyArray = value.split(":");
                                int port = Integer.valueOf(proxyArray[1]);
                                microRepository.getProxyList().add(new ProxySOCKS5(proxyArray[0],port));
                            } else {
                                status = 1;
                            }
                        }

                        break;
                    }
                    case 2: {
                        if (objectName.equals("micro-actions")) {
                            action = new MicroAction();
                            microRepository.getMicroActionMap().put(currField, action);
                            action.setName(currField);
                            status = 3;
                        } else if (objectName.equals("micro-modules")) {
                            module = new MicroModule();
                            microRepository.getMicroModuleMap().put(currField,module);
                            module.setName(currField);
                            status = 3;
                        }
                        break;
                    }
                    case 3: {
                        if (objectName.equals("micro-actions")) {
                            if (currField.equals("src")) {
                                value = variableMap.replace(value);
                                action.setSrc(value);
                            } else if (currField.equals("async")) {
                                action.setAsync(Boolean.valueOf(value));
                            } else if (currField.equals("cron")) {
                                action.setCron(value);
                            }
                        } else if (objectName.equals("micro-modules")) {
                            if (currField.equals("src")) {
                                value = variableMap.replace(value);
                                module.setSrc(value);
                            }
                        }
                        break;
                    }
                }
            } else if ( status == 1 && currTokenName.equals("START_OBJECT") ){
                status = 2;
            } else if ( status == 3 && currTokenName.equals("END_OBJECT") ){
                status = 2;
            } else if ( status == 2 && currTokenName.equals("END_ARRAY") ){
                status = 0;
            } else if ( objectName.equals("proxy") && currTokenName.equals("VALUE_STRING") ){
                String value = parser.getText();
                if( value != null ) {
//                    System.out.println(value);
                    String[] proxyArray = value.split(":");
                    int port = Integer.valueOf(proxyArray[1]);
                    microRepository.getProxyList().add(new ProxySOCKS5(proxyArray[0],port));
                }
            }


        }

        return microRepository;

    }
}
