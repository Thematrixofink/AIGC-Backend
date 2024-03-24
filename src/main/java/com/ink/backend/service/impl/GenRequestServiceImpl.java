package com.ink.backend.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.ink.backend.constant.FileConstant;
import com.ink.backend.model.dto.networkRequest.RemoteGenRequest;
import com.ink.backend.model.dto.networkRequest.UploadRequest;
import com.ink.backend.service.GenRequestService;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class GenRequestServiceImpl implements GenRequestService {

    @Override
    public HttpResponse upload(Long id, String wavUrl, String imgUrl) {
        UploadRequest uploadRequest = new UploadRequest();
        if(StrUtil.isBlank(imgUrl) || StrUtil.isEmpty(imgUrl)){
            uploadRequest.setImg("null");
        }else{
            uploadRequest.setImg(imgUrl);
        }
        uploadRequest.setId(String.valueOf(id));
        uploadRequest.setWav(wavUrl);
        HttpResponse response = HttpRequest.post(FileConstant.MODEL_URL +"/upload")
                .body(JSONUtil.toJsonStr(uploadRequest))
                .timeout(600000)
                .setConnectionTimeout(600000)
                .execute();
        return response;
    }

    @Override
    public HttpResponse genVoice(Long id, String txt) {
        RemoteGenRequest remoteGenRequest = new RemoteGenRequest();
        remoteGenRequest.setId(String.valueOf(id));
        remoteGenRequest.setTxt(txt);
        HttpResponse response = HttpRequest.get(FileConstant.MODEL_URL + "/generator/voice")
                .body(JSONUtil.toJsonStr(remoteGenRequest))
                .timeout(240000)
                .setConnectionTimeout(240000)
                .execute();
        System.out.println("==============================生成音频的响应为===============================");
        System.out.println(response);
        return response;
    }

    @Override
    public HttpResponse genVideo(Long id, String txt) {
        RemoteGenRequest remoteGenRequest = new RemoteGenRequest();
        remoteGenRequest.setId(String.valueOf(id));
        remoteGenRequest.setTxt(txt);
        HttpResponse response = HttpRequest.get(FileConstant.MODEL_URL + "/generator/video")
                .body(JSONUtil.toJsonStr(remoteGenRequest))
                .timeout(240000)
                .setConnectionTimeout(240000)
                .execute();
        System.out.println("==============================生成视频的响应为===============================");
        System.out.println(response);
        return response;
    }


}
