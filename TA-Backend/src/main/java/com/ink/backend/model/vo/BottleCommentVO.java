package com.ink.backend.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 漂流瓶评论的返回
 */
@Data
public class BottleCommentVO {
    /**
     * 评论的id
     */
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
     * 发布此评论的用户名
     */
    private String userName;

    /**
     * 发布此评论的用户头像地址
     */
    private String userAvatar;

    /**
     * 被回复的用户的id
     */
    private Long replyUserId;

    /**
     * 被回复的用户的用户名
     */
    private String replyUserName;

    /**
     * 被回复的用户的用户头像地址
     */
    private String replyUserAvatar;

    /**
     * 评论的内容
     */
    private String detail;

    /**
     * 创建时间
     */
    private Date createTime;
}
