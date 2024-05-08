package com.aixbox.usercenter.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author 魔王Aixbox
 * @version 1.0
 */
@SpringBootTest
public class RedisTest {


    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("Test:String", "用户");
        System.out.println(valueOperations.get("Test:String"));
    }
}
