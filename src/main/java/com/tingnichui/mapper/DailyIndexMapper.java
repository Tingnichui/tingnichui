package com.tingnichui.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tingnichui.pojo.po.DailyIndex;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

/**
 * @author  Geng Hui
 * @date  2022/8/25 9:24
 */
@Mapper
public interface DailyIndexMapper extends BaseMapper<DailyIndex> {

    /**
     * 根据股票代码计算一定数量的收盘价之和，用于实时计算均线
     * @param stockCode 股票代码
     * @param limit 条数
     * @return 收盘价总和
     */
    BigDecimal sumCloserPrice(String stockCode,Integer limit);

}
