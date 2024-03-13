package com.ink.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.ink.backend.common.ErrorCode;
import com.ink.backend.constant.CommonConstant;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.exception.ThrowUtils;
import com.ink.backend.mapper.AIPersonInfoMapper;
import com.ink.backend.model.dto.aIPersonInfo.AIPersonInfoQueryRequest;
import com.ink.backend.model.entity.AIPersonInfo;
import com.ink.backend.model.entity.User;
import com.ink.backend.model.vo.AIPersonInfoVO;
import com.ink.backend.model.vo.UserVO;
import com.ink.backend.service.AIPersonInfoService;
import com.ink.backend.service.UserService;
import com.ink.backend.utils.SqlUtils;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 数字人服务实现
 *
 */
@Service
@Slf4j
public class AIPersonInfoServiceImpl extends ServiceImpl<AIPersonInfoMapper, AIPersonInfo> implements AIPersonInfoService {


    @Resource
    private UserService userService;


    @Override
    public void validAIPersonInfo(AIPersonInfo aIPersonInfo, boolean add) {
        if (aIPersonInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String aiName = aIPersonInfo.getAiName();
        String aiProfile = aIPersonInfo.getAiProfile();
        String aiVoice = aIPersonInfo.getAiVoice();
        String aiPicture = aIPersonInfo.getAiPicture();


        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(aiName,aiProfile,aiVoice), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        // todo 对AI的称谓进行校验
        if (aiName.length() > 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        // todo 对AI人的简介进行校验
        if (aiProfile.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        // todo 对AI的声音的地址合法性进行校验
        if (aiPicture.length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        // todo 校验aiPicture的地址的合法性
        if (StringUtils.isNotBlank(aiPicture) && aiPicture.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param aIPersonInfoQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<AIPersonInfo> getQueryWrapper(AIPersonInfoQueryRequest aIPersonInfoQueryRequest) {
        QueryWrapper<AIPersonInfo> queryWrapper = new QueryWrapper<>();
        if (aIPersonInfoQueryRequest == null) {
            return queryWrapper;
        }
        Long id = aIPersonInfoQueryRequest.getId();
        Long userId = aIPersonInfoQueryRequest.getUserId();
        String aiName = aIPersonInfoQueryRequest.getAiName();
        String sortField = aIPersonInfoQueryRequest.getSortField();
        String sortOrder = aIPersonInfoQueryRequest.getSortOrder();

        queryWrapper.eq(id > 0 ,"id",id);
        queryWrapper.like(StringUtils.isNotBlank(aiName),"aiName",aiName);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




