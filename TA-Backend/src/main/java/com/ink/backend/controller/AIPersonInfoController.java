package com.ink.backend.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ink.backend.annotation.AuthCheck;
import com.ink.backend.common.*;
import com.ink.backend.constant.ModelConstant;
import com.ink.backend.constant.UserConstant;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.exception.ThrowUtils;
import com.ink.backend.manager.RedisLimiterManager;
import com.ink.backend.model.dto.GenRequest.GenAIRequest;
import com.ink.backend.model.dto.aIPersonInfo.AIPersonInfoAddRequest;
import com.ink.backend.model.dto.aIPersonInfo.AIPersonInfoEditRequest;
import com.ink.backend.model.dto.aIPersonInfo.AIPersonInfoQueryRequest;
import com.ink.backend.model.dto.aIPersonInfo.AIPersonInfoUpdateRequest;
import com.ink.backend.model.entity.AIPersonInfo;
import com.ink.backend.model.entity.Message;
import com.ink.backend.model.entity.User;
import com.ink.backend.model.dto.GenRequest.StatusResponse;
import com.ink.backend.mq.AIGCMessageProducer;
import com.ink.backend.mq.MQConstant;
import com.ink.backend.service.AIPersonInfoService;
import com.ink.backend.service.GenRequestService;
import com.ink.backend.service.MessageService;
import com.ink.backend.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI数字人信息接口
 *
 */
@RestController
@RequestMapping("/aiPerson")
@Slf4j
public class AIPersonInfoController {

    @Resource
    private AIPersonInfoService aIPersonInfoService;

    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;

    @Resource
    private RedisLimiterManager redisLimiterManager;


    @Resource
    private AIGCMessageProducer aigcMessageProducer;


