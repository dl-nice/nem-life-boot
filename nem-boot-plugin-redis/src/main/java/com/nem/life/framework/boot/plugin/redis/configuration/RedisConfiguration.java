package com.nem.life.framework.boot.plugin.redis.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nem.life.framework.boot.plugin.redis.util.RedisTemplates;
import com.nem.life.framework.boot.plugin.redis.util.StringRedisTemplates;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Resource;

@RequiredArgsConstructor
@ConditionalOnClass({LettuceConnectionFactory.class})
@Configuration
public class RedisConfiguration extends CachingConfigurerSupport {
    @Resource
    private LettuceConnectionFactory lettuceConnectionFactory;

    @ConditionalOnClass(LettuceConnectionFactory.class)
    @Bean
    public RedisTemplates<Object, Object> redisTemplates() {
        RedisTemplates<Object, Object> redisTemplate = new RedisTemplates<>();
        initRedisTemplate(redisTemplate, lettuceConnectionFactory);
        return redisTemplate;
    }

    @ConditionalOnClass(LettuceConnectionFactory.class)
    @Bean
    public StringRedisTemplates strRedisTemplates() {
        StringRedisTemplates redisTemplate = new StringRedisTemplates();
        initRedisTemplate(redisTemplate, lettuceConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        initRedisTemplate(redisTemplate, lettuceConnectionFactory);
        return redisTemplate;
    }

    private void initRedisTemplate(RedisTemplate redisTemplate, LettuceConnectionFactory lettuceConnectionFactory) {

        // 使用Jackson2JsonRedisSerialize 替换默认序列化
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(
                Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        // 设置value的序列化规则和 key的序列化规则
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        // hash参数序列化方式
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        // 缓存支持回滚(事务管理)
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        redisTemplate.afterPropertiesSet();
    }
}
