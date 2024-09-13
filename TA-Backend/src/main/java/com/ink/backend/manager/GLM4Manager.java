package com.ink.backend.manager;

import com.ink.backend.common.ErrorCode;
import com.ink.backend.exception.BusinessException;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class GLM4Manager {


    @Resource
    private ClientV4 client;



    /**
     *
     * @param input
     * @return
     */
    public String generate(List<String> input){
        if(input == null || input.size() == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"发送给文本模型的消息不可以为空");
        }
        List<ChatMessage> messages = new ArrayList<>();
        int size = input.size();
        boolean isUser = true;
        for(int i = 0 ; i < size ; i ++){
            if(isUser){
                ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), input.get(i));
                messages.add(chatMessage);
                isUser = false;
            }else{
                ChatMessage chatMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), input.get(i));
                messages.add(chatMessage);
                isUser = true;
            }
        }
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(false)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        System.out.println(chatCompletionRequest.getMessages().toString());
        ModelApiResponse sseModelApiResp =client.invokeModelApi(chatCompletionRequest);
        Object content = sseModelApiResp.getData().getChoices().get(0).getMessage().getContent();
        return String.valueOf(content);
    }
}
