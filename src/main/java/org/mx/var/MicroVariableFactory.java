package org.mx.var;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

import java.io.File;
import java.io.IOException;

/**
 * Created by fsbsilva on 1/1/17.
 */
public class MicroVariableFactory {

    public static void main(String[] args){
        try {
            MicroVariableFactory.parser("/opt/mxdeploy/variables.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MicroVariableMap parser(String ymlPath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        YAMLFactory factory = new YAMLFactory();
        YAMLParser parser = factory.createParser(new File(ymlPath));
        MicroVariableMap microVariableMap = new MicroVariableMap();
        int status = -1;

        while (parser.nextToken() != null) {
            //System.out.println(parser.getCurrentToken().name());

            if( parser.getCurrentToken().name().equals("FIELD_NAME") ) {
                //System.out.println(parser.getCurrentName());
                String value = parser.nextTextValue();
                if( value!=null && microVariableMap.keySet().size() > 0 )
                    value = microVariableMap.replace(value);
                //System.out.println(value);
                microVariableMap.put(parser.getCurrentName(), value);
            }

        }
        return microVariableMap;
    }

}
