package com.ink.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 漂流瓶评论表
 * @TableName bottlecomment
 */

@TableName(value ="bottlecomment")
@Data
public class Bottlecomment implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 瓶子的Id
     */
    private Long bottleId;

    /**
     * 此条评论的父评论,0时表示为顶级评论
     */
    private Long parentId;

    /**
     * 发布此条评论的id
     */
    private Long userId;

    /**
     * 被回复的用户的id
     */
    private Long replyUserId;

    /**
     * 评论的内容
     */
    private String detail;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
