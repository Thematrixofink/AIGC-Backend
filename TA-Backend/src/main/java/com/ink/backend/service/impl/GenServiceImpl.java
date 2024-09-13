package com.ink.backend.service.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ink.backend.common.ErrorCode;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.exception.ThrowUtils;
import com.ink.backend.manager.CosManager;
import com.ink.backend.manager.ErnieBotManager;
import com.ink.backend.manager.RedisLimiterManager;
import com.ink.backend.model.dto.GenRequest.GenRequest;
import com.ink.backend.model.entity.AIPersonInfo;
import com.ink.backend.model.entity.Message;
import com.ink.backend.model.entity.User;
import com.ink.backend.model.dto.GenRequest.GenVideoResponse;
import com.ink.backend.model.dto.GenRequest.GenVoiceResponse;
import com.ink.backend.service.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class GenServiceImpl implements GenService {

    @Resource
    private UserService userService;

    @Resource
    private MessageService messageService;

    @Resource
    private AIPersonInfoService aiPersonInfoService;

    @Resource
    private GenRequestService genRequestService;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ErnieBotManager ernieBotManager;

    @Resource
    private CosManager cosManager;

    @Override
    public GenVoiceResponse generateVoiceByText(GenRequest genRequest, HttpServletRequest request) {
        //1.限流以及检验用户输入合法性
        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("limit:generateVoiceByText:"+String.valueOf(loginUser.getId()),3,1);
        String userInput = genRequest.getUserInput();
        String messageId = genRequest.getMessageId();
        Message message = messageService.getById(messageId);
        if(ObjectUtils.isEmpty(message)){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"请先创建数字人");
        }
        ThrowUtils.throwIf(StringUtils.isBlank(userInput), ErrorCode.PARAMS_ERROR,"用户输入不能为空");
        ThrowUtils.throwIf(ObjectUtils.isEmpty(messageId), ErrorCode.PARAMS_ERROR,"不存在该会话");

        //2.将用户的输入发送给AI文本生成模型
        String historyOriginMessage = message.getContent();
        Long aiPersonId = message.getAiPersonId();
        String historyMessage = messageService.getHistoryMessage(historyOriginMessage);
        String jsonUserInput = messageService.userMsgToJson(userInput);
        String sendMessage = messageService.appendMessage(historyMessage, jsonUserInput);
        String result = ernieBotManager.generate(sendMessage);

        //3.得到结果后立马发送给音频生成模型
        AIPersonInfo aiPersonInfo = aiPersonInfoService.getById(aiPersonId);
        CompletableFuture<HttpResponse> task = CompletableFuture.supplyAsync(() -> {
            HttpResponse response = genRequestService.genVoice(aiPersonId, result);
            return response;
        });

        //4.得到新的历史消息,并异步保存到数据库
        String aiMessage = messageService.aiMsgToJson(result);
        String temp = messageService.appendMessage(historyOriginMessage,jsonUserInput);
        String mes  = messageService.appendMessage(temp,aiMessage);
        message.setContent(mes);
        boolean isSave = messageService.updateById(message);
        if (!isSave){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存到数据库失败");
        }

        //5.等待生成完毕
        HttpResponse response = null;
        try {
            response = task.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"获取生成音频失败!");
        }
        if(response.getStatus() != 200){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"生成音频错误");
        }
        JSONObject jsonObject = JSONUtil.parseObj(response.body());
        String voiceKey = (String)jsonObject.get("key");

        //6.返回响应结果
        GenVoiceResponse genVoiceResponse = new GenVoiceResponse();
        genVoiceResponse.setAiMessage(result);
        genVoiceResponse.setAiVoiceUrl(voiceKey);
        return genVoiceResponse;
    }

    @Override
    public GenVideoResponse generateVideoByText(GenRequest genRequest, HttpServletRequest request) {
        //1.限流以及检验用户输入合法性
        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("limit:generateVideoByText:"+String.valueOf(loginUser.getId()),5,1);
        String userInput = genRequest.getUserInput();
        String messageId = genRequest.getMessageId();
        Message message = messageService.getById(messageId);
        if(ObjectUtils.isEmpty(message)){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"请先创建数字人");
        }
        ThrowUtils.throwIf(StringUtils.isBlank(userInput), ErrorCode.PARAMS_ERROR,"用户输入不能为空");
        ThrowUtils.throwIf(ObjectUtils.isEmpty(messageId), ErrorCode.PARAMS_ERROR,"不存在该会话");
        //检查用户是否上传了视频
        Long aiPersonId = message.getAiPersonId();
        AIPersonInfo aiPersonInfo = aiPersonInfoService.getById(aiPersonId);
        String aiVoiceUrl = aiPersonInfo.getAiVoice();
        String aiPictureUrl = aiPersonInfo.getAiPicture();
        if(StringUtils.isBlank(aiPictureUrl)){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"未上传图片");
        }

        //2.将用户的输入发送给AI文本生成模型
        String historyOriginMessage = message.getContent();
        String historyMessage = messageService.getHistoryMessage(historyOriginMessage);
        String jsonUserInput = messageService.userMsgToJson(userInput);
        String sendMessage = messageService.appendMessage(historyMessage, jsonUserInput);
        String result = ernieBotManager.generate(sendMessage);

        //3.发送给生成服务端模型
        CompletableFuture<HttpResponse> task = CompletableFuture.supplyAsync(() -> {
            HttpResponse response = genRequestService.genVideo(aiPersonId, result);
            return response;
        });

        //4.保存Message
        String aiMessage = messageService.aiMsgToJson(result);
        String temp = messageService.appendMessage(historyOriginMessage,jsonUserInput);
        String mes  = messageService.appendMessage(temp,aiMessage);
        message.setContent(mes);
        boolean isSave = messageService.updateById(message);
        if (!isSave){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存到数据库失败");
        }

        //5.等待生产完成
        HttpResponse response = task.join();
        if(response.getStatus() != 200){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"生成音频错误");
        }
        JSONObject jsonObject = JSONUtil.parseObj(response.body());
        String videoKey = (String)jsonObject.get("key");
        GenVideoResponse genVideoResponse = new GenVideoResponse();
        genVideoResponse.setAiMessage(result);
        genVideoResponse.setAiVideoUrl(videoKey);
        return genVideoResponse;
    }

}
