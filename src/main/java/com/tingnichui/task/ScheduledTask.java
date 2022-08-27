package com.tingnichui.task;

import cn.hutool.core.util.IdUtil;
import com.tingnichui.service.StockService;
import com.tingnichui.util.DingdingUtil;
import com.tingnichui.util.StockUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Geng Hui
 * @date 2022/8/14 16:58
 */
@Slf4j
@Component
public class ScheduledTask {

    @Resource
    private StockService stockService;

    @Resource
    private StockUtil stockUtil;


    /**
     * 更新股票信息
     */
    @Scheduled(cron = "0 0 9 ? * MON-FRI")
    public void updateStockInfoTask() {
        MDC.put("processId", IdUtil.simpleUUID());
        boolean isBusinessDate = stockUtil.isStockTradeDate(new Date());
        log.info("开始更新股票信息" + isBusinessDate);
        if (!isBusinessDate) {
            log.info("非工作日-结束更新股票信息");
            return;
        }
        try {
            stockService.updateStockInfo();
        } catch (Exception e) {
            DingdingUtil.sendMsg("更新股票信息异常!");
            log.error("更新股票信息异常", e);
        }
        log.info("结束更新股票信息");
    }


    /**
     * 保存日线数据
     */
    @Scheduled(cron = "0 0 17,18,19 ? * MON-FRI")
    public void saveDailyIndexTask() {
        MDC.put("processId", IdUtil.simpleUUID());
        boolean isBusinessDate = stockUtil.isStockTradeDate(new Date());
        log.info("开始保存日线数据" + isBusinessDate);
        if (!isBusinessDate) {
            return;
        }
        try {
            stockService.saveDailyRecord4EastMoney();
        } catch (Exception e) {
            DingdingUtil.sendMsg("保存日线数据异常!");
            log.error("保存日线数据异常", e);
        }
        log.info("结束保存日线数据");
    }

    /**
     * 更新股票均线
     */
    @Scheduled(cron = "0 0 20,21,22 ? * MON-FRI")
    public void updateDailyIndexAverageTask() {
        MDC.put("processId", IdUtil.simpleUUID());
        boolean isBusinessDate = stockUtil.isStockTradeDate(new Date());
        log.info("开始更新股票均线" + isBusinessDate);
        if (!isBusinessDate) {
            return;
        }
        try {
            stockService.updateDailyIndexAverage();
        } catch (Exception e) {
            DingdingUtil.sendMsg("保存日线数据异常!");
            log.error("保存日线数据异常", e);
        }
        log.info("结束更新股票均线");
    }

    /**
     * 实时监控监控
     */
//    @Scheduled(cron = "0,15,30,45 * 9,10,11,13,14 ? * MON-FRI")
    @Scheduled(cron = "0 0/1 9,10,11,13,14 ? * MON-FRI")
    public void monitorStockTask() {
        MDC.put("processId", IdUtil.simpleUUID());
        boolean isBusinessTime = stockUtil.isStockTradeTime(new Date());
        log.info("开始实时监控监控" + isBusinessTime);
        if (!isBusinessTime) {
            return;
        }

        try {
            stockService.monitorStock();
        } catch (Exception e) {
            DingdingUtil.sendMsg("实时监测股票数据异常!");
            log.error("实时监测股票数据异常", e);
        }
        log.info("结束实时监控监控");
    }



}
