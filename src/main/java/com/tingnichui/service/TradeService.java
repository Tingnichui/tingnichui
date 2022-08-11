package com.tingnichui.service;

import com.tingnichui.pojo.vo.Result;

public interface TradeService {

    Result buy() throws Exception;

    Result sell() throws Exception;

    Result cancel();

    Result getBalance() throws Exception;

    Result getBalanceDetail() throws Exception;
}
