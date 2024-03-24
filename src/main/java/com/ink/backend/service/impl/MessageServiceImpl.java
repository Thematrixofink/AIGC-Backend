package com.ink.backend.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ink.backend.common.ErrorCode;
import com.ink.backend.common.ResultUtils;
import com.ink.backend.constant.ModelConstant;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.manager.ErnieBotManager;
import com.ink.backend.manager.GLM4Manager;
import com.ink.backend.manager.RedisLimiterManager;
import com.ink.backend.model.dto.aiChat.AIChatRequest;
import com.ink.backend.model.dto.aiChat.AIChatResponse;
import com.ink.backend.model.dto.networkRequest.MessageModelRequest;
import com.ink.backend.model.entity.Message;
import com.ink.backend.model.entity.User;
import com.ink.backend.service.MessageService;
import com.ink.backend.mapper.MessageMapper;
import com.ink.backend.service.UserService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* @author 24957
* @description 针对表【message(聊天记录表)】的数据库操作Service实现
* @createDate 2024-01-17 15:43:56
*/
@Service
@Slf4j
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService{


    @Resource
    private UserService userService;

    @Resource
    private ErnieBotManager ernieBotManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private GLM4Manager glm4Manager;

    //关联的对话轮数
    private static final Integer RELATIVE_MESSAGE_COUNT = 5;

    //心理咨询师师关联的对话轮数
    private static final Integer CONSULTANT_RELATIVE_MESSAGE_COUNT = 2;

    @Override
    public Long startChat(String prompt, Long companyId, HttpServletRequest request) {

        //todo 判断限流
        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("limit:startChat:"+String.valueOf(loginUser.getId()),2,1);

        Message message = new Message();
        message.setUserId(loginUser.getId());
        message.setAiPersonId(companyId);
        String jsonUserInput = userMsgToJson(prompt);
        String result = ModelConstant.FIRST_RESULT;

        //将AI返回的消息保存下来
        String aiMessage = aiMsgToJson(result);
        String mes = appendMessage(jsonUserInput,aiMessage);
        message.setContent(mes);
        boolean save = save(message);
        if(!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return message.getId();
    }

    @Override
    public AIChatResponse chat(AIChatRequest aiChatRequest, HttpServletRequest request) {
        //1.限流以及检验参数合法性
        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("limit:chat:"+String.valueOf(loginUser.getId()),2,1);
        Long id = aiChatRequest.getChatId();
        String userInput = aiChatRequest.getUserInput();
        Message message = getById(id);
        if(ObjectUtils.isEmpty(message)){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if(message.getIsDelete() == 1){
            AIChatResponse response = new AIChatResponse();
            response.setReply("delete");
            return response;
        }
        String historyOriginMessage = message.getContent();

        String historyMessage = getHistoryMessage(historyOriginMessage);
        //构建用户输入
        String jsonUserInput = userMsgToJson(userInput);
        String sendMessage = appendMessage(historyMessage,jsonUserInput);
        List<String> input = jsonToList(sendMessage);
//        String result = glm4Manager.generate(input);
        String result = ernieBotManager.generate(sendMessage);
        log.info("生成文字结果为:"+result);
        if(result == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //将AI返回的消息保存下来
        String aiMessage = aiMsgToJson(result);
        String temp = appendMessage(historyOriginMessage, jsonUserInput);
        String mes  = appendMessage(temp,aiMessage);
        message.setContent(mes);
        boolean update = updateById(message);
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
    private String getHistoryMessageTemplate(Integer round,String content) {
        String[] messages = content.split(",\n");
        int messageCount = messages.length;
        if(messageCount <= 2 * RELATIVE_MESSAGE_COUNT){
            return content;
        }
        //除去prompt以及应答外剩余的对话数
        int leftMessagesCount = round * 2 - 2;
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

    /**
     * 将JSON数据转换为消息列表，第一条消息为用户输入
     * @param jsonInput
     * @return
     */
    private List<String> jsonToList(String jsonInput){
        String input = "["+jsonInput+"]";
        List<MessageModelRequest> modelRequests = JSONUtil.toList(input, MessageModelRequest.class);
        List<String> result = new ArrayList<>();
        List<String> messages = modelRequests.stream().map(new Function<MessageModelRequest, String>() {
            @Override
            public String apply(MessageModelRequest messageModelRequest) {
                return messageModelRequest.getContent();
            }
        }).collect(Collectors.toList());
        return messages;
    }

    @Override
    public String getHistoryMessage(String content) {
        return getHistoryMessageTemplate(RELATIVE_MESSAGE_COUNT, content);
    }

    @Override
    public String getConsultantMessage(String content) {
        return getHistoryMessageTemplate(CONSULTANT_RELATIVE_MESSAGE_COUNT,content);
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

    @Override
    public List<Message> getNowMessage(Long userId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Message::getUserId,userId);
        queryWrapper.eq(Message::getIsDelete,0);
        queryWrapper.ne(Message::getAiPersonId,1);
        queryWrapper.ne(Message::getAiPersonId,2);
        List<Message> messages = list(queryWrapper);
        return messages;
    }
}




