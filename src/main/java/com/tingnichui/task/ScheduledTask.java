package com.tingnichui.task;

import com.tingnichui.service.StockService;
import com.tingnichui.util.DingdingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Geng Hui
 * @version 1.0
 * @date 2022/8/14 16:58
 */
@Slf4j
@Component
public class ScheduledTask {

    @Resource
    private StockService stockService;


    /**
     * 更新股票信息
     */
    @Scheduled(cron = "0 0 9 ? * MON-FRI")
    public void updateStockInfoTask() {
        boolean isBusinessTime = stockService.isBusinessDate(new Date());
        if (!isBusinessTime) {
            return;
        }
        try {
            stockService.updateStockInfo();
        } catch (Exception e) {
            DingdingUtil.sendMsg("更新股票信息异常!");
            log.error("更新股票信息异常", e);
        }
    }


    /**
     * 保存日线数据
     */
    @Scheduled(cron = "0 0 17,18,19 ? * MON-FRI")
    public void saveDailyIndexTask() {
        boolean isBusinessTime = stockService.isBusinessDate(new Date());
        if (!isBusinessTime) {
            return;
        }
        try {
            stockService.saveDailyRecord4EastMoney();
        } catch (Exception e) {
            DingdingUtil.sendMsg("保存日线数据异常!");
            log.error("保存日线数据异常", e);
        }
    }

    /**
     * 更新股票均线
     */
    @Scheduled(cron = "0 0 20,21,22 ? * MON-FRI")
    public void updateDailyIndexAverageTask() {
        boolean isBusinessTime = stockService.isBusinessDate(new Date());
        if (!isBusinessTime) {
            return;
        }
        try {
            stockService.updateDailyIndexAverage();
        } catch (Exception e) {
            DingdingUtil.sendMsg("保存日线数据异常!");
            log.error("保存日线数据异常", e);
        }
    }

    /**
     * 实时监控监控
     */
//    @Scheduled(cron = "0,15,30,45 * 9,10,11,13,14 ? * MON-FRI")
    @Scheduled(cron = "0 0 9,10,11,13,14 ? * MON-FRI")
    public void monitorStockTask() {
        boolean isBusinessTime = stockService.isBusinessTime(new Date());
        if (!isBusinessTime) {
            return;
        }

        try {
            stockService.monitorStock();
        } catch (Exception e) {
            DingdingUtil.sendMsg("实时监测股票数据异常!");
            log.error("实时监测股票数据异常", e);
        }
    }



}
