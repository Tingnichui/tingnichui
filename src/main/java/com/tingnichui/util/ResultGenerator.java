package com.tingnichui.util;



import com.tingnichui.common.ResultCode;
import com.tingnichui.pojo.vo.Result;
import org.apache.commons.lang3.StringUtils;


/**
 * @author  Geng Hui
 * @date  2022/8/25 9:33
 */
public class ResultGenerator {
    private static final String DEFAULT_SUCCESS_MESSAGE = "SUCCESS";
    private static final String DEFAULT_FAIL_MESSAGE = "FAIL";
    private static final String RESULT_CODE_SUCCESS = "0000";
    private static final String RESULT_CODE_FAIL = "9999";


    private ResultGenerator() {
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.setCode(ResultCode.OK.getCode());
        result.setMsg(ResultCode.OK.getMessage());
        result.setSubCode(RESULT_CODE_SUCCESS);
        result.setSubMsg(DEFAULT_SUCCESS_MESSAGE);
        return result;
    }

    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<T>();
        result.setCode(ResultCode.OK.getCode());
        result.setMsg(ResultCode.OK.getMessage());
        result.setSubCode(RESULT_CODE_SUCCESS);
        result.setSubMsg(message);
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<T>();
        result.setCode(ResultCode.OK.getCode());
        result.setMsg(ResultCode.OK.getMessage());
        result.setSubCode(RESULT_CODE_SUCCESS);
        result.setSubMsg(DEFAULT_SUCCESS_MESSAGE);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<T>();
        result.setCode(ResultCode.OK.getCode());
        result.setMsg(ResultCode.OK.getMessage());
        result.setSubCode(RESULT_CODE_FAIL);
        if (StringUtils.isBlank(message)) {
            result.setSubMsg(DEFAULT_FAIL_MESSAGE);
        } else {
            result.setSubMsg(message);
        }
        return result;
    }

    public static <T> Result<T> error() {
        Result<T> result = new Result<T>();
        result.setCode(ResultCode.SERVICE_ERROR.getCode());
        result.setMsg(ResultCode.SERVICE_ERROR.getMessage());
        return result;
    }

}
