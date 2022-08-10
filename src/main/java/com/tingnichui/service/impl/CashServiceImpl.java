package com.tingnichui.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.tingnichui.dao.CashMapper;
import com.tingnichui.entity.po.Cash;
import com.tingnichui.entity.vo.Result;
import com.tingnichui.service.CashService;
import com.tingnichui.util.ResultGenerator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CashServiceImpl implements CashService {

    @Resource
    private CashMapper cashMapper;

    @Override
    public Result save(BigDecimal tradeAmount, BigDecimal actualAmount) {

        Cash cash = new Cash();
        cash.setTradeAmount(tradeAmount);
        cash.setActualAmount(actualAmount);
        cash.setFee(NumberUtil.sub(tradeAmount, actualAmount));
        cashMapper.insert(cash);

        return ResultGenerator.genSuccessResult();
    }

}
