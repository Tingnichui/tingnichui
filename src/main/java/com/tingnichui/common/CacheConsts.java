package com.tingnichui.common;

/**
 * @author Geng Hui
 * @date 2022/8/28 1:31
 */
public  class CacheConsts {

    /**
     * 从东方财富保存股票日线信息
     */
    public static final String SAVE_DAILY_RECORD_FROM_EASTMONEY_LOCK = "SAVE_DAILY_RECORD_FROM_EASTMONEY_LOCK";

    /**
     * 更新股票ma5 ma10 ma20 ma100 ma500
     */
    public static final String UPDATE_DAILY_INDEX_AVERAGE_LOCK = "UPDATE_DAILY_INDEX_AVERAGE_LOCK";

    /**
     * 更新股票信息
     */
    public static final String UPDATE_STOCK_INFO_LOCK = "UPDATE_STOCK_INFO_LOCK";

    /**
     * 更新股票信息定时任务
     */
    public static final String UPDATE_STOCK_INFO_TASK_LOCK = "UPDATE_STOCK_INFO_TASK_LOCK";

    /**
     * 保存股票日线信息任务
     */
    public static final String SAVE_DAILY_RECORD_TASK_LOCK = "SAVE_DAILY_RECORD_TASK_LOCK";

    /**
     * 更新股票均线
     */
    public static final String UPDATE_DAILY_INDEX_AVERAGE_TASK_LOCK = "UPDATE_DAILY_INDEX_AVERAGE_TASK_LOCK";

    /**
     * 实时监控股票交易
     */
    public static final String MONITOR_STOCK_TASK_LOCk = "MONITOR_STOCK_TASK_LOCk";
}
