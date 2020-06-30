package com.nem.life.framework.boot.plugin.redis.util;

import com.nem.life.framework.boot.util.JsonUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class StringRedisTemplates extends RedisTemplate<String, String> {

    /**
     * 放入永不过期的数据
     *
     * @param key   redis key
     * @param value 放入的数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void set(String key, String value) {
        this.opsForValue().set(key, value);
    }

    /***
     * 放入指定过期时间的数据
     * @param key redis key
     * @param value 放入的数据
     * @param expired 过期时间
     * @param timeUnit 时间单位
     */
    @Transactional(rollbackFor = Exception.class)
    public void set(String key, String value, Long expired, TimeUnit timeUnit) {
        this.opsForValue().set(key, value, expired, timeUnit);
    }

    /**
     * 删除数据
     *
     * @param key redis key
     */
    @Transactional(rollbackFor = Exception.class)
    public void del(String key) {
        this.delete(key);
    }

    /**
     * 根据key获得String value
     *
     * @param key redis key
     * @return string value
     */
    @Transactional(rollbackFor = Exception.class)
    public String get(String key) {
        return this.opsForValue().get(key);
    }

    /**
     * 根据规则获取key
     *
     * @param keys 规则
     * @return key set
     */
    @Transactional(rollbackFor = Exception.class)
    public Set<String> keySet(String keys) {
        return this.keys(keys);
    }

    /**
     * 模糊匹配查出所有的数据-不建议使用
     *
     * @param keys key规则
     * @return 所查出来的key的对应的value集合
     */
    @Transactional(rollbackFor = Exception.class)
    public List<String> getListByKey(String keys) {
        return Objects.requireNonNull(this.keys(keys))
                .stream()
                .map(this::get)
                .collect(Collectors.toList());
    }

    /**
     * 模糊匹配查出所有的key和value
     *
     * @param keys key规则
     * @return 所有的key和value
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> getMapByKey(String keys) {
        return Objects.requireNonNull(this.keys(keys))
                .stream()
                .collect(Collectors.toMap(
                        e -> e,
                        this::get,
                        (a, b) -> b));
    }

    /**
     * 设置key的过期时间-秒
     *
     * @param key  key
     * @param time 过期时间
     */
    @Transactional(rollbackFor = Exception.class)
    public void setExpire(String key, Long time) {
        setExpire(key, time, TimeUnit.SECONDS);
    }

    /**
     * 设置key的过期时间
     *
     * @param key      key
     * @param time     过期时间
     * @param timeUnit 时间单位
     */
    @Transactional(rollbackFor = Exception.class)
    public void setExpire(String key, Long time, TimeUnit timeUnit) {
        this.expire(key, time, timeUnit);
    }

    /**
     * 获取key 的过期时间
     *
     * @param key key
     * @return 过期时间
     */
    @Transactional(rollbackFor = Exception.class)
    public Long getExpire(String key) {
        Long expire = this.getExpire(key, TimeUnit.SECONDS);
        if (expire == null) return 0L;
        return expire;
    }

    /**
     * 获取redis里的hashMap
     *
     * @param key redis key
     * @param <T> map key
     * @param <E> map value
     * @return hashMap
     */
    @Transactional(rollbackFor = Exception.class)
    public <T, E> Map<T, E> hashMapGet(String key) {
        return (Map<T, E>) this.opsForHash().entries(key);
    }

    /**
     * 获取某个hashMap的某一行
     *
     * @param key  redis key
     * @param item map key
     * @param <T>  返回类型
     * @return map value
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T hashMapGetItemValue(String key, String item, Class<T> tClass) {
        var o = this.opsForHash().get(key, item);

        // if (o == null) return null;
        // o = o.toString().replace("\"", "");
        return JsonUtil.jsonToPojo(o.toString(), tClass);
    }


    @Transactional(rollbackFor = Exception.class)
    public String hashMapGetItemValue(String key, String item) {
        Object o = this.opsForHash().get(key, item);
        if (o == null) return null;
        o = o.toString().replace("\"", "");
        return o.toString();
    }


    /**
     * 整个map放进redis
     *
     * @param key redis key
     * @param map map
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean hashMapSet(String key, Map<String, Object> map) {
        try {
            this.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 整个map放进redis
     *
     * @param key  redis key
     * @param map  map
     * @param time 有效分钟数
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean hashMapSet(String key, Map<String, Object> map, Long time) {
        try {
            this.opsForHash().putAll(key, map);
            if (time > 0) {
                setExpire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 往某个hashMap put值。如果不存在这个map则创建
     *
     * @param key   redis key
     * @param item  要放入的key
     * @param value 要放入的value
     * @param time  放入的分钟数
     * @return 是否放入成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean hashMapSetItem(String key, String item, Object value, long time) {
        return hashMapSetItem(key, item, value, time, TimeUnit.SECONDS);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean hashMapSetItem(String key, String item, Object value, long time, TimeUnit timeUnit) {
        try {
            this.opsForHash().put(key, item, Objects.requireNonNull(JsonUtil.objectToJson(value)));
            if (time > 0) {
                setExpire(key, time, timeUnit);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除多个item
     *
     * @param key  redis key
     * @param item map key
     */
    @Transactional(rollbackFor = Exception.class)
    public void hashMapDeleteItem(String key, Object... item) {
        this.opsForHash().delete(key, item);
    }

    /**
     * 某个hashMap是否有指定item
     *
     * @param key  redis key
     * @param item map key
     * @return 是否有
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean hashMapHasKey(String key, String item) {
        return this.opsForHash().hasKey(key, item);
    }
}
