package com.nem.life.framework.boot.plugin.redis.listener;

import com.nem.life.framework.boot.plugin.redis.configuration.RedisConfiguration;
import com.nem.life.framework.boot.plugin.redis.util.RedisTemplates;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.ArrayList;
import java.util.List;

@ConditionalOnClass({RedisTemplates.class})
@RequiredArgsConstructor
@Slf4j
@Configuration
@Import({RedisConfiguration.class})
public class RedisMessageListener {
    public static List<RedisMonitor> redisMonitorList = new ArrayList<>();

    private final RedisProperties redisProperties;


    /**
     * 添加默认监控 指定默认数据库
     *
     * @param messageListener
     * @return
     */
    public boolean addDefaultRedisMonitor(MessageListener messageListener) {
        int database = redisProperties.getDatabase();
        com.nem.life.framework.boot.plugin.redis.listener.RedisMessageListener.redisMonitorList
                .add(new  com.nem.life.framework.boot.plugin.redis.listener.RedisMessageListener.RedisMonitor(messageListener, "__keyevent@" + database + "__:expired"));
        return true;
    }

    /**
     * 添加监控，指定数据库编号
     *
     * @param messageListener
     * @param dbIndex
     * @return
     */
    public boolean addDefaultRedisMonitor(MessageListener messageListener, int dbIndex) {
        if (dbIndex > 16) return false;
        if (dbIndex < 0) return false;
        com.nem.life.framework.boot.plugin.redis.listener.RedisMessageListener
                .redisMonitorList.add(new com.nem.life.framework.boot.plugin.redis.listener
                .RedisMessageListener.RedisMonitor(messageListener, "__keyevent@" + dbIndex + "__:expired"));
        return true;
    }

    /**
     * 为所有数据库添加监控
     *
     * @param messageListener
     * @return
     */
    public boolean addAllDatabaseRedisMonitor(MessageListener messageListener) {
        com.nem.life.framework.boot.plugin.redis.listener
                .RedisMessageListener.redisMonitorList
                .add(new com.nem.life.framework.boot.plugin.redis.listener
                        .RedisMessageListener.RedisMonitor(messageListener, "__keyevent@*__:expired"));
        return true;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RedisMonitor {
        private MessageListener messageListener;
        private String patten;
    }

    @SneakyThrows
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        Thread.sleep(5000);
        for (RedisMonitor redisMonitor : redisMonitorList) {
            log.info("Add a redisMonitor By:" + redisMonitor.getPatten());
            container.addMessageListener(redisMonitor.getMessageListener(), new ChannelTopic(redisMonitor.getPatten()));
        }

        return container;
    }
}
