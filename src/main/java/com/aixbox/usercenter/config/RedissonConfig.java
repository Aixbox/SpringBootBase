package com.aixbox.usercenter.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置
 * @author Nick
 * @create 2023--07-15:23
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")//引用配置文件中redis的地址
@Data
public class RedissonConfig {
 
  private String host;
 
  private String port;

  private String password;
 
  @Bean
  public RedissonClient redissonClient(){
    // 1. 创建配置
    Config config = new Config();
    String redisAddress = String.format("redis://:%s@%s:%s/1", password, host, port );
    config.useSingleServer().setAddress(redisAddress).setDatabase(1);
    //2.创建Redisson实例
    RedissonClient redisson = Redisson.create(config);
    return redisson;
   }
 
 
}