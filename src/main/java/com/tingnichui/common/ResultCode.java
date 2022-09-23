package com.tingnichui.common;

/**
 * @author Geng Hui
 * @date 2022/9/23 22:25
 */
public enum ResultCode {
    /**
     * 一切OK
     */
    OK("00000","调用服务正常"),
    SERVICE_ERROR("B0001","系统执行出错"),
    SERVICE_TIMEOUT("B0100","系统执行超时");

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String code;
    private String message;
}
