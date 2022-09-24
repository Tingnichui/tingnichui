package com.tingnichui.service;

import com.tingnichui.pojo.po.Cash;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tingnichui.pojo.vo.Result;

import java.math.BigDecimal;

/**
* @author Tingnichui
* @description 针对表【t_cash】的数据库操作Service
* @createDate 2022-09-24 14:41:22
*/
public interface CashService extends IService<Cash> {
    Result save(BigDecimal tradeAmount, BigDecimal actualAmount);
    Result listCashRecord();
}
