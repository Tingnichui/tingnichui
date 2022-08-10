package com.tingnichui.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {


    private static final long serialVersionUID = 1L;

    private String resultCode;

    private String message;

    private T data;

}
