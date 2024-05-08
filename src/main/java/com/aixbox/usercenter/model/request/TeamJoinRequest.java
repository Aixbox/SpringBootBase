package com.aixbox.usercenter.model.request;

import lombok.Data;

/**
 * @author 魔王Aixbox
 * @version 1.0
 */
@Data
public class TeamJoinRequest {

    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;

}
