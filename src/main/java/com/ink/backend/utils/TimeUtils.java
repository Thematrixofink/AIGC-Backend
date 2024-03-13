package com.ink.backend.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * 时间工具类
 *
 */
public class TimeUtils {

    /**
     * 得到当前时间距离0点的秒数
     * @param currentTime
     * @return
     */
    public static Integer getRemainSecondsOneDay(LocalDateTime currentTime) {
        Instant currentDate = currentTime.atZone(ZoneId.systemDefault()).toInstant();
        // 使用 plusDays 加传入的时间加1天，将时分秒设置成0
        LocalDateTime midnight = LocalDateTime.ofInstant(currentDate, ZoneId.systemDefault())
                .plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentDate, ZoneId.systemDefault());
        // 使用 ChronoUnit.SECONDS.between 方法，传入两个 LocalDateTime 对象即可得到相差的秒数
        long seconds = ChronoUnit.SECONDS.between(currentDateTime, midnight);
        return (int) seconds;
    }
}
