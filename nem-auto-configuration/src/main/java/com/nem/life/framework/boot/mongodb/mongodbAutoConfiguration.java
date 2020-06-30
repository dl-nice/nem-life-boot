package com.nem.life.framework.boot.mongodb;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(prefix = "spring.data.mongodb", value = "uri")
public class mongodbAutoConfiguration {
}
