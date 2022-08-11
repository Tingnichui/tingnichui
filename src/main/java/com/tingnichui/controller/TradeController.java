package com.tingnichui.controller;

import cn.hutool.core.date.DateUtil;
import com.tingnichui.service.TradeService;
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
    public Object health(){
        return DateUtil.now();
    }

    @PostMapping("buy")
    public Object buy() throws Exception {
        return tradeService.buy();
    }

    @PostMapping("sell")
    public Object sell() throws Exception {
        return tradeService.sell();
    }

    @PostMapping("cancel")
    public Object cancel(){
        return tradeService.cancel();
    }

    @PostMapping("getBalance")
    public Object getBalance() throws Exception {
        return tradeService.getBalance();
    }

    @PostMapping("getBalanceDetail")
    public Object getBalanceDetail() throws Exception {
        return tradeService.getBalanceDetail();
    }
}
