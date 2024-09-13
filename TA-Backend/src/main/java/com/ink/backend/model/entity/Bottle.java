package com.ink.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 漂流瓶表
 * @TableName bottle
 */
@TableName(value ="bottle")
@Data
public class Bottle implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 发送此瓶子的用户的Id
     */
    private Long userId;

    /**
     * 捞起此瓶子的用户的Id
     */
    private Long pickUserId;

    /**
     * 漂流瓶内容
     */
    private String content;

    /**
     * 是否被打捞
     */
    private Integer isPick;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
