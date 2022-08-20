package com.tingnichui;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tingnichui.dao.DailyIndexMapper;
import com.tingnichui.dao.StockInfoMapper;
import com.tingnichui.dao.StockTradeStrategyMapper;
import com.tingnichui.dao.UserMapper;
import com.tingnichui.pojo.po.StockTradeStrategy;
import com.tingnichui.service.StockService;
import com.tingnichui.service.impl.StockServiceImpl;
import com.tingnichui.task.ScheduledTask;
import com.tingnichui.util.BaiduUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
class TingnichuiApplicationTests {

    @Resource
    private BaiduUtil baiduUtil;

    @Resource
    private UserMapper userMapper;

    @Resource
    private StockTradeStrategyMapper stockTradeStrategyMapper;

    @Resource
    private StockInfoMapper stockInfoMapper;


    @Resource
    private StockService stockService;

    @Resource
    private StockServiceImpl stockServiceImpl;

    @Resource
    private DailyIndexMapper dailyIndexMapper;

    @Resource
    private ScheduledTask scheduledTask;

    @Test
    void monitorTest() {
        stockService.monitorStock();
    }

    @Test
    void TradeRule() {

        // 买点
//        StockTradeStrategy buyStrategy = new StockTradeStrategy();
//        buyStrategy.setStockCode("601288");
//        buyStrategy.setStrategyType("buy");
//        buyStrategy.setIsWork(true);
//        buyStrategy.setMonitorType("closePrice");
//        buyStrategy.setCompareMethod("lt");
//        buyStrategy.setTargetType("preClosePrice");
//        buyStrategy.setTargetCalculationType("percentage");
//        buyStrategy.setTargetValue(new BigDecimal("-2"));
//        buyStrategy.setTragetAmount(1);
//        stockTradeStrategyMapper.insert(buyStrategy);
//        stockTradeStrategyMapper.update(buyStrategy,new LambdaQueryWrapper<StockTradeStrategy>().eq(StockTradeStrategy::getStockCode,buyStrategy.getStockCode()));

        // 卖点
        StockTradeStrategy sellStrategy = new StockTradeStrategy();
        sellStrategy.setStockCode("601288");
        sellStrategy.setStrategyType("sell");
        sellStrategy.setIsWork(true);
        sellStrategy.setMonitorType("closePrice");
        sellStrategy.setCompareMethod("gt");
        sellStrategy.setTargetType("buyPrice");
        sellStrategy.setTargetCalculationType("percentage");
        sellStrategy.setTargetValue(new BigDecimal("1"));
        sellStrategy.setTragetAmount(1);
//        stockTradeStrategyMapper.insert(sellStrategy);
        stockTradeStrategyMapper.update(sellStrategy,new LambdaQueryWrapper<StockTradeStrategy>().eq(StockTradeStrategy::getStockCode,sellStrategy.getStockCode()));

    }
}
