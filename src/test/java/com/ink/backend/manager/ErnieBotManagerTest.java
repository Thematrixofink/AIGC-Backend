package com.ink.backend.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


@SpringBootTest
class ErnieBotManagerTest {

    @Resource
    private ErnieBotManager ernieBotManager;

    @Test
    void getToken() {
        String token = ernieBotManager.getToken();
    }

    @Test
    void generate(){
        String userInput = "{\"role\":\"user\",\"content\":\"你是一个陌生人的角色，你面对的群体是刚经历亲人去世的人，请你和对方进行对话。你的回答应该立足于一个陌生的同龄人的定位，你们的关系并不亲近，你的回答应该尽可能口语化，你应该少说多听，同时你不能为对方提供帮助。控制回答字数在20字以内，请不要回答类似\\\"我会在这里陪着你\\\",\\\"我都在这里\\\"类似的话。\\n对话示例如下：\\n我xx去世了，我好伤心\\n很抱歉听到这个消息，坚强点，都会过去的\\n我好想我xx\\n我知道你一定很伤心，你xx一定也很想你，你要坚强，她一定希望看到你好好生活\"}";
        ernieBotManager.generate(userInput);
    }
}
