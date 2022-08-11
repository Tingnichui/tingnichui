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
     * code
     */
    private String code;

    /**
     * date
     */
    private Date date;

    /**
     * pre_closing_price
     */
    private BigDecimal preClosingPrice;

    /**
     * opening_price
     */
    private BigDecimal openingPrice;

    /**
     * highest_price
     */
    private BigDecimal highestPrice;

    /**
     * lowest_price
     */
    private BigDecimal lowestPrice;

    /**
     * closing_price
     */
    private BigDecimal closingPrice;

    /**
     * trading_volume
     */
    private Long tradingVolume;

    /**
     * trading_value
     */
    private BigDecimal tradingValue;

    /**
     * rurnover_rate
     */
    private BigDecimal rurnoverRate;

    /**
     * create_time
     */
    private Timestamp createTime;

    /**
     * update_time
     */
    private Timestamp updateTime;



}
