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
 * @TableName t_stock_trade_strategy
 */
@TableName(value ="t_stock_trade_strategy")
@Data
public class StockTradeStrategy implements Serializable {
    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 策略标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 股票代码
     */
    @TableField(value = "stock_code")
    private String stockCode;

    /**
     * 策略类型 buy买入策略 sell卖出策略
     */
    @TableField(value = "strategy_type")
    private String strategyType;

    /**
     * 是否生效 0不生效 1生效
     */
    @TableField(value = "is_work")
    private Boolean work;

    /**
     * 监测类型
     */
    @TableField(value = "monitor_type")
    private String monitorType;

    /**
     * 比较方法 gt 大于 lt 小于 eq 等于
     */
    @TableField(value = "compare_method")
    private String compareMethod;

    /**
     * 目标类型
     */
    @TableField(value = "target_type")
    private String targetType;

    /**
     * 目标计算类型  fixed 固定价 目标值无需进行计算直接与比较类型当前值进行比较 percentage 百分比 获取目标类型值后按百分比（1 + 目标值）计算出目标值
     */
    @TableField(value = "target_calculation_type")
    private String targetCalculationType;

    /**
     * 目标值
     */
    @TableField(value = "target_value")
    private BigDecimal targetValue;

    /**
     * 目标数量
     */
    @TableField(value = "traget_amount")
    private Integer tragetAmount;

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