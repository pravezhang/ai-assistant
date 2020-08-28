package com.lowrisk.aiassistant.backend;

import com.lowrisk.aiassistant.front.ConversationView;
import com.lowrisk.aiassistant.front.MeetingWebView;

import java.lang.annotation.*;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * 如果检测到是本地调用，如清屏、退出会议等，就不上传到management去了。
 */
public class LocalFunctionInterceptor {

    @KeyWord("清屏")
    public static void clearScreen(){
        ConversationView.getInstance().clearListView();
    }

    @KeyWord({"结束会议","停止开会","散会","关闭会议"})
    public static void stopMeeting(){
        MeetingWebView.getInstance().unload();
    }




    private static HashMap<String , Method> methodMap;
    static {
        methodMap = new HashMap<>();
        Method[] methods = LocalFunctionInterceptor.class.getMethods();
        for (Method method : methods){
            KeyWord annotation = method.getAnnotation(KeyWord.class);
            if(annotation != null){
                for (String key : annotation.value())
                    methodMap.put(key,method);
            }
        }
    }
    /**
     *
     * @param query 关键词
     * @return  true=拦截， false=放行
     */
    public static boolean intercept(String query){
        for (String key : methodMap.keySet())
            if(query.contains(key)) {
                try {
                    Method method = methodMap.get(key);
                    if(method.getParameterCount()==0)
                        method.invoke(null);
                    else if(method.getParameterCount()==1)
                        method.invoke(null,query);
                    else throw new WrongMethodTypeException("该方法参数个数有误！");
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("调用拦截方法失败。");
                }
                return true;
            }
        return false;
    }
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface KeyWord{
        String[] value() default {};
    }
}
