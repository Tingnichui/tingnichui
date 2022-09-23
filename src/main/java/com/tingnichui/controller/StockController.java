package com.tingnichui.controller;

import com.tingnichui.pojo.vo.Result;
import com.tingnichui.service.StockService;
import com.tingnichui.util.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Geng Hui
 * @version 1.0
 * @date 2022/8/10 21:34
 */
@Api(tags = "股票自动交易")
@RestController
@RequestMapping("/stock")
public class StockController {

    @Resource
    private StockService stockService;

    @ApiOperation("测试服务是否正常运行")
    @GetMapping("/health")
    public Result health() {
        return ResultGenerator.success("HEALTH");
    }

    @ApiOperation("从东方财富保存股票日线记录")
    @PostMapping("/saveDailyRecord4EastMoney")
    public Result saveDailyRecord4EastMoney() {
        return stockService.saveDailyRecord4EastMoney();
    }

    @ApiOperation("更新股票基本信息")
    @PutMapping("/updateStockInfo")
    public Result updateStockInfo() {
        return stockService.updateStockInfo();
    }

    @ApiOperation("更新股票日线（均线、涨幅）")
    @PutMapping("/updateDailyIndexAverageAndIncrease")
    public Result updateDailyIndexAverageAndIncrease() {
        return stockService.updateDailyIndexAverageAndIncrease();
    }

    @ApiOperation("分页获取交易交易记录")
    @GetMapping("/listStockTrade")
    public Result listStockTrade() {
        return stockService.listStockTrade();
    }

}
