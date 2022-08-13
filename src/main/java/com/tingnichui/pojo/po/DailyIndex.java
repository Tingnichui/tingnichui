package com.tingnichui.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * @author Geng Hui
 * @version 1.0
 * @date 2022/8/11 21:38
 */
@Data
@TableName(value = "t_daily_index", autoResultMap = true)
public class DailyIndex {

    @TableId(type = IdType.AUTO)
    /**
     * id
     */
    private Integer id;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 股票日期
     */
    private Date stockDate;

    /**
     * 昨日收盘价
     */
    private BigDecimal preClosePrice;

    /**
     * 今日开盘价
     */
    private BigDecimal openPrice;

    /**
     * 最高价
     */
    private BigDecimal highestPrice;

    /**
     * 最低价
     */
    private BigDecimal lowestPrice;

    /**
     * 收盘价
     */
    private BigDecimal closePrice;

    /**
     * 交易量
     */
    private Long tradeVolume;

    /**
     * 交易总金额
     */
    private BigDecimal tradeAmount;

    /**
     * 换手率
     */
    private BigDecimal rurnoverRate;

    /**
     * 5日均线值
     */
    private BigDecimal ma5;

    /**
     * 10日均线值
     */
    private BigDecimal ma10;

    /**
     * 20日均线值
     */
    private BigDecimal ma20;

    /**
     * 100日均线值
     */
    private BigDecimal ma100;

    /**
     * 500日均线值
     */
    private BigDecimal ma500;

    /**
     * create_time
     */
    private java.util.Date createTime;

    /**
     * update_time
     */
    private java.util.Date updateTime;



}
