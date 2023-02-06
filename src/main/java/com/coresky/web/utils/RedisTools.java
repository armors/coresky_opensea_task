package com.coresky.web.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
/**
 * Redis工具类
 */
@Component
public class RedisTools {

    @Resource
    public StringRedisTemplate template;
}