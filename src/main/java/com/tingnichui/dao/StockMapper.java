package com.tingnichui.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tingnichui.pojo.po.Stock;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface StockMapper extends BaseMapper<Stock> {

}