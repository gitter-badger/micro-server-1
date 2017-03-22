package org.mx.client;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import org.mx.server.GlobalVariableService;
import org.mx.var.MicroVariableMap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fsbsilva on 1/3/17.
 */
public class MicroClientYmlFactory {

    private static Map<String, MicroClientYml> mserverClientMap = null;

    public static void main(String[] args){
        try {
            MicroClientYmlFactory.parser("/opt/mxdeploy/micro-server/micro-clients.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, MicroClientYml> parser(String ymlPath) throws IOException {
        mserverClientMap = new HashMap<String, MicroClientYml>();

        YAMLFactory factory = new YAMLFactory();
        YAMLParser parser = factory.createParser(new File(ymlPath));
        MicroClientYml mclient = null;

        MicroVariableMap microVariableMap = GlobalVariableService.getGlobalVariable();
        int status = -1;

        while (parser.nextToken() != null) {
            //System.out.println(parser.getCurrentToken().name());

            if( parser.getCurrentToken().name().equals("FIELD_NAME") ) {
                //System.out.println(parser.getCurrentName());

                if (status == 2) {
                    if (parser.getCurrentName().equals("host")) {
                        String value = parser.nextTextValue();
                        //System.out.println(value);
                        mclient.setHost(value);
                    } else if (parser.getCurrentName().equals("port")) {
                        int ivalue = parser.nextIntValue(0);
                        //System.out.println(ivalue);
                        mclient.setPort(ivalue);
                    } else if (parser.getCurrentName().equals("truststore_path")) {
                        String value = parser.nextTextValue();
                        value = microVariableMap.replace(value);
                        //System.out.println(value);
                        mclient.setTruststorePath(value);
                    } else if (parser.getCurrentName().equals("log4j_path")) {
                        String value = parser.nextTextValue();
                        value = microVariableMap.replace(value);
                        //System.out.println(value);
                        mclient.setLog4jPath(value);
                    }
                } else if (status == -1 && parser.getCurrentName().equals("micro-clients")) {
                    status = 0;
                } else if (status == 1) {
                    mserverClientMap.put(parser.getCurrentName(), mclient);
                    mclient.setName(parser.getCurrentName());
                    status = 2;
                }
            } else if( parser.getCurrentToken().name().equals("START_OBJECT") &&  status == 0 ) {
                mclient = new MicroClientYml();
                status = 1;
            } else if (status == 2 && parser.getCurrentToken().name().equals("END_OBJECT")) {
                status = 0;
            }

        }
        return mserverClientMap;
    }

}
