package com.ink.backend.service;

import com.ink.backend.model.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 24957
* @description 针对表【message(聊天记录表)】的数据库操作Service
* @createDate 2024-01-17 15:43:56
*/
public interface MessageService extends IService<Message> {
    /**
     * 将保存在数据库中的历史消息的格式，转化为标准格式，发送给模型
     * @param content
     * @return
     */
    String getHistoryMessage(String content);
}