    /**
     * 生成数字人
     * @param genAIRequest
     * @param request
     * @return
     */
    @PostMapping("/preGenerator")
    public BaseResponse<String> preGenerator(@RequestBody GenAIRequest genAIRequest, HttpServletRequest request) {
        //1.检查用户是否还有使用功能的次数以及进行限流
        User user = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("limit:preGenerator:"+String.valueOf(user.getId()),1,1);
        AtomicReference<Integer> aigcCount = new AtomicReference<>(user.getAigcCount());
        if(aigcCount.get() <= 0){
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR,"您的次数已经用光！");
        }
        // 检查是否有已经开启的对话
        List<Message> nowMessage = messageService.getNowMessage(user.getId());
        if(nowMessage.size() > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"现在已经有Message在进行");
        }
        String aiName = genAIRequest.getAiName();
        String aiProfile = genAIRequest.getAiProfile();
        String aiVoice = genAIRequest.getAiVoice();
        String aiPicture = genAIRequest.getAiPicture();

        //2.对各项属性进行校验
        if(StringUtils.isBlank(aiName) || StringUtils.isBlank(aiProfile) || StringUtils.isBlank(aiVoice)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        boolean voiceValid = validURL(aiVoice,user.getId());
        boolean pictureValid = true;
        if(StringUtils.isNotBlank(aiPicture)){
             pictureValid = validURL(aiPicture, user.getId());
        }
        if(!voiceValid || !pictureValid){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件路径非法");
        }

        //3.创建AIperson
        Long userId = user.getId();
        AIPersonInfo aiPersonInfo = new AIPersonInfo();
        aiPersonInfo.setUserId(userId);
        aiPersonInfo.setAiName(aiName);
        aiPersonInfo.setAiProfile(aiProfile);
        aiPersonInfo.setAiVoice(aiVoice);
        aiPersonInfo.setAiPicture(aiPicture);
        aiPersonInfo.setStatus(StatusCode.TO_GEN.getCode());
        //3.将aiPersonInfo保存到数据库
        boolean isSaveAiInfo = aIPersonInfoService.save(aiPersonInfo);
        if(!isSaveAiInfo){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新任务状态失败");
        }
        Long id = aiPersonInfo.getId();

        //5. 构件第一轮聊天
        Message message = new Message();
        message.setAiPersonId(aiPersonInfo.getId());
        message.setUserId(userId);
        String userInput = getPrompt(aiName,aiProfile);
        String userMessage = messageService.userMsgToJson(userInput);

        //6.将AI返回的消息保存下来
        String result = ModelConstant.FIRST_RESULT;
        String aiMessage = messageService.aiMsgToJson(result);
        String mes = messageService.appendMessage(userMessage, aiMessage);
        message.setContent(mes);
        boolean isSaveMes = messageService.save(message);
        if(!isSaveMes){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        //7.对话的唯一id，返回给前端，之后前端请求必须携带着这个对话的id
        Long messageId = message.getId();

        //4.向消息队列发送消息消息队列
        String mqMessage = id+"&"+userId+"&"+messageId+"&"+aigcCount;
        aigcMessageProducer.sendMessage(MQConstant.AIGC_EXCHANGE_NAME,MQConstant.AIGC_ROUTING_KEY,mqMessage);

        log.info("返回messageId："+messageId);
        return ResultUtils.success(messageId.toString());
    }

    /**
     * 查询任务状况
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/getStatus")
    public BaseResponse<StatusResponse> getStatus(@RequestParam("id")Long id, HttpServletRequest request){
        if(ObjectUtils.isEmpty(id)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"id为空!");
        }
        log.info("收到messageId："+id);
        Message message = messageService.getById(id);
        if(message == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"该数字人不存在!");
        }
        Long aiPersonId = message.getAiPersonId();
        AIPersonInfo byId = aIPersonInfoService.getById(aiPersonId);
        if(ObjectUtils.isEmpty(byId) || ObjectUtil.isNull(byId)){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"该数字人不存在!");
        }
        Integer status = byId.getStatus();
        String aiPicture = byId.getAiPicture();
        String messageByCode = StatusCode.getMessageByCode(status);
        StatusResponse response = new StatusResponse();
        response.setStatus(messageByCode);
        //执行失败的话，获取错误信息
        if(status == 3){
            response.setExecMessage(byId.getExecMessage());
        }
        //执行成功的话，获取预生成视频的key
        if(status == 2){
            response.setExecMessage(byId.getExecMessage());
        }
        if(aiPicture == null || StrUtil.isBlank(aiPicture)){
            response.setTask("voice");
        }else{
            response.setTask("video");
        }
        return ResultUtils.success(response);
    }



    // region 增删改查

    /**
     * 创建AI数字人
     *
     * @param AIPersonInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addAIPersonInfo(@RequestBody AIPersonInfoAddRequest AIPersonInfoAddRequest, HttpServletRequest request) {
        if (AIPersonInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AIPersonInfo aIPersonInfo = new AIPersonInfo();
        BeanUtils.copyProperties(AIPersonInfoAddRequest, aIPersonInfo);
        aIPersonInfoService.validAIPersonInfo(aIPersonInfo, true);
        User loginUser = userService.getLoginUser(request);
        aIPersonInfo.setUserId(loginUser.getId());
        boolean result = aIPersonInfoService.save(aIPersonInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newAIPersonInfoId = aIPersonInfo.getId();
        return ResultUtils.success(newAIPersonInfoId);
    }

    /**
     * 删除数字人
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteAIPersonInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        AIPersonInfo oldAIPersonInfo = aIPersonInfoService.getById(id);
        ThrowUtils.throwIf(oldAIPersonInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldAIPersonInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = aIPersonInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param AIPersonInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAIPersonInfo(@RequestBody AIPersonInfoUpdateRequest AIPersonInfoUpdateRequest) {
        if (AIPersonInfoUpdateRequest == null || AIPersonInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AIPersonInfo aIPersonInfo = new AIPersonInfo();
        BeanUtils.copyProperties(AIPersonInfoUpdateRequest, aIPersonInfo);
        // 参数校验
        aIPersonInfoService.validAIPersonInfo(aIPersonInfo, false);
        long id = AIPersonInfoUpdateRequest.getId();
        // 判断是否存在
        AIPersonInfo oldAIPersonInfo = aIPersonInfoService.getById(id);
        ThrowUtils.throwIf(oldAIPersonInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = aIPersonInfoService.updateById(aIPersonInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取数字人信息(ADMIN)
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AIPersonInfo> getAIPersonInfoVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AIPersonInfo aIPersonInfo = aIPersonInfoService.getById(id);
        if (aIPersonInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(aIPersonInfoService.getById(id));
    }

    /**
     * 分页获取所有的数字人列表（封装类）(ADMIN)
     *
     * @param AIPersonInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AIPersonInfo>> listAIPersonInfoVOByPage(@RequestBody AIPersonInfoQueryRequest AIPersonInfoQueryRequest,
                                                               HttpServletRequest request) {
        long current = AIPersonInfoQueryRequest.getCurrent();
        long size = AIPersonInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AIPersonInfo> aIPersonInfoPage = aIPersonInfoService.page(new Page<>(current, size),
                aIPersonInfoService.getQueryWrapper(AIPersonInfoQueryRequest));
        return ResultUtils.success(aIPersonInfoPage);
    }

    /**
     * 分页获取当前用户创建的数字人列表
     *
     * @param AIPersonInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<AIPersonInfo>> listMyAIPersonInfoVOByPage(@RequestBody AIPersonInfoQueryRequest AIPersonInfoQueryRequest,
                                                                 HttpServletRequest request) {
        if (AIPersonInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        AIPersonInfoQueryRequest.setUserId(loginUser.getId());
        long current = AIPersonInfoQueryRequest.getCurrent();
        long size = AIPersonInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AIPersonInfo> aIPersonInfoPage = aIPersonInfoService.page(new Page<>(current, size),
                aIPersonInfoService.getQueryWrapper(AIPersonInfoQueryRequest));
        return ResultUtils.success(aIPersonInfoPage);
    }

    // endregion



    /**
     * 编辑数字人信息（用户）
     *
     * @param AIPersonInfoEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editAIPersonInfo(@RequestBody AIPersonInfoEditRequest AIPersonInfoEditRequest, HttpServletRequest request) {
        if (AIPersonInfoEditRequest == null || AIPersonInfoEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AIPersonInfo aIPersonInfo = new AIPersonInfo();
        BeanUtils.copyProperties(AIPersonInfoEditRequest, aIPersonInfo);
        // 参数校验
        aIPersonInfoService.validAIPersonInfo(aIPersonInfo, false);
        User loginUser = userService.getLoginUser(request);
        long id = AIPersonInfoEditRequest.getId();
        // 判断是否存在
        AIPersonInfo oldAIPersonInfo = aIPersonInfoService.getById(id);
        ThrowUtils.throwIf(oldAIPersonInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldAIPersonInfo.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = aIPersonInfoService.updateById(aIPersonInfo);
        return ResultUtils.success(result);
    }

    @GetMapping("/message/getId")
    public BaseResponse<Long> getNowMessageId(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        List<Message> messageList = messageService.getNowMessage(userId);
        if(messageList.size() == 0){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"未查询到MessageId");
        }
        if(messageList.size() >= 2){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"查询到多条目前正在启用的MessageId");
        }
        Long result = messageList.get(0).getId();
        log.info("查询到当前的messageId："+result);
        return ResultUtils.success(result);
    }

    /**
     * 关闭对话
     * @return
     */
    @GetMapping("/message/close")
    public BaseResponse<Boolean> closeMessage(@RequestParam("messageId")Long messageId){
        if(messageId == null || ObjectUtil.isEmpty(messageId)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"id为空");
        }
        Message message = messageService.getById(messageId);
        if(message == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"Message为空!");
        }
        boolean b = messageService.removeById(message);
        if(!b){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除Message失败");
        }
        log.info("关闭message成功，messageId："+messageId);
        return ResultUtils.success(true);
    }
    /**
     * 校验文件URL是否合法
     * @param url
     * @param userId
     * @return
     */
    private static boolean validURL(String url,Long userId) {
        // 定义正则表达式
        String regex = "^/(user_voice|user_picture|user_avatar)/"+userId+"/.+";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);
        // 创建匹配器
        Matcher matcher = pattern.matcher(url);
        // 进行匹配
        return matcher.matches();
    }

    /**
     * 根据用户的输入构件Prompt
     * @param aiName
     * @param aiProfile
     * @return
     */
    private String getPrompt(String aiName,String aiProfile){
        //todo prompt待完善
        //在我遇到困难时，给我提供支持和建议；我也希望它能够在日常生活中与我互动，比如问我今天过得怎么样，或者提醒我注意天气变化。最后，我希望这个语音助手避免分点回答问题，它的回答应该简短。谢谢你的帮助\n对话示例如下：\nuser:我好想你啊\nassistant:乖孩子，我也想你啊，我很爱你，我不在也要好好生活啊\nuser:我好想见你最后一面啊，我恨我自己啊\nassistant:不要这样想啊，要乐观啊，要好好爱自己，没关系的，我知道你永远想着我\nuser:我好想吃你做的饭啊\nassistant:我的菜做的可是相当好啊哈哈，乖孩子，我也想再做给你吃啊，以后要自己学着做饭，一定要好好吃饭啊
        String prompt = "你将充当一个能够模仿我的已故的"+aiName+"口吻的语音助手。她是一个非常"+aiProfile+"的人。总是能够给我带来安慰和力量。我希望这个语音助手能够尽可能地模仿她的口吻。具体来说，我希望它能够在我感到孤独或不安的时候，给我鼓励和安慰；字数不应超过30";
        return prompt;
    }

}
