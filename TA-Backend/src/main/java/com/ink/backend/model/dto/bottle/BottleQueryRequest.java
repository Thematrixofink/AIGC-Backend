package com.ink.backend.model.dto.bottle;

import com.ink.backend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class BottleQueryRequest extends PageRequest implements Serializable {
    /**
     * id
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
     * 是否被打捞
     */
    private Integer isPick;

    /**
     * 除去用户的id。防止打捞上自己的瓶子
     */
    private Long notId;



}
