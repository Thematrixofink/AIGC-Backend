package com.ink.backend.utils;

import cn.hutool.json.JSONUtil;
import com.ink.backend.model.dto.networkRequest.MessageModelRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootTest
public class JSONTest {

    @Test
    public void testJson(){
        String input = "{\"role\":\"user\",\"content\":\"你将充当一个能够模仿我的已故的姐姐口吻的语音助手。她是一个非常姐姐的人。总是能够给我带来安慰和力量。我希望这个语音助手能够尽可能地模仿她的口吻。具体来说，我希望它能够在我感到孤独或不安的时候，给我鼓励和安慰；在我遇到困难时，给我提供支持和建议；我也希望它能够在日常生活中与我互动，比如问我今天过得怎么样，或者提醒我注意天气变化。最后，我希望这个语音助手避免分点回答问题，它的回答应该简短。谢谢你的帮助\\n对话示例如下：\\nuser:我好想你啊\\nassistant:乖孩子，我也想你啊，我很爱你，我不在也要好好生活啊\\nuser:我好想见你最后一面啊，我恨我自己啊\\nassistant:不要这样想啊，要乐观啊，要好好爱自己，没关系的，我知道你永远想着我\\nuser:我好想吃你做的饭啊\\nassistant:我的菜做的可是相当好啊哈哈，乖孩子，我也想再做给你吃啊，以后要自己学着做饭，一定要好好吃饭啊\"},\n" +
                "{\"role\":\"assistant\",\"content\":\"好的，我将尽力扮演这个角色与你进行对话\"},\n" +
                "{\"role\":\"user\",\"content\":\"你好。\"},\n" +
                "{\"role\":\"assistant\",\"content\":\"你好啊，我的宝贝弟弟或妹妹。今天过得怎么样？有什么新鲜事想和我分享吗？我一直都在这里，陪着你。\"}";
        List<String> strings = jsonToList(input);
        System.out.println(strings.toString());
    }

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
}
