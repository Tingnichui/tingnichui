package com.tingnichui.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tingnichui.mapper.CashMapper;
import com.tingnichui.pojo.po.Cash;
import com.tingnichui.pojo.vo.Result;
import com.tingnichui.service.CashService;
import com.tingnichui.util.ResultGenerator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
* @author Tingnichui
* @description 针对表【t_cash】的数据库操作Service实现
* @createDate 2022-09-24 14:41:22
*/
@Service
public class CashServiceImpl extends ServiceImpl<CashMapper, Cash>
    implements CashService{

    @Resource
    private CashMapper cashMapper;

    @Override
    public Result save(BigDecimal tradeAmount, BigDecimal actualAmount) {

        if (tradeAmount.compareTo(BigDecimal.ZERO) <= 0 || actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResultGenerator.fail("到账金额或交易金额不得小于0");
        }

        if (tradeAmount.compareTo(actualAmount) < 0) {
            return ResultGenerator.fail("到账金额不得小于交易金额");
        }

        Cash cash = new Cash();
        cash.setTradeAmount(tradeAmount);
        cash.setActualAmount(actualAmount);
        cash.setFee(NumberUtil.sub(tradeAmount, actualAmount));
        cashMapper.insert(cash);

        return ResultGenerator.success("保存成功");
    }

    @Override
    public Result listCashRecord() {
        List<Cash> cashList = cashMapper.selectList(null);
        return ResultGenerator.success(cashList);
    }

}




