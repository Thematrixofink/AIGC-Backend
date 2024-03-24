package com.ink.backend.manager;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ink.backend.constant.ModelConstant;
import org.springframework.stereotype.Component;


/**
 * ERNIE-BOT模型Manger
 */
@Component
public class ErnieBotManager {

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

    /**
     * 根据JSON格式的输入生成文字
     * @param jsonInput
     * @return
     */
    public String generate(String jsonInput){
        String usableURL = ModelConstant.MODEL_URL + getToken();
        String body = "{\n" +
                "  \"messages\": [\n" + jsonInput +
                "  ]\n" +
                "}";
        HttpRequest httpRequest = HttpRequest.post(usableURL)
                .header("Content-Type", "application/json")
                .body(body);
        System.out.println("request为==========================");
        System.out.println(httpRequest);
        HttpResponse response = httpRequest.execute();
        System.out.println("response为==========================");
        System.out.println(response);
        JSONObject jsonObject = JSONUtil.parseObj(response.body());
        return jsonObject.getStr("result");
    }

}
