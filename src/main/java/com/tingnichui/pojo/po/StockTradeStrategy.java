package com.tingnichui.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Geng Hui
 * @version 1.0
 * @date 2022/8/16 22:32
 */
@Data
@TableName("t_stock_trade_strategy")
public class StockTradeStrategy {

    @TableId(type = IdType.AUTO)
    /**
     * id
     */
    private Long id;

    /**
     * 策略标题
     */
    private String title;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 策略类型 buy买入策略 sell卖出策略
     */
    private String strategyType;

    /**
     * 是否生效 0不生效 1生效
     */
    private Boolean isWork;

    /**
     * 监测类型
     */
    private String monitorType;

    /**
     * 比较方法 gt 大于 lt 小于 eq 等于
     */
    private String compareMethod;

    /**
     * 目标类型
     */
    private String targetType;

    /**
     * 目标计算类型 fixed 固定价 目标值无需进行计算直接与比较类型当前值进行比较 percentage 百分比 获取目标类型值后按百分比（1 目标值）计算出目标值
     */
    private String targetCalculationType;

    /**
     * 目标值
     */
    private BigDecimal targetValue;

    /**
     * 目标数量
     */
    private Integer tragetAmount;

    /**
     * update_time
     */
    private Date updateTime;

    /**
     * create_time
     */
    private Date createTime;

}
