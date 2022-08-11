package com.tingnichui.controller;

import com.tingnichui.entity.vo.Result;
import com.tingnichui.service.StockService;
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

    @RequestMapping("updateStock")
    public Result updateStockd() {
        return stockService.updateStock();
    }

    @RequestMapping("run")
    public Result run() {
        return stockService.run();
    }

}
