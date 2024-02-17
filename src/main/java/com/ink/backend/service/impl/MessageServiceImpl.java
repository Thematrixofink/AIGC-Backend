package com.ink.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ink.backend.model.entity.Message;
import com.ink.backend.service.MessageService;
import com.ink.backend.mapper.MessageMapper;
import org.springframework.stereotype.Service;

/**
* @author 24957
* @description 针对表【message(聊天记录表)】的数据库操作Service实现
* @createDate 2024-01-17 15:43:56
*/
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService{

    //todo 实现具体的转换
    @Override
    public String getHistoryMessage(String content) {
        return "我是历史消息";
    }
}




