package com.tingnichui.service;

import com.tingnichui.pojo.vo.Result;

import java.math.BigDecimal;

/**
 * @author  Geng Hui
 * @date  2022/8/25 9:32
 */
public interface CashService {
    Result save(BigDecimal tradeAmount, BigDecimal actualAmount);
}
