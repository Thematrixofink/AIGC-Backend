package com.ink.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ink.backend.model.dto.bottle.BottleQueryRequest;
import com.ink.backend.model.entity.AIPersonInfo;
import com.ink.backend.model.entity.Bottle;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ink.backend.model.vo.BottleVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 24957
* @description 针对表【bottle(漂流瓶表)】的数据库操作Service
* @createDate 2024-02-21 15:44:52
*/
public interface BottleService extends IService<Bottle> {
    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @return
     */
    QueryWrapper<Bottle> getQueryWrapper(BottleQueryRequest postQueryRequest);

    /**
     * 分页获取漂流瓶封装
     * @param bottlePage
     * @param request
     * @return
     */
    Page<BottleVO> getBottleVOPage(Page<Bottle> bottlePage, HttpServletRequest request);
}
