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
 * @TableName t_stock_trade_record
 */
@TableName(value ="t_stock_trade_record")
@Data
public class StockTradeRecord implements Serializable {
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
     * 政策表id
     */
    @TableField(value = "stock_trade_strategy_id")
    private Long stockTradeStrategyId;

    /**
     * 交易类型 buy买入 sell卖出

     */
    @TableField(value = "trade_type")
    private String tradeType;

    /**
     * 交易价格
     */
    @TableField(value = "trade_price")
    private BigDecimal tradePrice;

    /**
     * 交易日期
     */
    @TableField(value = "trade_date")
    private Date tradeDate;

    /**
     * 交易数量，单位手
     */
    @TableField(value = "trade_amount")
    private Integer tradeAmount;

    /**
     * 是否交易成功 0未交易1已交易
     */
    @TableField(value = "is_done")
    private Boolean done;

    /**
     * 
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}