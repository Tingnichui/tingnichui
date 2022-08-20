package com.tingnichui.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tingnichui.pojo.po.StockInfo;
import com.tingnichui.pojo.po.StockTradeRecord;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface StockTradeRecordMapper extends BaseMapper<StockTradeRecord> {

}
