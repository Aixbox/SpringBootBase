package com.aixbox.usercenter.job;

import com.aixbox.usercenter.model.domain.User;
import com.aixbox.usercenter.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 魔王Aixbox
 * @version 1.0
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;


    private List<Long> mainUserList = Arrays.asList(12L);


    @Scheduled(cron = "0 0 12 * * ?")
    public void doCacheRecommendUser(){
        RLock lock = redissonClient.getLock("hobanpipei:precachejob:docache:lock");
        try{
            if(lock.tryLock(0, 30000L, TimeUnit.MILLISECONDS)){
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> list = userService.page(new Page<>(1, 8), queryWrapper);
                    //脱敏
                    List<User> collect = list.getRecords().stream().map((user -> {
                        return userService.getSafetyUser(user);
                    })).collect(Collectors.toList());
                    String redisKey = String.format("hobanpipei:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    valueOperations.set(redisKey, collect, 300000, TimeUnit.MILLISECONDS);
                    log.info("定时任务： 记录用户推荐");
                }
            }
        } catch (Exception e){
            log.error("redis分布式锁获取失败", e.getMessage());
        } finally {
            //判断是否为当前线程
            if(lock.isHeldByCurrentThread()){
                //释放自己的锁
                lock.unlock();
            }
        }

    }
}
