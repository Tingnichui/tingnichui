package com.tingnichui.service.impl;

import com.tingnichui.pojo.vo.Result;
import com.tingnichui.service.TradeService;
import com.tingnichui.util.ResultGenerator;
import com.tingnichui.util.StockUtil;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service
public class TradeServiceImpl implements TradeService {


    @Override
    public Result buy() throws Exception {
        System.err.println(StockUtil.buy(Arrays.asList("601288", "3.123456", "300")));

        return ResultGenerator.genSuccessResult();
    }

    @Override
    public Result sell() throws Exception {
        System.err.println(StockUtil.sell(Arrays.asList("601288", "3.21", "300")));

        return ResultGenerator.genSuccessResult();
    }

    @Override
    public Result cancel() {
        System.err.println(StockUtil.cancel());

        return ResultGenerator.genSuccessResult();
    }

    @Override
    public Result getBalance() throws Exception {
        Map<String, String> balance = StockUtil.getBalance();

        return ResultGenerator.genSuccessResult(balance);
    }

    @Override
    public Result getBalanceDetail() throws Exception {
        System.err.println(StockUtil.getBalanceDetail());

        return ResultGenerator.genSuccessResult();
    }
}
