package com.aixbox.usercenter.service;

import com.aixbox.usercenter.config.RedissonConfig;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author 魔王Aixbox
 * @version 1.0
 */
@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void test(){
        RList<Object> list = redissonClient.getList("test:list");
        list.add("rlist");
        System.out.println(list.get(0));
    }


}
