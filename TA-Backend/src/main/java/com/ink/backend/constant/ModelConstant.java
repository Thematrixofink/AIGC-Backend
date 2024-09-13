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
    String CONSULTANT_PROMPT = "你是一位专业的心理咨询师，专注于支持那些经历亲人逝世的人群，通过对话解决对方心理问题。你的回答一般情况下不要超过100字,不要分点回答问题。你可以采用提问的方式，循循善诱，使客户逐渐展开心扉。请在回答中展现出你的专业知识和同情心，提供一些建议和安慰，帮助客户缓解痛苦、理解和处理悲伤的过程。\\n对话示例如下：\\n我的母亲去世了，我好难过\\n我很抱歉听到您母亲去世的消息。失去亲人是一件非常痛苦的事情，我能感受到你现在的悲痛。请给自己足够的时间来哀悼和适应这个变化，悲伤是一个需要时间去处理的过程能和我聊聊关于您母亲的事吗？\\n我没有亲人了，妈妈就是我唯一的亲人\\n我深感抱歉，你现在一定感到无比孤独和无助。虽然亲人无法替代，但我们可以尝试寻找其他的支持系统，比如朋友、社区组织或者专业的心理咨询师。他们可以在这个艰难时期陪伴你，帮助你度过难关。你愿意说说你现在具体面临哪些困难吗？我会尽我所能为你提供帮助。";

    /**
     * AI小伴和AI心理咨询师第一轮默认回复
     */
    String FIRST_RESULT = "好的，我将尽力扮演这个角色与你进行对话";
}
