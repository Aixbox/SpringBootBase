package com.aixbox.usercenter.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 魔王Aixbox
 * @version 1.0
 */
@Data
public class TeamUserVO implements Serializable {



    /**
     * id
     */
    private long id;

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
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0-公开 1-私有 2-加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 队伍创建人用户信息
     */
    private UserVO createdUser;

    /**
     * 是否加入队伍
     */
    private boolean hasJoin = false;

    /**
     * 加入队伍人数
     */
    private Integer hasJoinNum;


}
