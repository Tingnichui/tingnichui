package com.tingnichui.service;

import com.tingnichui.pojo.vo.Result;

/**
 * @author Geng Hui
 * @version 1.0
 * @date 2022/8/10 21:22
 */
public interface StockService {

    Result updateStock();

    Result saveDailyRecord(String code, String name);

    Result run();

}
