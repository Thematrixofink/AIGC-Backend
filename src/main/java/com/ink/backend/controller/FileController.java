package com.ink.backend.controller;

import cn.hutool.core.io.FileUtil;
import com.ink.backend.common.BaseResponse;
import com.ink.backend.common.ErrorCode;
import com.ink.backend.common.ResultUtils;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.manager.CosManager;
import com.ink.backend.model.dto.file.UploadFileRequest;
import com.ink.backend.model.entity.User;
import com.ink.backend.model.enums.FileUploadBizEnum;
import com.ink.backend.service.UserService;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.qcloud.cos.model.ciModel.auditing.ImageAuditingResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口
 *

 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    private final List<String> PICTURE_SUFFIX = Arrays.asList("jpeg", "jpg", "svg", "png", "webp");

    private final List<String> AUDIO_SUFFIX   = Arrays.asList("mp3", "wav", "wma", "flac", "ra","pcm");

    private final List<String> VIDEO_SUFFIX   = Arrays.asList("avi", "flv", "mov", "wmv", "mkv","mp4");

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        System.out.println(multipartFile);
        System.out.println(uploadFileRequest.getBiz());
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        String tempPath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        File file = null;
        //对文件进行校验，以及格式转换
        String filepath = validFile(multipartFile, fileUploadBizEnum,tempPath);
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            boolean isValid = validContent(filepath);
            if(!isValid){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"图片涉及敏感信息!");
            }
            // 返回可访问地址
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 校验文件的内容是否合法
     * @param filepath
     * @return
     */
    private boolean validContent(String filepath) {
        String[] split = filepath.split("\\.");
        if(split.length <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"COS文件路径错误!");
        }
        //文件的后缀
        String fileSuffix = split[split.length - 1];
        if(PICTURE_SUFFIX.contains(fileSuffix)){
            ImageAuditingResponse response = cosManager.imageAuditing(filepath);
            String result = response.getResult();
            if(result.equals("1")){
                return false;
            }else{
                return true;
            }
        }
        return true;
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private String validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum,String filePath) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //1024 * 1024L 是 1M
        final long ONE_M =1024 * 1024L;

        //如果传的文件是头像的话，那么对头像的合法性进行检验
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > 5 * ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 5M");
            }
            if (!PICTURE_SUFFIX.contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
        //如果用户传的文件是要模拟的图片
        if (FileUploadBizEnum.USER_PICTURE.equals(fileUploadBizEnum)){
            if (fileSize > 5 * ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 5M");
            }
            if (!PICTURE_SUFFIX.contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
        //如果用户传的文件是音频（视频）文件
        if(FileUploadBizEnum.USER_VOICE.equals(fileUploadBizEnum)){
            if (fileSize > 50 * ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "音（视）频文件大小不能超过 50M");
            }
            //如果是音频文件
            if (AUDIO_SUFFIX.contains(fileSuffix)) {

            }
            //如果是视频文件，那么返回路径为音频文件的路径
            //这里使用了腾讯云COS的自动人声提取
            else if (VIDEO_SUFFIX.contains(fileSuffix)) {
                return filePath.substring(0, filePath.length() - 4) + "_vocal.flac";
            }
            else{
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
        return filePath;
    }
}
