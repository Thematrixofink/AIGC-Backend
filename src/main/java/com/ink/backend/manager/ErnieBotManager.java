package com.ink.backend.manager;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.ink.backend.constant.ModelConstant;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * ERNIE-BOT模型Manger
 */
@Component
public class ErnieBotManager {

    @Resource
    private YuCongMingClient client;


    /**
     * 获取access_token
     * @return
     */
    public String getToken(){
        HttpRequest request = HttpRequest.post(ModelConstant.MODEL_TOKEN_URL);
        HttpResponse response = request.header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .execute();
        String responseBody = response.body();
        JSONObject jsonObject = JSONUtil.parseObj(responseBody);
        return jsonObject.getStr("access_token");
    }

//    /**
//     * 根据JSON格式的输入生成文字
//     * @param jsonInput
//     * @return
//     */
//    public String generate(String jsonInput){
//        String usableURL = ModelConstant.MODEL_URL + getToken();
//        String body = "{\n" +
//                "  \"messages\": [\n" + jsonInput +
//                "  ]\n" +
//                "}";
//        HttpRequest httpRequest = HttpRequest.post(usableURL)
//                .header("Content-Type", "application/json")
//                .body(body);
//        System.out.println("request为==========================");
//        System.out.println(httpRequest);
//        HttpResponse response = httpRequest.execute();
//        System.out.println("response为==========================");
//        System.out.println(response);
//        JSONObject jsonObject = JSONUtil.parseObj(response.body());
//        return jsonObject.getStr("result");
//    }

    /**
     * 鱼聪明版本
     * @param jsonInput
     * @return
     */
    public String generate(String jsonInput){
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1654785040361893889L);
        devChatRequest.setMessage(jsonInput);
        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        //拿到json格式的数据
        String content = response.getData().getContent();
        //如果返回的是json格式的话
        if(content.startsWith("{")) {
            Gson gson = new Gson();
            Map map = gson.fromJson(content, Map.class);
            Object content1 = map.get("content");
            return String.valueOf(content1);
        }
        return content;
    }
}
