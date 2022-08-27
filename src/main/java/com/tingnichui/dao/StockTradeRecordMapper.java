package com.tingnichui.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tingnichui.pojo.po.StockInfo;
import com.tingnichui.pojo.po.StockTradeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author  Geng Hui
 * @date  2022/8/25 9:25
 */
@Mapper
public interface StockTradeRecordMapper extends BaseMapper<StockTradeRecord> {

}
