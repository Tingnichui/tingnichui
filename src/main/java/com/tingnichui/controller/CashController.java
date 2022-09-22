package com.tingnichui.controller;

import com.tingnichui.pojo.vo.Result;
import com.tingnichui.service.CashService;
import com.tingnichui.util.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author  Geng Hui
 * @date  2022/8/25 9:31
 */
@Api(tags = "POS机交易")
@RestController
@RequestMapping("/api/cash")
public class CashController {

    @Resource
    private CashService cashService;

    @ApiOperation("测试服务是否正常运行")
    @GetMapping("health")
    public Result health() {
        return ResultGenerator.genSuccessResult("HEALTH");
    }

    @ApiOperation("保存交易")
    @PostMapping("save/{tradeAmount}/{actualAmount}")
    public Result save(@PathVariable BigDecimal tradeAmount, @PathVariable BigDecimal actualAmount) {
        return cashService.save(tradeAmount, actualAmount);
    }

    @ApiOperation("分页查询交易记录")
    @GetMapping("listCashRecord")
    public Result listCashRecord() {
        return cashService.listCashRecord();
    }
}
