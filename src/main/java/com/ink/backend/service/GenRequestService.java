package com.ink.backend.service;

import cn.hutool.http.HttpResponse;

/**
 * 向模型服务端发送请求Service
 */
public interface GenRequestService {

    /**
     * 上传文件
     * @param id
     * @param wavUrl
     * @param imgUrl
     * @return
     */
    HttpResponse upload(Long id,String wavUrl,String imgUrl);

    /**
     * 生成音频
     * @param id
     * @param txt
     * @return
     */
    HttpResponse genVoice(Long id,String txt);

    /**
     * 生成视频
     * @param id
     * @param txt
     * @return
     */
    HttpResponse genVideo(Long id,String txt);
}
