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
 * @TableName t_cash
 */
@TableName(value ="t_cash")
@Data
public class Cash implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 交易金额
     */
    @TableField(value = "trade_amount")
    private BigDecimal tradeAmount;

    /**
     * 实际金额
     */
    @TableField(value = "actual_amount")
    private BigDecimal actualAmount;

    /**
     * 手续费
     */
    @TableField(value = "fee")
    private BigDecimal fee;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}