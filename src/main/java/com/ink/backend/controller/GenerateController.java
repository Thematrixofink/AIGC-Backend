package com.ink.backend.controller;

import com.ink.backend.common.BaseResponse;
import com.ink.backend.common.ErrorCode;
import com.ink.backend.common.ResultUtils;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.exception.ThrowUtils;
import com.ink.backend.manager.RedisLimiterManager;
import com.ink.backend.model.dto.GenRequest.GenRequest;
import com.ink.backend.model.entity.AIPersonInfo;
import com.ink.backend.model.entity.Message;
import com.ink.backend.model.entity.User;
import com.ink.backend.model.vo.GenVoiceResponse;
import com.ink.backend.service.AIPersonInfoService;
import com.ink.backend.service.MessageService;
import com.ink.backend.service.UserService;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
public class GenerateController {

    @Resource
    private UserService userService;
    @Resource
    private MessageService messageService;
    @Resource
    private AIPersonInfoService aiPersonInfoService;
    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Resource
    private YuCongMingClient yuCongMingClient;


    /**
     * 通过文字生成音频
     * @param genRequest
     * @param request
     * @return
     */
    @PostMapping("/generateVoiceByText")
    public BaseResponse<GenVoiceResponse> generateVoiceByText(@RequestBody GenRequest genRequest, HttpServletRequest request) {
        //1.检验用户输入合法性
        String userInput = genRequest.getUserInput();
        String messageId = genRequest.getMessageId();
        boolean needVideo = genRequest.isNeedVideo();
        Message message = messageService.getById(messageId);
        if(ObjectUtils.isEmpty(message)){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"请先创建数字人");
        }
        ThrowUtils.throwIf(StringUtils.isBlank(userInput), ErrorCode.PARAMS_ERROR,"用户输入不能为空");
        ThrowUtils.throwIf(ObjectUtils.isEmpty(messageId), ErrorCode.PARAMS_ERROR,"不存在该会话");

        //todo 判断限流
        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("generateVoiceByText_"+String.valueOf(loginUser.getId()));

        //2.将用户的输入发送给AI文本生成模型
        //需要把历史消息也发送给文本生成模型
        String historyMessage = message.getContent();
        Long aiPersonId = message.getAiPersonId();

        //todo 将历史信息连同着新的信息发送给AI文本模型
        //todo getHistoryMessage方法待完善
        String historicMessage = messageService.getHistoryMessage(historyMessage);

        //todo 将历史消息拼接上用户新的输入，发送给AI文本模型
        String sendMessage = historicMessage +"user:"+ genRequest.getUserInput();
        //todo 发送给AI文本模型

        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1748617589749583874L);
        devChatRequest.setMessage(sendMessage);
        com.yupi.yucongming.dev.common.BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        String result = response.getData().getContent();
        System.out.println(result);

        //3.保存到Message中去

        //todo 对消息进行操作
        //message.setContent(mes.toString());

        boolean isSave = messageService.updateById(message);
        if (!isSave){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存到数据库失败");
        }
        //4.发送给音（视）频生成模型
        AIPersonInfo aiPersonInfo = aiPersonInfoService.getById(aiPersonId);
        String aiVoiceUrl = aiPersonInfo.getAiVoice();
        String aiPictureUrl = aiPersonInfo.getAiPicture();
        //如果需要生成视频
        if(needVideo){
            ThrowUtils.throwIf(StringUtils.isBlank(aiPictureUrl),ErrorCode.PARAMS_ERROR,"图片地址不能为空");
            //todo 调用生成视频的模型 发送：messageId、result
            System.out.println("生成视频。。。");
        }
        //如果不生成视频
        //todo 调用生成音频的模型  发送：messageId、result
        System.out.println("生成音频。。。");
        //todo 学习异步操作以及回调函数
        GenVoiceResponse genVoiceResponse = new GenVoiceResponse();
        return ResultUtils.success(genVoiceResponse);
    }


}
