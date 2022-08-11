package com.tingnichui.controller;

import cn.hutool.core.date.DateUtil;
import com.tingnichui.pojo.vo.Result;
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

    @RequestMapping("/health")
    public String health() {
        return DateUtil.now();
    }

    @RequestMapping("/updateStock4xueqiu")
    public Result updateStock4xueqiu() {
        return stockService.updateStock4xueqiu();
    }

    @RequestMapping("/saveDailyRecord4xueqiu")
    public Result saveDailyRecord4xueqiu() {
        return stockService.saveDailyRecord4xueqiu();
    }

}
