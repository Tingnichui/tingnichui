package com.tingnichui.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName t_stock_info
 */
@TableName(value ="t_stock_info")
@Data
public class StockInfo implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 股票名称
     */
    @TableField(value = "stock_name")
    private String stockName;

    /**
     * 股票交易所 例sh sz bj
     */
    @TableField(value = "stock_exchange")
    private String stockExchange;

    /**
     * 股票代码
     */
    @TableField(value = "stock_code")
    private String stockCode;

    /**
     * 股票类型 0：A股、1：指数、2：ETF、3：B股
     */
    @TableField(value = "stock_type")
    private Integer stockType;

    /**
     * 股票状态 0 上市 1停牌 2 退市
     */
    @TableField(value = "stock_state")
    private Integer stockState;

    /**
     * 0 不监控 1监控
     */
    @TableField(value = "is_monitor")
    private Boolean monitor;

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