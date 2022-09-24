package com.tingnichui.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tingnichui.pojo.po.Cash;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Tingnichui
* @description 针对表【t_cash】的数据库操作Mapper
* @createDate 2022-09-24 14:41:22
* @Entity com.tingnichui.pojo.po.Cash
*/
@Mapper
public interface CashMapper extends BaseMapper<Cash> {

}




