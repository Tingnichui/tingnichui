package com.tingnichui.controller;

import com.tingnichui.pojo.vo.Result;
import com.tingnichui.service.StockService;
import com.tingnichui.util.ResultGenerator;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/health")
    public Result health() {
        return ResultGenerator.genSuccessResult("HEALTH");
    }

    @PostMapping("/saveDailyRecord4EastMoney")
    public Result saveDailyRecord4EastMoney() {
        return stockService.saveDailyRecord4EastMoney();
    }

    @PutMapping("/updateStockInfo")
    public Result updateStockInfo() {
        return stockService.updateStockInfo();
    }

    @PutMapping("/updateDailyIndexAverage")
    public Result updateDailyIndexAverage() {
        return stockService.updateDailyIndexAverage();
    }

    @GetMapping("/listStockTrade")
    public Result listStockTrade() {
        return stockService.listStockTrade();
    }

}
