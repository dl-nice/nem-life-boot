package com.nem.life.framework.boot.mongodb;

import java.lang.annotation.*;

/**
 * @Author ：南有乔木
 * @Email ：1558146696@qq.com
 * @date ：Created in 2019/07/16 下午 05:46
 * @description：
 * @version: ：芒果专用注解
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MongoChild {
}
