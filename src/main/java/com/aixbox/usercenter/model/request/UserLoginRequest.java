package com.aixbox.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 魔王Aixbox
 * @version 1.0
 */
@Data
public class UserLoginRequest implements Serializable {
    private String userAccount;
    private String userPassword;

}
