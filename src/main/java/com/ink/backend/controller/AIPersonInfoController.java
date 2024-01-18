package com.ink.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.ink.backend.annotation.AuthCheck;
import com.ink.backend.common.BaseResponse;
import com.ink.backend.common.DeleteRequest;
import com.ink.backend.common.ErrorCode;
import com.ink.backend.common.ResultUtils;
import com.ink.backend.constant.UserConstant;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.exception.ThrowUtils;
import com.ink.backend.model.dto.aIPersonInfo.AIPersonInfoAddRequest;
import com.ink.backend.model.dto.aIPersonInfo.AIPersonInfoEditRequest;
import com.ink.backend.model.dto.aIPersonInfo.AIPersonInfoQueryRequest;
import com.ink.backend.model.dto.aIPersonInfo.AIPersonInfoUpdateRequest;
import com.ink.backend.model.entity.AIPersonInfo;
import com.ink.backend.model.entity.User;
import com.ink.backend.service.AIPersonInfoService;
import com.ink.backend.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI数字人信息接口
 *
 */
@RestController
@RequestMapping("/aiPerson")
@Slf4j
public class AIPersonInfoController {

    @Resource
    private AIPersonInfoService aIPersonInfoService;

    @Resource
    private UserService userService;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建AI数字人
     *
     * @param AIPersonInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addAIPersonInfo(@RequestBody AIPersonInfoAddRequest AIPersonInfoAddRequest, HttpServletRequest request) {
        if (AIPersonInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AIPersonInfo aIPersonInfo = new AIPersonInfo();
        BeanUtils.copyProperties(AIPersonInfoAddRequest, aIPersonInfo);
        aIPersonInfoService.validAIPersonInfo(aIPersonInfo, true);
        User loginUser = userService.getLoginUser(request);
        aIPersonInfo.setUserId(loginUser.getId());
        boolean result = aIPersonInfoService.save(aIPersonInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newAIPersonInfoId = aIPersonInfo.getId();
        return ResultUtils.success(newAIPersonInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteAIPersonInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        AIPersonInfo oldAIPersonInfo = aIPersonInfoService.getById(id);
        ThrowUtils.throwIf(oldAIPersonInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldAIPersonInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = aIPersonInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param AIPersonInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAIPersonInfo(@RequestBody AIPersonInfoUpdateRequest AIPersonInfoUpdateRequest) {
        if (AIPersonInfoUpdateRequest == null || AIPersonInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AIPersonInfo aIPersonInfo = new AIPersonInfo();
        BeanUtils.copyProperties(AIPersonInfoUpdateRequest, aIPersonInfo);
        // 参数校验
        aIPersonInfoService.validAIPersonInfo(aIPersonInfo, false);
        long id = AIPersonInfoUpdateRequest.getId();
        // 判断是否存在
        AIPersonInfo oldAIPersonInfo = aIPersonInfoService.getById(id);
        ThrowUtils.throwIf(oldAIPersonInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = aIPersonInfoService.updateById(aIPersonInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<AIPersonInfo> getAIPersonInfoVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AIPersonInfo aIPersonInfo = aIPersonInfoService.getById(id);
        if (aIPersonInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(aIPersonInfoService.getById(id));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param AIPersonInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<AIPersonInfo>> listAIPersonInfoVOByPage(@RequestBody AIPersonInfoQueryRequest AIPersonInfoQueryRequest,
                                                               HttpServletRequest request) {
        long current = AIPersonInfoQueryRequest.getCurrent();
        long size = AIPersonInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AIPersonInfo> aIPersonInfoPage = aIPersonInfoService.page(new Page<>(current, size),
                aIPersonInfoService.getQueryWrapper(AIPersonInfoQueryRequest));
        return ResultUtils.success(aIPersonInfoPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param AIPersonInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<AIPersonInfo>> listMyAIPersonInfoVOByPage(@RequestBody AIPersonInfoQueryRequest AIPersonInfoQueryRequest,
                                                                 HttpServletRequest request) {
        if (AIPersonInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        AIPersonInfoQueryRequest.setUserId(loginUser.getId());
        long current = AIPersonInfoQueryRequest.getCurrent();
        long size = AIPersonInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AIPersonInfo> aIPersonInfoPage = aIPersonInfoService.page(new Page<>(current, size),
                aIPersonInfoService.getQueryWrapper(AIPersonInfoQueryRequest));
        return ResultUtils.success(aIPersonInfoPage);
    }

    // endregion



    /**
     * 编辑（用户）
     *
     * @param AIPersonInfoEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editAIPersonInfo(@RequestBody AIPersonInfoEditRequest AIPersonInfoEditRequest, HttpServletRequest request) {
        if (AIPersonInfoEditRequest == null || AIPersonInfoEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AIPersonInfo aIPersonInfo = new AIPersonInfo();
        BeanUtils.copyProperties(AIPersonInfoEditRequest, aIPersonInfo);
        // 参数校验
        aIPersonInfoService.validAIPersonInfo(aIPersonInfo, false);
        User loginUser = userService.getLoginUser(request);
        long id = AIPersonInfoEditRequest.getId();
        // 判断是否存在
        AIPersonInfo oldAIPersonInfo = aIPersonInfoService.getById(id);
        ThrowUtils.throwIf(oldAIPersonInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldAIPersonInfo.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = aIPersonInfoService.updateById(aIPersonInfo);
        return ResultUtils.success(result);
    }

}
