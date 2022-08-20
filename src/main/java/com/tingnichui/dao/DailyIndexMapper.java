package com.tingnichui.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tingnichui.pojo.po.DailyIndex;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;


@Mapper
public interface DailyIndexMapper extends BaseMapper<DailyIndex> {

    BigDecimal sumCloserPrice(String stockCode,Integer limit);

}
