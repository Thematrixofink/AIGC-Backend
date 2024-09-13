package com.ink.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ink.backend.model.dto.aIPersonInfo.AIPersonInfoQueryRequest;
import com.ink.backend.model.entity.AIPersonInfo;
import com.ink.backend.model.vo.AIPersonInfoVO;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子服务
 *
 */
public interface AIPersonInfoService extends IService<AIPersonInfo> {

    /**
     * 校验
     *
     * @param aIPersonInfo
     * @param add
     */
    void validAIPersonInfo(AIPersonInfo aIPersonInfo, boolean add);

    /**
     * 获取查询条件
     *
     * @param aIPersonInfoQueryRequest
     * @return
     */
    QueryWrapper<AIPersonInfo> getQueryWrapper(AIPersonInfoQueryRequest aIPersonInfoQueryRequest);

}
