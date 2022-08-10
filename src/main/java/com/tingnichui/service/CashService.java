package com.tingnichui.service;

import com.tingnichui.entity.vo.Result;

import java.math.BigDecimal;

public interface CashService {
    Result save(BigDecimal tradeAmount, BigDecimal actualAmount);
}
