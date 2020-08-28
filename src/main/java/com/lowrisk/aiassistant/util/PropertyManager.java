package com.lowrisk.aiassistant.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;


public class PropertyManager {

    private static Properties properties;
    private static LinkedHashMap<String ,Object> yamlPropertiesMap;
    private static final Logger logger = LogManager.getLogger(PropertyManager.class);
    static {
        properties = new Properties();
        yamlPropertiesMap = new LinkedHashMap<>();
        try {
            Yaml yaml = new Yaml();
            File folder = new File(ResourceGetter.get("main.properties").getPath()).getParentFile();
            for (File file : Objects.requireNonNull(folder.listFiles(File::isFile))) {
                String fileName = file.getName();
                if (fileName.endsWith(".properties")) {
                    FileInputStream fis = new FileInputStream(file);
                    properties.load(fis);
                } else if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
                    yamlPropertiesMap.putAll(yaml.load(new FileInputStream(file)));
                }
            }
        }catch (IOException| NullPointerException e){
            logger.warn("加载外置属性文件失败了！",e);
        }
    }
    @SuppressWarnings("rawtypes")
    public static String getProperty(String key){
        if(key==null || key.equals(""))
            return null;
        String property = properties.getProperty(key);
        if(property == null){
            String[] keys = key.split("\\.");
            Object maybeMap = yamlPropertiesMap;
            for (String skey : keys){
                if(!(maybeMap instanceof Map))
                    break;
                try { maybeMap = ((Map) maybeMap).get(skey); }catch (Exception e){
                    maybeMap = null;
                    break;
                }
            }
            property = maybeMap == null ? null : maybeMap.toString();
        }
        return property;
    }
}
