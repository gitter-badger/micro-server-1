package org.mx.hadoop;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import org.mx.server.GlobalVariableService;
import org.mx.var.MicroVariableFactory;
import org.mx.var.MicroVariableMap;

import java.io.File;
import java.io.IOException;

/**
 * Created by fsbsilva on 1/16/17.
 */
public class HadoopFactory {

    public static void main(String[] args){
        try {
            MicroVariableMap varActionMap = MicroVariableFactory.parser("/opt/mxdeploy/micro-actions/variables.yml");
            GlobalVariableService.setRepositoryEnvironmentVariable(varActionMap);

            HadoopFactory.parser("/opt/mxdeploy/micro-actions/hadoop.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HadoopBean parser(String ymlPath) throws IOException {
        YAMLFactory factory = new YAMLFactory();
        YAMLParser parser = factory.createParser(new File(ymlPath));

        MicroVariableMap variableMap = GlobalVariableService.getRepositoryEnvironmentVariable();

        HadoopBean hadoop = new HadoopBean();
        int status = 0;

        while (parser.nextToken() != null) {
            System.out.println(parser.getCurrentToken().name());

            if (parser.getCurrentToken().name().equals("FIELD_NAME")) {
                System.out.println(parser.getCurrentName());

                String currField = parser.getCurrentName();
                if (currField.equals("hadoop")) {
                    status = 1;
                }
                if (status == 1) {
                    if (currField.equals("host")) {
                        String value = parser.nextTextValue();
                        System.out.println(value);
                        hadoop.setHost(value);
                    } else if (currField.equals("port")) {
                        int ivalue = parser.nextIntValue(0);
                        System.out.println(ivalue);
                        hadoop.setPort(ivalue);
                    } else if (currField.equals("repository")) {
                        String value = parser.nextTextValue();
                        System.out.println(value);
                        value = variableMap.replace(value);
                        System.out.println(value);
                        hadoop.setRepository(value);
                    }
                }


            }



        }
        return hadoop;
    }

}
