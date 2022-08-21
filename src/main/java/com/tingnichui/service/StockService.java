package com.tingnichui.service;

import com.tingnichui.pojo.vo.Result;

import java.util.Date;

/**
 * @author Geng Hui
 * @version 1.0
 * @date 2022/8/10 21:22
 */
public interface StockService {

    Result updateStock4xueqiu();

    Result saveDailyRecord4xueqiu();

    Result saveDailyRecord4EastMoney();

    Result updateDailyIndexAverage();

    Result monitorStock();

    Result updateStockInfo();

}
