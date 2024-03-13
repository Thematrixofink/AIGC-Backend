package com.ink.backend.config;
import io.swagger.models.auth.In;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String host;

    private Integer database;

    private String password;

    private Integer port;

    /**
     * 创建
     * @return
     */
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer()
                .setDatabase(database)
                //todo 有密码要取消注释
                //todo 没密码要注释
//                .setPassword(password)
                .setAddress("redis://"+host+":"+port);
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
