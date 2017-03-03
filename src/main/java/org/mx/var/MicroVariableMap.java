package org.mx.var;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by fsbsilva on 12/31/16.
 */
public class MicroVariableMap {

    private Map<String, String> varStrMap = new HashMap<String, String>();
    private Map<String, Map> varMapMap = new HashMap<String, Map>();

    public String get(String key){
        return varStrMap.get(key);
    }

    public void put(String key, String value){
        varStrMap.put(key, value);
    }

    public Set<String> keySet(){
        return varStrMap.keySet();
    }

    public void putMap(String key, Map valeuMap){
        varMapMap.put(key, valeuMap);
    }

    public Map<String, String> getMap(String key){
        return varMapMap.get(key);
    }

    public String replace(String value){
        Set keySet = this.keySet();
        Iterator<String> iterator = keySet.iterator();
        while( iterator.hasNext() ){
            String key = iterator.next();
            if( value.contains("${"+key+"}") ){
                String replacement = this.get(key);
                value = value.replace("${"+key+"}",replacement);
                break;
            }
        }
        return value;
    }

    public void clear(){
        varStrMap = new HashMap<String, String>();
        varMapMap = new HashMap<String, Map>();
    }

}
