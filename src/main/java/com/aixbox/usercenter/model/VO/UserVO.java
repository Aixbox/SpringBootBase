package com.aixbox.usercenter.model.VO;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 魔王Aixbox
 * @version 1.0
 */
@Data
public class UserVO implements Serializable {



    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话号码
     */
    private String phone;



    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 0-正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;



    /**
     * 用户角色， 普通用户 - 0  ，管理员 - 1
     */
    private Integer userRole;

    /**
     * 标签json列表
     */
    private String tags;

    /**
     * 个人简介
     */
    private String profile;






}
