package com.ink.backend.model.dto.bottle;

import lombok.Data;

/**
 * 漂流瓶评论请求
 */
@Data
public class BottleCommentRequest {

    /**
     * 瓶子的Id
     */
    private Long bottleId;

    /**
     * 此条评论的父评论,0时表示为顶级评论
     */
    private Long parentId;

    /**
     * 被回复的用户的id
     */
    private Long replyUserId;

    /**
     * 评论的内容
     */
    private String detail;

}
