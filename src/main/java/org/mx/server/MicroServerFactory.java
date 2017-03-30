package org.mx.server;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import org.apache.log4j.PropertyConfigurator;
import org.mx.var.MicroVariableFactory;
import org.mx.var.MicroVariableMap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fsbsilva on 1/1/17.
 */
public class MicroServerFactory {

    private static Map<String, MicroServerBean> mserverMap = null;
    //private static Logger logger = Logger.getLogger(MicroServerFactory.class);

    public static void main(String[] args){
        try {
            MicroVariableMap varMap = MicroVariableFactory.parser("/opt/mxdeploy/variables.yml");
            GlobalVariableService.setMicroServerEnvironmetVariable(varMap);
            PropertyConfigurator.configure("/opt/mxdeploy/micro-server/log4j.properties");
            MicroServerFactory.parser("/opt/mxdeploy/micro-server/micro-servers.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, MicroServerBean> parser(String ymlPath) throws IOException {
        mserverMap = new HashMap<String, MicroServerBean>();

        YAMLFactory factory = new YAMLFactory();
        YAMLParser parser = factory.createParser(new File(ymlPath));
        MicroServerBean mserver = null;
        MicroVariableMap microVariableMap = GlobalVariableService.getMicroServerEnvironmetVariable();
        int status = -1;

        while (parser.nextToken() != null) {
            //logger.debug(parser.getCurrentToken().name());

            if (parser.getCurrentToken().name().equals("FIELD_NAME")) {
                //logger.debug(parser.getCurrentName());

                if (status == -1 && parser.getCurrentName().equals("micro-servers")) {
                    status = 0;
                } else if (status == 1) {
                    mserverMap.put(parser.getCurrentName(), mserver);
                    mserver.setName(parser.getCurrentName());
                    status = 2;
                } else if (status == 2) {
                    if (parser.getCurrentName().equals("host")) {
                        String value = parser.nextTextValue();
                        //logger.debug(value);
                        mserver.setHost(value);
                    } else if (parser.getCurrentName().equals("port")) {
                        int ivalue = parser.nextIntValue(0);
                        //logger.debug(ivalue);
                        mserver.setPort(ivalue);
                    } else if (parser.getCurrentName().equals("repository_path")) {
                        String value = parser.nextTextValue();
                        value = microVariableMap.replace(value);
                        //logger.debug(value);
                        mserver.setRepositoryPath(value);
                    } else if (parser.getCurrentName().equals("keystore_path")) {
                        String value = parser.nextTextValue();
                        value = microVariableMap.replace(value);
                        //logger.debug(value);
                        mserver.setKeystorePath(value);
                    } else if (parser.getCurrentName().equals("keystore_password")) {
                        String value = parser.nextTextValue();
                        //logger.debug(value);
                        mserver.setKeystorePassword(value);
                    } else if (parser.getCurrentName().equals("keystore_pkgs")) {
                        String value = parser.nextTextValue();
                        //logger.debug(value);
                        mserver.setKeystorePkgs(value);
                    } else if (parser.getCurrentName().equals("log4j_path")) {
                        String value = parser.nextTextValue();
                        value = microVariableMap.replace(value);
                        //logger.debug(value);
                        mserver.setLog4jPath(value);
                    } else if (parser.getCurrentName().equals("encrypt_key")) {
                        String value = parser.nextTextValue();
                        mserver.setEncryptKey(value);
                    }

                }
            } else if (parser.getCurrentToken().name().equals("START_OBJECT") && status == 0) {
                mserver = new MicroServerBean();
                status = 1;
            } else if (status == 2 && parser.getCurrentToken().name().equals("END_OBJECT")) {
                status = 0;
            }

        }
        return mserverMap;
    }

}
