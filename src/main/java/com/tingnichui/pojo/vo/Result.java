package com.tingnichui.pojo.vo;

import cn.hutool.core.date.DateUtil;
import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {


    private static final long serialVersionUID = 1L;

    private String code;

    private String message;

    private T data;

    private String time = DateUtil.now();

}
