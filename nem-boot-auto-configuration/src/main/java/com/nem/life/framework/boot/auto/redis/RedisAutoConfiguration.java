package com.nem.life.framework.boot.auto.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "spring.redis", name = "host")
@Configuration
public class RedisAutoConfiguration {
}
