package com.tingnichui.controller;

import com.tingnichui.pojo.vo.Result;
import com.tingnichui.service.StockService;
import com.tingnichui.util.ResultGenerator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Geng Hui
 * @version 1.0
 * @date 2022/8/10 21:34
 */
@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Resource
    private StockService stockService;

    @RequestMapping("/health")
    public Result health() {
        return ResultGenerator.genSuccessResult("HEALTH");
    }

    @RequestMapping("/saveDailyRecord4EastMoney")
    public Result saveDailyRecord4EastMoney() {
        return stockService.saveDailyRecord4EastMoney();
    }

    @RequestMapping("/updateStockInfo")
    public Result updateStockInfo() {
        return stockService.updateStockInfo();
    }

}
