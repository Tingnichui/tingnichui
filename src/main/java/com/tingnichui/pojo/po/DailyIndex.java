package com.tingnichui.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName t_daily_index
 */
@TableName(value ="t_daily_index")
@Data
public class DailyIndex implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 股票代码
     */
    @TableField(value = "stock_code")
    private String stockCode;

    /**
     * 股票日期
     */
    @TableField(value = "stock_date")
    private Date stockDate;

    /**
     * 今日开盘价
     */
    @TableField(value = "open_price")
    private BigDecimal openPrice;

    /**
     * 最高价
     */
    @TableField(value = "highest_price")
    private BigDecimal highestPrice;

    /**
     * 最低价
     */
    @TableField(value = "lowest_price")
    private BigDecimal lowestPrice;

    /**
     * 收盘价
     */
    @TableField(value = "close_price")
    private BigDecimal closePrice;

    /**
     * 昨日收盘价
     */
    @TableField(value = "pre_close_price")
    private BigDecimal preClosePrice;

    /**
     * 交易量
     */
    @TableField(value = "trade_volume")
    private BigDecimal tradeVolume;

    /**
     * 交易总金额
     */
    @TableField(value = "trade_amount")
    private BigDecimal tradeAmount;

    /**
     * 换手率
     */
    @TableField(value = "rurnover_rate")
    private BigDecimal rurnoverRate;

    /**
     * 涨幅
     */
    @TableField(value = "increase")
    private BigDecimal increase;

    /**
     * 5日均线值
     */
    @TableField(value = "ma5")
    private BigDecimal ma5;

    /**
     * 10日均线值
     */
    @TableField(value = "ma10")
    private BigDecimal ma10;

    /**
     * 20日均线值
     */
    @TableField(value = "ma20")
    private BigDecimal ma20;

    /**
     * 100日均线值
     */
    @TableField(value = "ma100")
    private BigDecimal ma100;

    /**
     * 500日均线值
     */
    @TableField(value = "ma500")
    private BigDecimal ma500;

    /**
     * 
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}