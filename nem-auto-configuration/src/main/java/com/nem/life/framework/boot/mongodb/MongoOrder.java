package com.nem.life.framework.boot.mongodb;

import java.lang.annotation.*;


@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MongoOrder {
}
