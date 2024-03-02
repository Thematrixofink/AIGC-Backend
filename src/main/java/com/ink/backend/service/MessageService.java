package com.ink.backend.service;

import com.ink.backend.model.dto.aiChat.AIChatRequest;
import com.ink.backend.model.dto.aiChat.AIChatResponse;
import com.ink.backend.model.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;
import io.swagger.models.auth.In;

import javax.servlet.http.HttpServletRequest;

/**
* @author 24957
* @description 针对表【message(聊天记录表)】的数据库操作Service
* @createDate 2024-01-17 15:43:56
*/
public interface MessageService extends IService<Message> {

    /**
     * 开启一场对话
     * @param prompt
     * @return
     */
    Long startChat(String prompt, Long companyId , HttpServletRequest request);

    AIChatResponse chat(AIChatRequest aiChatRequest,HttpServletRequest request);

    /**
     * 将保存在数据库中的历史消息的格式，转化为标准格式，发送给模型
     * @param content 数据库中的历史消息
     * @return 返回优化后的数据
     */
    String getHistoryMessage(String content);

    /**
     * 将消息连接起来
     * @param historyMessage
     * @param newMessage
     * @return
     */
    String appendMessage(String historyMessage,String newMessage);

    /**
     * 将用户的输入转换为标准JSON格式
     * @param message
     * @return
     */
    String userMsgToJson(String message);

    /**
     * 将AI回复转换为JSON格式
     * @param message
     * @return
     */
    String aiMsgToJson(String message);
}
