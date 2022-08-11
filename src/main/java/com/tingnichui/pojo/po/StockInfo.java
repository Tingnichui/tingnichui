package com.tingnichui.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author Geng Hui
 * @version 1.0
 * @date 2022/8/11 22:26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "t_stock_info")
public class StockInfo {
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
     * name
     */
    private String name;

    /**
     * exchange
     */
    private String exchange;

    /**
     * abbreviation
     */
    private String abbreviation;

    /**
     * state
     */
    private int state;

    /**
     * type
     */
    private int type;

    /**
     * create_time
     */
    private Timestamp createTime;

    /**
     * update_time
     */
    private Timestamp updateTime;

    /**
     * description
     */
    private String description;

}
