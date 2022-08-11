package com.tingnichui.service.impl;

import com.tingnichui.pojo.vo.Result;
import com.tingnichui.service.TradeService;
import com.tingnichui.util.ResultGenerator;
import com.tingnichui.util.StockTradeUtil;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service
public class TradeServiceImpl implements TradeService {


    @Override
    public Result buy() throws Exception {
        System.err.println(StockTradeUtil.buy(Arrays.asList("601288", "3.123456", "300")));

        return ResultGenerator.genSuccessResult();
    }

    @Override
    public Result sell() throws Exception {
        System.err.println(StockTradeUtil.sell(Arrays.asList("601288", "3.21", "300")));

        return ResultGenerator.genSuccessResult();
    }

    @Override
    public Result cancel() {
        System.err.println(StockTradeUtil.cancel());

        return ResultGenerator.genSuccessResult();
    }

    @Override
    public Result getBalance() throws Exception {
        Map<String, String> balance = StockTradeUtil.getBalance();

        return ResultGenerator.genSuccessResult(balance);
    }

    @Override
    public Result getBalanceDetail() throws Exception {
        System.err.println(StockTradeUtil.getBalanceDetail());

        return ResultGenerator.genSuccessResult();
    }
}
