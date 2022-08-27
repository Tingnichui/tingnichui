package com.tingnichui.controller;

import com.tingnichui.pojo.vo.Result;
import com.tingnichui.service.CashService;
import com.tingnichui.util.ResultGenerator;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author  Geng Hui
 * @date  2022/8/25 9:31
 */
@RestController
@RequestMapping("/api/cash")
public class CashController {

    @Resource
    private CashService cashService;

    @GetMapping("health")
    public Result health() {
        return ResultGenerator.genSuccessResult("HEALTH");
    }

    @PostMapping("save/{tradeAmount}/{actualAmount}")
    public Result save(@PathVariable BigDecimal tradeAmount, @PathVariable BigDecimal actualAmount) {
        return cashService.save(tradeAmount, actualAmount);
    }
}
