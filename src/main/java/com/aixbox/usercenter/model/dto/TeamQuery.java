package com.aixbox.usercenter.model.dto;


import com.aixbox.usercenter.common.PageRequest;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author 魔王Aixbox
 * @version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {


    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0公开 1私有 2加密
     */
    private Integer status;

    /**
     * id列表
     */
    private List<Long> idList;





}
