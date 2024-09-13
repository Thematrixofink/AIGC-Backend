package com.ink.backend.model.dto.aIPersonInfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.ink.backend.common.PageRequest;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询请求
 *

 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AIPersonInfoQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 创建此数字人的用户的Id
     */
    private Long userId;

    /**
     * 给AI设定的称谓
     */
    private String aiName;


    private static final long serialVersionUID = 1L;
}
