package com.ink.backend.controller;

import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.ink.backend.annotation.AuthCheck;
import com.ink.backend.common.BaseResponse;
import com.ink.backend.common.DeleteRequest;
import com.ink.backend.common.ErrorCode;
import com.ink.backend.common.ResultUtils;
import com.ink.backend.constant.UserConstant;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.exception.ThrowUtils;
import com.ink.backend.manager.CosManager;
import com.ink.backend.model.dto.GenRequest.GenAIRequest;
import com.ink.backend.model.dto.aIPersonInfo.AIPersonInfoAddRequest;
import com.ink.backend.model.dto.aIPersonInfo.AIPersonInfoEditRequest;
import com.ink.backend.model.dto.aIPersonInfo.AIPersonInfoQueryRequest;
import com.ink.backend.model.dto.aIPersonInfo.AIPersonInfoUpdateRequest;
import com.ink.backend.model.entity.AIPersonInfo;
import com.ink.backend.model.entity.Message;
import com.ink.backend.model.entity.User;
import com.ink.backend.service.AIPersonInfoService;
import com.ink.backend.service.MessageService;
import com.ink.backend.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

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
    private CosManager cosManager;

    @Resource
    private YuCongMingClient yuCongMingClient;

    private final static Gson GSON = new Gson();


    /**
     * 生成数字人
     * @param genAIRequest
     * @param request
     * @return
     */
    @PostMapping("/preGenerator")
    public BaseResponse<String> preGenerator(@RequestBody GenAIRequest genAIRequest, HttpServletRequest request) {
        String aiName = genAIRequest.getAiName();
        String aiProfile = genAIRequest.getAiProfile();
        String aiVoice = genAIRequest.getAiVoice();
        String aiPicture = genAIRequest.getAiPicture();
        //1.对各项属性进行校验
        if(StringUtils.isBlank(aiName) || StringUtils.isBlank(aiProfile) || StringUtils.isBlank(aiVoice)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        boolean voiceValid = validURL(aiVoice);
        boolean pictureValid = true;
        if(StringUtils.isNotBlank(aiPicture)){
             pictureValid = validURL(aiPicture);
        }
        if(!voiceValid || !pictureValid){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件路径非法");
        }
        //2.创建AIperson以及message到数据库
        Long userId = userService.getLoginUser(request).getId();
        AIPersonInfo aiPersonInfo = new AIPersonInfo();
        aiPersonInfo.setUserId(userId);
        aiPersonInfo.setAiName(aiName);
        aiPersonInfo.setAiProfile(aiProfile);
        aiPersonInfo.setAiVoice(aiVoice);
        aiPersonInfo.setAiPicture(aiPicture);
        boolean isSaveAiInfo = aIPersonInfoService.save(aiPersonInfo);
        if(!isSaveAiInfo){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        Message message = new Message();
        message.setAiPersonId(aiPersonInfo.getId());
        message.setUserId(userId);
        //2.对用户的输入进行一系列前置修改
        //todo 更加合理的进行前置准备
        StringBuilder userInputBuilder = new StringBuilder();
        userInputBuilder.append("我希望你扮演我离世的"+aiName+"来和我对话。"+aiProfile);
        userInputBuilder.append("我现在很想念他，希望你能扮演他来安慰安慰我，和我对话。");
        String userInput = userInputBuilder.toString();

        //3.把aiVoice以及aiPicture的连接发送给视频生成大模型
        //todo 发送给视频模型,发送id、视频连接、音频链接
        Long talkId = aiPersonInfo.getId();
        //HttpUtil.post()

        //4.发送给AI大模型
        //todo 发送给文本模型 ,替换为自己的
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1748617589749583874L);
        devChatRequest.setMessage(userInput);
        com.yupi.yucongming.dev.common.BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        String result = response.getData().getContent();
        System.out.println(result);

        //5.将AI返回的消息保存下来
        StringBuilder mes = new StringBuilder();
        //使用&&分割两条消息
        mes.append("user:"+userInput+"&&"+"ai:"+result+"&&");
        message.setContent(mes.toString());
        boolean isSaveMes = messageService.save(message);
        if(!isSaveMes){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //对话的唯一id，返回给前端，之后前端请求必须携带着这个对话的id（因为API的无状态性)
        Long messageId = message.getId();
        return ResultUtils.success(messageId.toString());
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
     * 删除
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
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
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
     * 分页获取列表（封装类）
     *
     * @param AIPersonInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
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
     * 分页获取当前用户创建的资源列表
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
     * 编辑（用户）
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

    private static boolean validURL(String url) {
        // 定义正则表达式
        String regex = "^https://original-1317028174\\.cos\\.ap-beijing\\.myqcloud\\.com/(user_voice|user_picture|user_avatar)/.+";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);
        // 创建匹配器
        Matcher matcher = pattern.matcher(url);
        // 进行匹配
        return matcher.matches();
    }

}
