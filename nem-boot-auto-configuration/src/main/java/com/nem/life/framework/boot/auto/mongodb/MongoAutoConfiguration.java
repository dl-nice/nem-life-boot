package com.nem.life.framework.boot.auto.mongodb;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

@ConditionalOnProperty(prefix = "spring.data.mongodb", value = "uri")
@ComponentScan("com.nem.life.framework.boot.plugin.mongodb")
public class MongoAutoConfiguration {
}
