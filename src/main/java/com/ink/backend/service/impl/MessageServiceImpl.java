package com.ink.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ink.backend.common.ErrorCode;
import com.ink.backend.common.ResultUtils;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.manager.RedisLimiterManager;
import com.ink.backend.model.dto.aiChat.AIChatRequest;
import com.ink.backend.model.dto.aiChat.AIChatResponse;
import com.ink.backend.model.entity.Message;
import com.ink.backend.model.entity.User;
import com.ink.backend.service.MessageService;
import com.ink.backend.mapper.MessageMapper;
import com.ink.backend.service.UserService;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
* @author 24957
* @description 针对表【message(聊天记录表)】的数据库操作Service实现
* @createDate 2024-01-17 15:43:56
*/
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService{

    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;

    @Resource
    private YuCongMingClient yuCongMingClient;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    //关联的对话轮数
    private static final Integer RELATIVE_MESSAGE_COUNT = 5;


    @Override
    public Long startChat(String prompt, Long companyId, HttpServletRequest request) {

        //todo 判断限流
        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("startChat_"+String.valueOf(loginUser.getId()));

        Message message = new Message();
        message.setUserId(loginUser.getId());
        message.setAiPersonId(companyId);
        String jsonUserInput = messageService.userMsgToJson(prompt);

        //todo 换成文心一言大模型
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1748617589749583874L);
        devChatRequest.setMessage(jsonUserInput);
        com.yupi.yucongming.dev.common.BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        String result = response.getData().getContent();

        //将AI返回的消息保存下来
        String aiMessage = messageService.aiMsgToJson(result);
        String mes = messageService.appendMessage(jsonUserInput,aiMessage);
        message.setContent(mes);
        boolean save = messageService.save(message);
        if(!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return message.getId();
    }

    @Override
    public AIChatResponse chat(AIChatRequest aiChatRequest, HttpServletRequest request) {
        Long id = aiChatRequest.getChatId();
        String userInput = aiChatRequest.getUserInput();
        Message message = messageService.getById(id);
        if(ObjectUtils.isEmpty(message)){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if(message.getIsDelete() == 1){
            AIChatResponse response = new AIChatResponse();
            response.setReply("delete");
            return response;
        }
        String historyOriginMessage = message.getContent();
        String historyMessage = messageService.getHistoryMessage(historyOriginMessage);
        //构建用户输入
        String jsonUserInput = messageService.userMsgToJson(userInput);
        String sendMessage = messageService.appendMessage(historyMessage,jsonUserInput);

        //todo 换成文心一言大模型
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1748617589749583874L);
        devChatRequest.setMessage(sendMessage);
        com.yupi.yucongming.dev.common.BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        String result = response.getData().getContent();

        //将AI返回的消息保存下来
        String aiMessage = messageService.aiMsgToJson(result);
        String temp = messageService.appendMessage(historyOriginMessage, jsonUserInput);
        String mes  = messageService.appendMessage(temp,aiMessage);
        message.setContent(mes);
        boolean update = messageService.updateById(message);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //todo 异步更新数据库
        AIChatResponse aiChatResponse = new AIChatResponse();
        aiChatResponse.setChatId(id);
        aiChatResponse.setReply(result);
        return aiChatResponse;
    }

    /**
     * 将历史消息转换为管理某几条历史消息。
     * @param content 数据库中的历史消息
     * @return
     */
    @Override
    public String getHistoryMessage(String content) {
        String[] messages = content.split(",\n");
        int messageCount = messages.length;
        if(messageCount <= 10){
            return content;
        }
        //除去prompt以及应答外剩余的对话数
        int leftMessagesCount = RELATIVE_MESSAGE_COUNT * 2 - 2;
        //开始的messages的下标
        int i = messageCount - leftMessagesCount;
        StringBuilder result = new StringBuilder();
        result.append(messages[0]).append(",\n").append(messages[1]).append(",\n");
        for(int j = i ; j < messageCount - 1 ; j++){
            result.append(messages[j]).append(",\n");
        }
        result.append(messages[messageCount-1]);
        return result.toString();
    }

    @Override
    public String appendMessage(String historyMessage, String newMessage) {
        StringBuilder result = new StringBuilder();
        result.append(historyMessage).append(",\n").append(newMessage);
        return result.toString();
    }

    @Override
    public String userMsgToJson(String message) {
        return "{\"role\":\"user\",\"content\":\""+message+"\"}";
    }

    @Override
    public String aiMsgToJson(String message) {
        return "{\"role\":\"assistant\",\"content\":\""+message+"\"}";
    }
}




