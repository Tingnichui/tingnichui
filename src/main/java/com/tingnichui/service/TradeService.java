package com.tingnichui.service;

import com.tingnichui.pojo.vo.Result;

/**
 * @author  Geng Hui
 * @date  2022/8/25 9:33
 */
public interface TradeService {

    Result buy() throws Exception;

    Result sell() throws Exception;

    Result cancel();

    Result getBalance() throws Exception;

    Result getBalanceDetail() throws Exception;
}
