package com.ink.backend.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ink.backend.annotation.AuthCheck;
import com.ink.backend.common.BaseResponse;
import com.ink.backend.common.DeleteRequest;
import com.ink.backend.common.ErrorCode;
import com.ink.backend.common.ResultUtils;
import com.ink.backend.constant.UserConstant;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.exception.ThrowUtils;
import com.ink.backend.model.dto.bottle.BottleAddRequest;
import com.ink.backend.model.dto.bottle.BottleQueryRequest;
import com.ink.backend.model.dto.user.UserAddRequest;
import com.ink.backend.model.entity.Bottle;
import com.ink.backend.model.entity.User;
import com.ink.backend.model.vo.BottleVO;
import com.ink.backend.service.BottleService;
import com.ink.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 漂流瓶
 */
@RestController
@RequestMapping("/drift")
@Slf4j
public class DriftController {

    @Resource
    private BottleService bottleService;
    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建漂流瓶
     *
     * @param bottleAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addBottle(@RequestBody BottleAddRequest bottleAddRequest, HttpServletRequest request) {
        if (bottleAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String content = bottleAddRequest.getContent();
        //1.校验漂流瓶内容是否合法
        if(StringUtils.isBlank(content)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"漂流瓶内容为空!");
        }
        //漂流瓶内容长度是否过长
        if(content.length() > 200){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"漂流瓶内容过长");
        }
        Bottle bottle = new Bottle();
        bottle.setContent(content);
        User loginUser = userService.getLoginUser(request);
        bottle.setUserId(loginUser.getId());
        boolean result = bottleService.save(bottle);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newPostId = bottle.getId();
        return ResultUtils.success(newPostId);
    }

    /**
     * 删除漂流瓶,用户或管理员可操作
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteBottle(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Bottle bottle = bottleService.getById(id);
        ThrowUtils.throwIf(bottle == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!bottle.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = bottleService.removeById(id);
        return ResultUtils.success(b);
    }


    /**
     * 根据 id 获取漂流瓶
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<BottleVO> getBottleVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Bottle bottle = bottleService.getById(id);
        if (bottle == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(BottleVO.objToVo(bottle));
    }


    /**
     * 分页获取当前用户创建的漂流瓶列表
     *
     * @param bottleQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<BottleVO>> listMyBottleVOByPage(@RequestBody BottleQueryRequest bottleQueryRequest,
                                                           HttpServletRequest request) {
        if (bottleQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        bottleQueryRequest.setUserId(loginUser.getId());
        long current = bottleQueryRequest.getCurrent();
        long size = bottleQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Bottle> bottlePage = bottleService.page(new Page<>(current, size),
                bottleService.getQueryWrapper(bottleQueryRequest));
        return ResultUtils.success(bottleService.getBottleVOPage(bottlePage, request));
    }


    /**
     * 打捞一个漂流瓶
     * @param request
     * @return
     */
    @GetMapping("/pick")
    public BaseResponse<BottleVO> pickBottle(HttpServletRequest request){
        //todo 检查用户是否还有打捞次数，限制用户打捞的次数
        BottleQueryRequest bottleQueryRequest = new BottleQueryRequest();
        //防止捞到自己的
        Long pickUserId = userService.getLoginUser(request).getId();
        bottleQueryRequest.setNotId(pickUserId);
        //没有被捞到的瓶子
        bottleQueryRequest.setIsPick(0);
        QueryWrapper<Bottle> queryWrapper = bottleService.getQueryWrapper(bottleQueryRequest);
        Bottle bottle = bottleService.getOne(queryWrapper,false);
        if(ObjectUtils.isEmpty(bottle)){
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR,"瓶子已经被捞完了");
        }
        BottleVO bottleVO = BottleVO.objToVo(bottle);
        //修改原来的瓶子为已被捞起状态
        bottle.setPickUserId(pickUserId);
        bottle.setIsPick(1);
        bottleService.updateById(bottle);
        return ResultUtils.success(bottleVO);
    }

    // endregion



}
