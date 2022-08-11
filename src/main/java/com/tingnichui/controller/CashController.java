package com.tingnichui.controller;

import cn.hutool.core.date.DateUtil;
import com.tingnichui.entity.vo.Result;
import com.tingnichui.service.CashService;
import com.tingnichui.util.ResultGenerator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cash")
public class CashController {

    @Resource
    private CashService cashService;


    @RequestMapping("save/{tradeAmount}/{actualAmount}")
    public Result save(@PathVariable BigDecimal tradeAmount, @PathVariable BigDecimal actualAmount) {
        return cashService.save(tradeAmount, actualAmount);
    }

    @RequestMapping("health")
    public Result health() {
        return ResultGenerator.genSuccessResult(DateUtil.now());
    }
}
