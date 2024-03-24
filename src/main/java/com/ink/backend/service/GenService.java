package com.ink.backend.service;

import com.ink.backend.model.dto.GenRequest.GenRequest;
import com.ink.backend.model.dto.GenRequest.GenVideoResponse;
import com.ink.backend.model.dto.GenRequest.GenVoiceResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * 生成Service
 */
public interface GenService {
    /**
     * 根据文字生成音频
     * @param genRequest
     * @param request
     * @return
     */
    GenVoiceResponse generateVoiceByText(GenRequest genRequest, HttpServletRequest request);

    /**
     * 根据文字生成视频
     * @param genRequest
     * @param request
     * @return
     */
    GenVideoResponse generateVideoByText(GenRequest genRequest, HttpServletRequest request);
}
