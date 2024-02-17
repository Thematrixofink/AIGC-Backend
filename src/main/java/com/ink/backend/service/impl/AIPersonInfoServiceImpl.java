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

    private final static Gson GSON = new Gson();

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

//    @Override
//    public AIPersonInfoVO getAIPersonInfoVO(AIPersonInfo aIPersonInfo, HttpServletRequest request) {
//        AIPersonInfoVO aIPersonInfoVO = AIPersonInfoVO.objToVo(aIPersonInfo);
//        long aIPersonInfoId = aIPersonInfo.getId();
//        // 1. 关联查询用户信息
//        Long userId = aIPersonInfo.getUserId();
//        User user = null;
//        if (userId != null && userId > 0) {
//            user = userService.getById(userId);
//        }
//        UserVO userVO = userService.getUserVO(user);
//        aIPersonInfoVO.setUser(userVO);
//        // 2. 已登录，获取用户点赞、收藏状态
//        User loginUser = userService.getLoginUserPermitNull(request);
//        if (loginUser != null) {
//            // 获取点赞
//            QueryWrapper<AIPersonInfoThumb> aIPersonInfoThumbQueryWrapper = new QueryWrapper<>();
//            aIPersonInfoThumbQueryWrapper.in("aIPersonInfoId", aIPersonInfoId);
//            aIPersonInfoThumbQueryWrapper.eq("userId", loginUser.getId());
//            AIPersonInfoThumb aIPersonInfoThumb = aIPersonInfoThumbMapper.selectOne(aIPersonInfoThumbQueryWrapper);
//            aIPersonInfoVO.setHasThumb(aIPersonInfoThumb != null);
//            // 获取收藏
//            QueryWrapper<AIPersonInfoFavour> aIPersonInfoFavourQueryWrapper = new QueryWrapper<>();
//            aIPersonInfoFavourQueryWrapper.in("aIPersonInfoId", aIPersonInfoId);
//            aIPersonInfoFavourQueryWrapper.eq("userId", loginUser.getId());
//            AIPersonInfoFavour aIPersonInfoFavour = aIPersonInfoFavourMapper.selectOne(aIPersonInfoFavourQueryWrapper);
//            aIPersonInfoVO.setHasFavour(aIPersonInfoFavour != null);
//        }
//        return aIPersonInfoVO;
//    }

//    @Override
//    public Page<AIPersonInfoVO> getAIPersonInfoVOPage(Page<AIPersonInfo> aIPersonInfoPage, HttpServletRequest request) {
//        List<AIPersonInfo> aIPersonInfoList = aIPersonInfoPage.getRecords();
//        Page<AIPersonInfoVO> aIPersonInfoVOPage = new Page<>(aIPersonInfoPage.getCurrent(), aIPersonInfoPage.getSize(), aIPersonInfoPage.getTotal());
//        if (CollectionUtils.isEmpty(aIPersonInfoList)) {
//            return aIPersonInfoVOPage;
//        }
//        // 1. 关联查询用户信息
//        Set<Long> userIdSet = aIPersonInfoList.stream().map(AIPersonInfo::getUserId).collect(Collectors.toSet());
//        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
//                .collect(Collectors.groupingBy(User::getId));
//        // 2. 已登录，获取用户点赞、收藏状态
//        Map<Long, Boolean> aIPersonInfoIdHasThumbMap = new HashMap<>();
//        Map<Long, Boolean> aIPersonInfoIdHasFavourMap = new HashMap<>();
//        User loginUser = userService.getLoginUserPermitNull(request);
//        if (loginUser != null) {
//            Set<Long> aIPersonInfoIdSet = aIPersonInfoList.stream().map(AIPersonInfo::getId).collect(Collectors.toSet());
//            loginUser = userService.getLoginUser(request);
//            // 获取点赞
//            QueryWrapper<AIPersonInfoThumb> aIPersonInfoThumbQueryWrapper = new QueryWrapper<>();
//            aIPersonInfoThumbQueryWrapper.in("aIPersonInfoId", aIPersonInfoIdSet);
//            aIPersonInfoThumbQueryWrapper.eq("userId", loginUser.getId());
//            List<AIPersonInfoThumb> aIPersonInfoAIPersonInfoThumbList = aIPersonInfoThumbMapper.selectList(aIPersonInfoThumbQueryWrapper);
//            aIPersonInfoAIPersonInfoThumbList.forEach(aIPersonInfoAIPersonInfoThumb -> aIPersonInfoIdHasThumbMap.put(aIPersonInfoAIPersonInfoThumb.getAIPersonInfoId(), true));
//            // 获取收藏
//            QueryWrapper<AIPersonInfoFavour> aIPersonInfoFavourQueryWrapper = new QueryWrapper<>();
//            aIPersonInfoFavourQueryWrapper.in("aIPersonInfoId", aIPersonInfoIdSet);
//            aIPersonInfoFavourQueryWrapper.eq("userId", loginUser.getId());
//            List<AIPersonInfoFavour> aIPersonInfoFavourList = aIPersonInfoFavourMapper.selectList(aIPersonInfoFavourQueryWrapper);
//            aIPersonInfoFavourList.forEach(aIPersonInfoFavour -> aIPersonInfoIdHasFavourMap.put(aIPersonInfoFavour.getAIPersonInfoId(), true));
//        }
//        // 填充信息
//        List<AIPersonInfoVO> aIPersonInfoVOList = aIPersonInfoList.stream().map(aIPersonInfo -> {
//            AIPersonInfoVO aIPersonInfoVO = AIPersonInfoVO.objToVo(aIPersonInfo);
//            Long userId = aIPersonInfo.getUserId();
//            User user = null;
//            if (userIdUserListMap.containsKey(userId)) {
//                user = userIdUserListMap.get(userId).get(0);
//            }
//            aIPersonInfoVO.setUser(userService.getUserVO(user));
//            aIPersonInfoVO.setHasThumb(aIPersonInfoIdHasThumbMap.getOrDefault(aIPersonInfo.getId(), false));
//            aIPersonInfoVO.setHasFavour(aIPersonInfoIdHasFavourMap.getOrDefault(aIPersonInfo.getId(), false));
//            return aIPersonInfoVO;
//        }).collect(Collectors.toList());
//        aIPersonInfoVOPage.setRecords(aIPersonInfoVOList);
//        return aIPersonInfoVOPage;
//    }

}




