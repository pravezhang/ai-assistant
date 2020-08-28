package com.lowrisk.aiassistant.util;

import com.lowrisk.aiassistant.global.VariablesHolder;

import java.net.URL;
import java.util.Objects;

public class ResourceGetter {

    public static URL get(String filePath){
        return Objects.requireNonNull(ResourceGetter.class.getClassLoader().getResource(filePath));
    }

    public static URL getImage(String image){
        return get("image/"+image);
    }

    public static URL getCss(String css){
        return get("css/"+css);
    }

}

