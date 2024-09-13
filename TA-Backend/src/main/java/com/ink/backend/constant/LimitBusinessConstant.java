package com.ink.backend.constant;

/**
 * 限制用户使用的业务常量
 */
public interface LimitBusinessConstant {

    String DRIFT_ADD_PREFIX = "drift:addTimes:";

    Integer DRIFT_ADD_TIMES = 5;

    String DRIFT_PICK_PREFIX = "drift:pickTimes:";

    Integer DRIFT_PICK_TIMES = 3;
}
