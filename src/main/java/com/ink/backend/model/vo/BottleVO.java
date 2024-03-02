package com.ink.backend.model.vo;

import com.ink.backend.model.entity.Bottle;
import com.ink.backend.model.entity.Bottlecomment;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class BottleVO implements Serializable {
    /**
     * 瓶子的Id
     */
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
     * 评论
     */
    private List<BottleCommentVO> comments;

    /**
     * 包装类转对象
     *
     * @param bottleVO
     * @return
     */
    public static Bottle voToObj(BottleVO bottleVO) {
        if (bottleVO == null) {
            return null;
        }
        Bottle post = new Bottle();
        BeanUtils.copyProperties(bottleVO, post);
        return post;
    }

    /**
     * 对象转包装类
     *
     * @param bottle
     * @return
     */
    public static BottleVO objToVo(Bottle bottle) {
        if (bottle == null) {
            return null;
        }
        BottleVO bottleVO = new BottleVO();
        BeanUtils.copyProperties(bottle, bottleVO);
        return bottleVO;
    }
}
