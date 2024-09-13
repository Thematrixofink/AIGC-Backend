package com.ink.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ink.backend.constant.CommonConstant;
import com.ink.backend.model.dto.bottle.BottleQueryRequest;
import com.ink.backend.model.entity.Bottle;
import com.ink.backend.model.entity.User;
import com.ink.backend.model.vo.BottleVO;
import com.ink.backend.model.vo.UserVO;
import com.ink.backend.service.BottleService;
import com.ink.backend.mapper.BottleMapper;
import com.ink.backend.service.UserService;
import com.ink.backend.utils.SqlUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 24957
 * @description 针对表【bottle(漂流瓶表)】的数据库操作Service实现
 * @createDate 2024-02-21 15:44:52
 */
@Service
public class BottleServiceImpl extends ServiceImpl<BottleMapper, Bottle>
        implements BottleService {

    @Resource
    private UserService userService;

    @Override
    public QueryWrapper<Bottle> getQueryWrapper(BottleQueryRequest bottleQueryRequest) {
        QueryWrapper<Bottle> queryWrapper = new QueryWrapper<>();
        if (bottleQueryRequest == null) {
            return queryWrapper;
        }
        Long id = bottleQueryRequest.getId();
        Long userId = bottleQueryRequest.getUserId();
        Long pickUserId = bottleQueryRequest.getPickUserId();
        Long notId = bottleQueryRequest.getNotId();
        Integer isPick = bottleQueryRequest.getIsPick();
        String sortField = bottleQueryRequest.getSortField();
        String sortOrder = bottleQueryRequest.getSortOrder();


        // 拼接查询条件
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "userId", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(pickUserId), "pickUserId", pickUserId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(isPick), "isPick", isPick);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<BottleVO> getBottleVOPage(Page<Bottle> bottlePage, HttpServletRequest request) {

        List<Bottle> bottleList = bottlePage.getRecords();
        Page<BottleVO> bottleVOPage = new Page<>(bottlePage.getCurrent(), bottlePage.getSize(), bottlePage.getTotal());
        if (CollectionUtils.isEmpty(bottleList)) {
            return bottleVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = bottleList.stream().map(Bottle::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        List<BottleVO> bottleVOList = bottleList.stream().map(bottle -> {
            BottleVO bottleVO = BottleVO.objToVo(bottle);
            Long userId = bottle.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            //
            return bottleVO;
        }).collect(Collectors.toList());
        bottleVOPage.setRecords(bottleVOList);
        return bottleVOPage;
    }
}




