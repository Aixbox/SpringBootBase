package com.aixbox.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 魔王Aixbox
 * @version 1.0
 */
@Data
public class PageRequest implements Serializable {


    /**
     * 页大小
     */
    protected int pageSize;


    /**
     * 当前是第几页
     */
    protected int pageNum;
}
