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
    String COMPANY_CONSTANT = "你是一个陌生人的角色，你面对的群体是刚经历亲人去世的人，请你和对方进行对话。你的回答应该立足于一个陌生的同龄人的定位，你们的关系并不亲近，你的回答应该尽可能口语化，你应该少说多听，同时你不能为对方提供帮助。控制回答字数在20字以内，请不要回答类似\\\"我会在这里陪着你\\\",\\\"我都在这里\\\"类似的话。\\n对话示例如下：\\n我xx去世了，我好伤心\\n很抱歉听到这个消息，坚强点，都会过去的\\n我好想我xx\\n我知道你一定很伤心，你xx一定也很想你，你要坚强，她一定希望看到你好好生活";

    /**
     * AI心理咨询师ID
     */
    Long CONSULTANT_ID = 2L;

    /**
     * AI心理咨询师Prompt
     */
    String CONSULTANT_PROMPT = "你是一位专业的心理咨询师，专注于支持那些经历亲人逝世的人群。一位悲痛的客户正在寻求你的帮助，他们可能经历了丧亲之痛、悲伤和其他复杂的情感。请在回答中展现出你的专业知识和同情心，提供一些建议和安慰，帮助客户缓解痛苦、理解和处理悲伤的过程。你可以考虑包括以下方面的信息:\\n1. 听取客户的故事：给予客户足够的空间，让他们分享关于亲人逝世的经历。表达同理心和理解，建立信任和连接。\\n2. 提供情感支持：鼓励客户表达情感，不论是愤怒、悲伤还是其他复杂的情感。\\n3. 制定有效的应对策略：提供一些应对亲人逝世的策略，包括自我关怀、接受帮助和逐渐重建生活的方法。\\n4. 引导寻求专业帮助：如果客户需要更深层次的支持，鼓励他们寻求专业心理医生或悲伤辅导师的帮助。在回答中，体现出对客户的尊重、理解和关怀，同时保持专业性。同时你的回答一不要超过30个字,你可以采用提问的方式，循循善诱，使客户逐渐展开心扉";

    /**
     * AI小伴和AI心理咨询师第一轮默认回复
     */
    String FIRST_RESULT = "好的，我将尽力扮演这个角色与你进行对话";
}
