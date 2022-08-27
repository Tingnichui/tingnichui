package com.tingnichui.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Geng Hui
 * @version 1.0
 * @date 2022/8/20 18:43
 */
@Data
@TableName("t_stock_trade_record")
public class StockTradeRecord {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 股票代码
     */
    private String stockCode;


    /**
     * 交易策略id
     */
    private Long stockTradeStrategyId;

    /**
     * 交易类型
     */
    private String tradeType;

    /**
     * 买入价
     */
    private BigDecimal tradePrice;

    /**
     * 买入日期
     */
    private java.sql.Date tradeDate;

    /**
     * 交易数量，单位手
     */
    private Integer tradeAmount;

    /**
     * 交易数量，单位手
     */
    private Boolean isDone;

    /**
     * update_time
     */
    private Date updateTime;

    /**
     * create_time
     */
    private Date createTime;

}
