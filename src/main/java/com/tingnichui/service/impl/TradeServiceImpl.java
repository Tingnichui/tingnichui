package com.tingnichui.service.impl;

import com.tingnichui.interceptor.annotation.Test;
import com.tingnichui.service.TradeService;
import com.tingnichui.util.DateUtil;
import com.tingnichui.util.StockUtil;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

@Service
public class TradeServiceImpl implements TradeService {


    @Test
    @Override
    public Object health() {
        return DateUtil.format(new Date(), "yyyy-MM-dd HH-mm-ss:SSS");
    }

    @Override
    public Object buy() throws Exception {
        System.err.println(StockUtil.buy(Arrays.asList("601288", "3.123456", "300")));

        return DateUtil.format(new Date(), "yyyy-MM-dd HH-mm-ss:SSS");
    }

    @Override
    public Object sell() throws Exception {
        System.err.println(StockUtil.sell(Arrays.asList("601288", "3.21", "300")));

        return DateUtil.format(new Date(), "yyyy-MM-dd HH-mm-ss:SSS");
    }

    @Override
    public Object cancel() {
        System.err.println(StockUtil.cancel());

        return DateUtil.format(new Date(), "yyyy-MM-dd HH-mm-ss:SSS");
    }

    @Override
    public Object getBalance() throws Exception {
        Map<String, String> balance = StockUtil.getBalance();

        return balance;
    }

    @Override
    public Object getBalanceDetail() throws Exception {
        System.err.println(StockUtil.getBalanceDetail());

        return DateUtil.format(new Date(), "yyyy-MM-dd HH-mm-ss:SSS");
    }
}
