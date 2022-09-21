package com.tingnichui.task;

import cn.hutool.core.util.IdUtil;
import com.tingnichui.annotation.RedisLock;
import com.tingnichui.common.CacheConsts;
import com.tingnichui.service.StockService;
import com.tingnichui.util.DingdingUtil;
import com.tingnichui.util.RedisUtil;
import com.tingnichui.util.StockUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    @RedisLock(key = CacheConsts.UPDATE_STOCK_INFO_TASK_LOCK)
    @Scheduled(cron = "0 0 9 ? * MON-FRI")
    public void updateStockInfoTask() {
        try {
            MDC.put("processId", IdUtil.simpleUUID());
            boolean isBusinessDate = stockUtil.isStockTradeDate(new Date());
            if (!isBusinessDate) {
                log.info("非工作日-结束更新股票信息");
                return;
            }
            log.info("开始更新股票信息" + isBusinessDate);
            stockService.updateStockInfo();
            log.info("结束更新股票信息");
        } catch (Exception e) {
            DingdingUtil.sendMsg("更新股票信息异常!");
            log.error("更新股票信息异常", e);
        }
    }


    /**
     * 保存日线数据
     */
    @RedisLock(key = CacheConsts.SAVE_DAILY_RECORD_TASK_LOCK)
    @Scheduled(cron = "0 0 17,18,19 ? * MON-FRI")
    public void saveDailyIndexTask() {
        try {
            MDC.put("processId", IdUtil.simpleUUID());
            boolean isBusinessDate = stockUtil.isStockTradeDate(new Date());
            if (!isBusinessDate) {
                log.info("非工作日-结束更新股票信息");
                return;
            }
            log.info("开始保存日线数据" + isBusinessDate);
            stockService.saveDailyRecord4EastMoney();
            log.info("结束保存日线数据");
        } catch (Exception e) {
            DingdingUtil.sendMsg("保存日线数据异常!");
            log.error("保存日线数据异常", e);
        }
    }

    /**
     * 更新股票均线
     */
    @RedisLock(key = CacheConsts.UPDATE_DAILY_INDEX_AVERAGE_TASK_LOCK)
    @Scheduled(cron = "0 0 20,21,22 ? * MON-FRI")
    public void updateDailyIndexAverageTask() {
        try {
            MDC.put("processId", IdUtil.simpleUUID());
            boolean isBusinessDate = stockUtil.isStockTradeDate(new Date());
            if (!isBusinessDate) {
                log.info("非工作日-结束更新股票均线");
                return;
            }
            log.info("开始更新股票均线" + isBusinessDate);
            stockService.updateDailyIndexAverage();
            log.info("结束更新股票均线");
        } catch (Exception e) {
            DingdingUtil.sendMsg("保存日线数据异常!");
            log.error("更新股票均线异常", e);
        }
    }

    /**
     * 实时监控监控
     */
//    @Scheduled(cron = "0,15,30,45 * 9,10,11,13,14 ? * MON-FRI")
    @RedisLock(key = CacheConsts.MONITOR_STOCK_TASK_LOCk)
    @Scheduled(cron = "0/15 * 9,10,11,13,14 ? * MON-FRI")
    public void monitorStockTask() {
        try {
            MDC.put("processId", IdUtil.simpleUUID());
            boolean isBusinessTime = stockUtil.isStockTradeTime(new Date());
            if (!isBusinessTime) {
                log.info("非交易时间-结束监控股票信息");
                return;
            }
            log.info("开始实时监控监控" + isBusinessTime);
            stockService.monitorStock();
            log.info("结束实时监控监控");
        } catch (Exception e) {
            DingdingUtil.sendMsg("实时监测股票数据异常!");
            log.error("实时监测股票数据异常", e);
        }
    }


}
