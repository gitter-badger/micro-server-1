package org.mx.mmodule;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.jcraft.jsch.ProxySOCKS5;
import org.mx.maction.MicroAction;
import org.mx.mserver.GlobalVariableService;
import org.mx.var.MicroVariableFactory;
import org.mx.var.MicroVariableMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fsbsilva on 12/29/16.
 */
public class MicroModuleFactory {

    public static void main(String[] args){
        try {
            MicroVariableMap varActionMap = MicroVariableFactory.parser("/opt/mxdeploy/micro-actions/variables.yml");
            GlobalVariableService.setActionVariableScope(varActionMap);

            MicroModuleFactory.parser("/opt/mxdeploy/micro-actions/src/main/modules/resolv-conf/module.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MicroModule parser(String ymlPath) throws IOException {
        YAMLFactory factory = new YAMLFactory();
        YAMLParser parser = factory.createParser(new File(ymlPath));

        MicroModule module = new MicroModule();
        MicroAction action = null;

        MicroVariableMap variableMap = GlobalVariableService.getActionVariableScope();

        int status = 0;
        String objectName="None" ;
        List<String> valueStringList = null;

        while (parser.nextToken() != null) {
            String currTokenName = parser.getCurrentToken().name();
//            System.out.println(currTokenName);

            if( parser.getCurrentToken().name().equals("FIELD_NAME") ) {
                String value = parser.nextTextValue();
                String currFieldName = parser.getCurrentName();
//                System.out.println(currFieldName);
//                System.out.println(value);

                switch (status) {
                    case 0: {
                        if (currFieldName.equals("micro-actions")) {
                            status = 1;
                            objectName="micro-actions";
                        } else if (currFieldName.equals("proxy")) {
                            objectName="proxy";
                            if( value != null ){
//                                System.out.println(value);
                                String[] proxyArray = value.split(":");
                                int port = Integer.valueOf(proxyArray[1]);
                                module.getProxyList().add(new ProxySOCKS5(proxyArray[0],port));
                            } else {
                                status = 1;
                            }
                        } else if (currFieldName.equals("default")) {
                            module.setDefaultMicroAction(value);
                        }
                        break;
                    }
                    case 2: {
                        if( objectName.equals("micro-actions")) {
                            action = new MicroAction();
                            action.setName(currFieldName);

                            module.getMicroActionMap().put(currFieldName, action);
                            status = 3;
                        }

                        break;
                    }
                    case 3: {
                        if (currFieldName.equals("src")) {
                            value = variableMap.replace(value);
                            action.setSrc(value);
                        } else if (currFieldName.equals("async")) {
                            action.setAsync(Boolean.valueOf(value));
                        }
                    }
                }

            } else if ( status == 1 && currTokenName.equals("START_OBJECT") ){
                status = 2;
            } else if ( status == 3 && currTokenName.equals("END_OBJECT") ){
                status = 2;
            } else if ( currTokenName.equals("END_ARRAY") ){
                status = 0;
                objectName = "None";
            } else if ( objectName.equals("proxy") && currTokenName.equals("VALUE_STRING") ){
                String value = parser.getText();
                if( value != null ) {
//                    System.out.println(value);
                    String[] proxyArray = value.split(":");
                    int port = Integer.valueOf(proxyArray[1]);
                    module.getProxyList().add(new ProxySOCKS5(proxyArray[0],port));
                }
            }


        }

        return module;

    }
}
