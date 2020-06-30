package com.nem.life.framework.boot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 将对象转成json字符串
     *
     * @param data javabean
     * @return json字符串
     */
    public static String objectToJson(Object data) {
        try {
            return MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将json结果集转化为对象
     *
     * @param jsonData json数据
     * @param beanType 对象中的object类型
     * @return 实体类
     */
    public static <T> T jsonToPojo(String jsonData, Class<T> beanType) {
        try {
            return MAPPER.readValue(jsonData, beanType);
        } catch (Exception e) {
            throw new RuntimeException("Json转换实体类出错!");
        }
    }

    /**
     * 将json数据转换成pojo对象list
     *
     * @param jsonData json数据
     * @param beanType 实体类型
     * @return List
     */
    public static <T> List<T> jsonToList(String jsonData, Class<T> beanType) {
        var javaType = MAPPER.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            return MAPPER.readValue(jsonData, javaType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * json转map
     *
     * @param jsonData json数据
     * @param beanType 实体类型
     * @return map
     */
    public static <K, T> Map<K, T> jsonToMap(String jsonData, Class<T> beanType) {
        var javaType = MAPPER.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            Map<K, T> map = MAPPER.readValue(jsonData, javaType);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * json转map
     */
    public static Map<String, String> jsonToMap(String json) {
        try {
            return (Map<String, String>) MAPPER.readValue(json, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @SneakyThrows
    public static Map<String, Object> objectToMap(Object o) {
        String s = objectToJson(o);
        return MAPPER.readValue(s, Map.class);
    }


    /**
     * map转pojo
     *
     * @param map
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T mapToPojo(Map map, Class<T> clazz) {
        return new ObjectMapper().convertValue(map, clazz);
    }

    public static <T> T mapToPojo(Object map, Class<T> clazz) {
        return new ObjectMapper().convertValue(map, clazz);
    }

}
