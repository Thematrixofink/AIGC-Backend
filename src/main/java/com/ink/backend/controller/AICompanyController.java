package com.ink.backend.controller;

import cn.hutool.core.util.ObjectUtil;
import com.ink.backend.common.BaseResponse;
import com.ink.backend.common.ErrorCode;
import com.ink.backend.common.ResultUtils;
import com.ink.backend.constant.ModelConstant;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.model.dto.aiChat.AIChatRequest;
import com.ink.backend.model.dto.aiChat.AIChatResponse;
import com.ink.backend.model.entity.Message;
import com.ink.backend.model.entity.User;
import com.ink.backend.service.MessageService;
import com.ink.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * ai小伴
 * `
 */
@RestController
@RequestMapping("/aiCompany")
@Slf4j
public class AICompanyController {

    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;



    /**
     * 开启一个对话
     * @param request
     * @return
     */
    @PostMapping("/start")
    public BaseResponse<Long> startChat(HttpServletRequest request){
        Long chatId = messageService.startChat(ModelConstant.COMPANY_CONSTANT, ModelConstant.COMPANY_ID, request);
        return ResultUtils.success(chatId);
    }

    /**
     * 和ai小伴进行聊天
     * @param aiChatRequest
     * @param request
     * @return
     */
    @PostMapping("/chat")
    public BaseResponse<AIChatResponse> chat(@RequestBody AIChatRequest aiChatRequest, HttpServletRequest request){
        AIChatResponse response = messageService.chat(aiChatRequest, request);
        if(response.getReply().equals("delete")){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"对话已关闭");
        }
        return ResultUtils.success(response);
    }

    /**
     * 关闭一个对话
     * @param chatId
     * @param request
     * @return
     */
    @PostMapping("/stop")
    public BaseResponse<Boolean> stopChat(@RequestParam Long chatId,HttpServletRequest request){
        Message message = messageService.getById(chatId);
        if(ObjectUtils.isEmpty(message)){
             throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        message.setIsDelete(1);
        boolean update = messageService.updateById(message);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<String> getHistoryMessage(long id,HttpServletRequest request){
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Message message = messageService.getById(id);
        if(ObjectUtil.isNull(message) || ObjectUtil.isEmpty(message)){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        if(!message.getUserId().equals(loginUserId)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"无权限");
        }
        return ResultUtils.success(message.getContent());
    }
}
