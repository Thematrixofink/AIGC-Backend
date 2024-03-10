package com.ink.backend.controller;

import cn.hutool.core.util.ObjectUtil;
import com.ink.backend.common.BaseResponse;
import com.ink.backend.common.ErrorCode;
import com.ink.backend.common.ResultUtils;
import com.ink.backend.model.dto.GenRequest.GenRequest;
import com.ink.backend.model.vo.GenVideoResponse;
import com.ink.backend.model.vo.GenVoiceResponse;
import com.ink.backend.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/generate")
@Slf4j
public class AIGenerateController {

    @Resource
    private GenService genService;

    /**
     * 通过文字生成音频
     * @param genRequest
     * @param request
     * @return
     */
    @PostMapping("/voice")
    public BaseResponse<GenVoiceResponse> generateVoiceByText(@RequestBody GenRequest genRequest, HttpServletRequest request) throws ExecutionException, InterruptedException {
        GenVoiceResponse response = genService.generateVoiceByText(genRequest, request);
        if(ObjectUtil.isNull(response) || ObjectUtil.isEmpty(response)){
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(response);
    }


    /**
     * 通过文字生成音频以及视频信息
     * @param genRequest
     * @param request
     * @return
     */
    @PostMapping("/video")
    public BaseResponse<GenVideoResponse> generateVideoByText(@RequestBody GenRequest genRequest, HttpServletRequest request) throws ExecutionException, InterruptedException {
        GenVideoResponse response = genService.generateVideoByText(genRequest, request);
        if(ObjectUtil.isNull(response) || ObjectUtil.isEmpty(response)){
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(response);
    }
}
