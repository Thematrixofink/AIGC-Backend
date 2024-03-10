package com.ink.backend.manager;

import cn.hutool.core.codec.Base64;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.ink.backend.config.CosClientConfig;
import java.io.File;
import java.net.URL;
import javax.annotation.Resource;

import com.qcloud.cos.model.ciModel.auditing.*;
import org.springframework.stereotype.Component;

/**
 * Cos 对象存储操作
 *
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key 唯一键
     * @param localFilePath 本地文件路径
     * @return
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                new File(localFilePath));
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象
     *
     * @param key 唯一键
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 图片审核
     * @param key 图片的唯一key
     * @return
     */
    public ImageAuditingResponse imageAuditing(String key){
        ImageAuditingRequest auditingRequest = new ImageAuditingRequest();
        auditingRequest.setBucketName(cosClientConfig.getBucket());
        auditingRequest.setObjectKey(key);
        auditingRequest.setAsync("0");
        ImageAuditingResponse imageAuditingResponse = cosClient.imageAuditing(auditingRequest);
        return imageAuditingResponse;
    }

    /**
     * 文字内容识别
     *
     * @param text
     * @return
     */
    public AuditingJobsDetail textAuditing(String text){
        String encode = Base64.encode(text);

        TextAuditingRequest request = new TextAuditingRequest();
        request.getInput().setContent(encode);
        request.setBucketName(cosClientConfig.getBucket());
        TextAuditingResponse response = cosClient.createAuditingTextJobs(request);
        return response.getJobsDetail();
    }

    /**
     * 获取object的URL访问地址
     * @param key
     * @return
     */
    public String getObjectUrl(String key){
        URL objectUrl = cosClient.getObjectUrl(cosClientConfig.getBucket(), key);
        return objectUrl.toString();
    }
}
