package com.ink.backend.controller;

import cn.hutool.core.io.FileUtil;
import com.ink.backend.common.BaseResponse;
import com.ink.backend.common.ErrorCode;
import com.ink.backend.common.ResultUtils;
import com.ink.backend.constant.FileConstant;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.manager.CosManager;
import com.ink.backend.model.dto.file.UploadFileRequest;
import com.ink.backend.model.entity.User;
import com.ink.backend.model.enums.FileUploadBizEnum;
import com.ink.backend.service.UserService;
import java.io.File;
import java.util.Arrays;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
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
            // 返回可访问地址
            return ResultUtils.success(FileConstant.COS_HOST + filepath);
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
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
        //如果用户传的文件是要模拟的图片
        if (FileUploadBizEnum.USER_PICTURE.equals(fileUploadBizEnum)){
            if (fileSize > 5 * ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 5M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
        //如果用户传的文件是音频（视频）文件
        if(FileUploadBizEnum.USER_VOICE.equals(fileUploadBizEnum)){
            if (fileSize > 50 * ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "音（视）频文件大小不能超过 50M");
            }
            //如果是音频文件
            if (Arrays.asList("mp3", "wav", "wma", "flac", "ra","pcm").contains(fileSuffix)) {

            }
            //如果是视频文件，那么返回路径为音频文件的路径
            //这里使用了腾讯云COS的自动人声提取
            else if (Arrays.asList("avi", "flv", "mov", "wmv", "mkv","mp4").contains(fileSuffix)) {
                return filePath.substring(0, filePath.length() - 4) + "_vocal.mp3";
            }
            else{
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
        return filePath;
    }
}
