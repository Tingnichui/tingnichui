package com.tingnichui.controller;

import com.tingnichui.pojo.vo.Result;
import com.tingnichui.service.TradeService;
import com.tingnichui.util.ResultGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("api/trade")
public class TradeController {

    @Resource
    private TradeService tradeService;

    @GetMapping("health")
    public Result health() {
        return ResultGenerator.genSuccessResult("HEALTH");
    }

    @PostMapping("buy")
    public Result buy() throws Exception {
        return tradeService.buy();
    }

    @PostMapping("sell")
    public Result sell() throws Exception {
        return tradeService.sell();
    }

    @PostMapping("cancel")
    public Result cancel() {
        return tradeService.cancel();
    }

    @PostMapping("getBalance")
    public Result getBalance() throws Exception {
        return tradeService.getBalance();
    }

    @PostMapping("getBalanceDetail")
    public Result getBalanceDetail() throws Exception {
        return tradeService.getBalanceDetail();
    }
}
