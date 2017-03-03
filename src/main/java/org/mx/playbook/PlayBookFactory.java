package org.mx.playbook;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.jcraft.jsch.ProxySOCKS5;
import org.mx.mserver.GlobalVariableService;
import org.mx.var.MicroVariableFactory;
import org.mx.var.MicroVariableMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fsbsilva on 1/15/17.
 */
public class PlayBookFactory {

    public static void main(String[] args){
        try {
            MicroVariableMap varActionMap = MicroVariableFactory.parser("/opt/mxdeploy/micro-actions/variables.yml");
            GlobalVariableService.setActionVariableScope(varActionMap);

            PlayBookFactory.parser("/opt/mxdeploy/micro-actions/src/main/modules/hello/hello-playbook.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PlayBook parser(String ymlPath) throws IOException {
        YAMLFactory factory = new YAMLFactory();
        YAMLParser parser = factory.createParser(new File(ymlPath));

        MicroVariableMap variableMap = GlobalVariableService.getActionVariableScope();

        PlayBook playbook = new PlayBook();
        Task task = null;

        int status = 0;
        String objectName="None" ;
        List<String> valueStringList = null;

        while (parser.nextToken() != null) {
//            System.out.println(parser.getCurrentToken().name());

            String currTokenName = parser.getCurrentToken().name();
            if( currTokenName.equals("FIELD_NAME") ) {
//                System.out.println(parser.getCurrentName());
                String value = parser.nextTextValue();

                String currFieldName = parser.getCurrentName();
                switch (status) {
                    case 0: {
                        if (currFieldName.equals("name")) {
                            playbook.setName(value);
                        } else if (currFieldName.equals("vars")) {
                            status = 1;
                            objectName = "vars";
                        } else if (currFieldName.equals("tasks")) {
                            status = 1;
                            objectName = "tasks";
                        } else if (parser.getCurrentName().equals("proxy")) {
                            objectName = "proxy";
                            if (value != null) {
//                            System.out.println(value);
                                String[] proxyArray = value.split(":");
                                int port = Integer.valueOf(proxyArray[1]);
                                playbook.getProxyList().add(new ProxySOCKS5(proxyArray[0], port));
                            } else {
                                status = 1;
                            }
                        }
                        break;
                    }
                    case 2: {
                        if (objectName.equals("tasks")) {
                            if (currFieldName.equals("name")) {
                                task.setName(value);
//                            System.out.println(value);
                            } else if (currFieldName.equals("src")) {
                                value = variableMap.replace(value);
//                            System.out.println(value);
                                task.setSrc(value);
                            }
                        } else if (objectName.equals("vars")) {
//                        System.out.println(currField+": "+value);
                            if( value == null ) {
                                //Object object = parser.getCurrentValue();
                                valueStringList = new ArrayList<String>();
                                playbook.putVariable(currFieldName, valueStringList);
                            } else {
                                playbook.putVariable(currFieldName, value);
                            }
                        }
                        break;
                    }
                }

            } else if ( status == 1 && parser.getCurrentToken().name().equals("START_OBJECT") ){
                if( objectName.equals("tasks") ){
                    task = new Task();
                    playbook.addTask(task);
                }
                status = 2;
            } else if ( status == 2 && currTokenName.equals("END_OBJECT") ){
                status = 1;
            } else if ( currTokenName.equals("END_ARRAY") ){
                objectName="None";
                status = 0;
            } else if ( objectName.equals("vars") && currTokenName.equals("VALUE_STRING") ){
                String value = parser.getText();
                if( value != null ) {
//                    System.out.println(value);
                    valueStringList.add(value);
                }
            } else if ( objectName.equals("proxy") && currTokenName.equals("VALUE_STRING") ){
                String value = parser.getText();
                if( value != null ) {
//                    System.out.println(value);
                    String[] proxyArray = value.split(":");
                    int port = Integer.valueOf(proxyArray[1]);
                    playbook.getProxyList().add(new ProxySOCKS5(proxyArray[0],port));
                }
            }


        }

        return playbook;

    }

}
