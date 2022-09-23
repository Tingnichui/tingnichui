package com.tingnichui.pojo.vo;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author  Geng Hui
 * @date  2022/8/25 9:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 网关响应码
     */
    private String code;

    /**
     * 网关响应码描述
     */
    private String msg;

    /**
     * 业务响应码
     */
    private String subCode;

    /**
     * 业务响应描述
     */
    private String subMsg;

    /**
     * 响应时间，格式"yyyy-MM-dd HH:mm:ss"
     */
    private String time = DateUtil.now();

    /**
     * 追踪链,随机参数
     */
    private String trace;

    /**
     * 业务响应参数集合
     */
    private T data;


}
