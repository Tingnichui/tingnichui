package com.tingnichui.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author Geng Hui
 * @version 1.0
 * @date 2022/8/11 22:26
 */
@Data
@TableName(value = "t_stock_info")
public class StockInfo {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 股票名称
     */
    private String stockName;

    /**
     * 股票类型
     */
    private Integer stockType;

    /**
     * 股票交易所
     */
    private String stockExchange;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 股票状态
     */
    private Integer stockState;

    /**
     * 股票状态
     */
    private Boolean isMonitor;

    /**
     * create_time
     */
    private Date createTime;

    /**
     * update_time
     */
    private Date updateTime;

}
