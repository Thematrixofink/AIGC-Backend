package com.ink.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 数字人信息表
 * @TableName aipersoninfo
 */
@TableName(value ="aipersoninfo")
@Data
public class AIPersonInfo implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建此数字人的用户的Id
     */
    private Long userId;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 生成状态
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
