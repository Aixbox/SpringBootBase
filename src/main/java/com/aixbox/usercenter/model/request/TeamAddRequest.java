package com.aixbox.usercenter.model.request;

import lombok.Data;

import java.util.Date;

/**
 * @author 魔王Aixbox
 * @version 1.0
 */
@Data
public class TeamAddRequest {

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
     * 0公开 1私有 2加密
     */
    private Integer status;


    /**
     * 密码
     */
    private String password;





}
