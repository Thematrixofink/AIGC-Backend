package com.ink.backend.model.dto.aIPersonInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

/**
 * 编辑请求
 *

 */
@Data
public class AIPersonInfoEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 给AI设定的称谓
     */
    private String aiName;

    /**
     * 对模拟对象的简单描述
     */
    private String aiProfile;

    /**
     * 模拟对象的音频文件地址
     */
    private String aiVoice;

    /**
     * 模拟对象的图片（外貌）文件地址
     */
    private String aiPicture;

    private static final long serialVersionUID = 1L;
}
