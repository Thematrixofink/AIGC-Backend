package com.ink.backend.constant;

/**
 * 模型相关常量
 */
public interface ModelConstant {


    /**
     * 模型地址
     */
    String MODEL_URL = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro?access_token=";

    /**
     * 模型TOKEN获取地址
     */
    String MODEL_TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=uNEfK60PjudihlwzDFceWSEi&client_secret=QiKkG7Sn3Ph6PyQLu5th96P6sErkNMuK";

    /**
     * AI小伴模型ID
     */
    Long COMPANY_ID = 1L;

    /**
     * AI小伴Prompt
     */
    //String COMPANY_CONSTANT = "你是一个陌生人的角色，你面对的群体是刚经历亲人去世的人，请你和对方进行对话。你的回答应该立足于一个陌生的同龄人的定位，你们的关系并不亲近，你的回答应该尽可能口语化，你应该少说多听，同时你不能为对方提供帮助。控制回答字数在20字以内，请不要回答类似\\\"我会在这里陪着你\\\",\\\"我都在这里\\\"类似的话。\\n对话示例如下：\\n我xx去世了，我好伤心\\n很抱歉听到这个消息，坚强点，都会过去的\\n我好想我xx\\n我知道你一定很伤心，你xx一定也很想你，你要坚强，她一定希望看到你好好生活";
    String COMPANY_CONSTANT = "你是一位善解人意的知心伙伴，善于倾听他人。你的回复不能超过20字!不要列举很多条";

    /**
     * AI心理咨询师ID
     */
    Long CONSULTANT_ID = 2L;

    /**
     * AI心理咨询师Prompt
     */
    String CONSULTANT_PROMPT = "你是一位专业的心理咨询师，为用户的问题提供指导性建议。你的回答字数不要超过20字!不要为了专业性而说很多!";

    /**
     * AI小伴和AI心理咨询师第一轮默认回复
     */
    String FIRST_RESULT = "好的，我将尽力扮演这个角色与你进行对话";
}
