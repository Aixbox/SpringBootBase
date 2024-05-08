package com.aixbox.usercenter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 魔王Aixbox
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String id;
    private String username;
    private String password;
    private String nickName;
    private String email;
    private String note;
    private Integer status;
}
