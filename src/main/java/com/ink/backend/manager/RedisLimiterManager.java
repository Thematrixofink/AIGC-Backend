package com.ink.backend.manager;

import com.ink.backend.common.ErrorCode;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.exception.ThrowUtils;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 提供Redis Limit基础服务
 * (通用能力)，可以复用
 */
@Service
public class RedisLimiterManager {
    @Resource
    private RedissonClient redissonClient;

    /**
     * 进行限流操作
     * @param key 区分不同的限流器，比如不同的用户id需要进行不同的流量次数
     */
    public void doRateLimit(String key){
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        //todo 参数配置化
        //允许一个用户一秒进行两次运行
        rateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
        //请求令牌
        //参数为，一个请求占用多少个令牌
        boolean b = rateLimiter.tryAcquire(1);
        if(!b){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
