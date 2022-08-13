package com.tingnichui;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.tingnichui.dao.DailyIndexMapper;
import com.tingnichui.dao.UserMapper;
import com.tingnichui.pojo.po.DailyIndex;
import com.tingnichui.service.StockService;
import com.tingnichui.service.impl.StockServiceImpl;
import com.tingnichui.util.BaiduUtil;
import com.tingnichui.util.DingdingUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest
class ChunhuitradeApplicationTests {

    @Resource
    private BaiduUtil baiduUtil;

    @Resource
    private UserMapper userMapper;


    @Resource
    private StockService stockService;

    @Resource
    private StockServiceImpl stockServiceImpl;

    @Resource
    private DailyIndexMapper dailyIndexMapper;

    @Test
    void BaiduUtilTest() {
        System.err.println(baiduUtil.accurate());
    }

    @Test
    void stockServiceTest() {
        DailyIndex dailyIndex = dailyIndexMapper.selectById(4853);
        dailyIndex.setMa5(new BigDecimal("11.20"));
        dailyIndex.setMa10(new BigDecimal("12.20"));
        dailyIndex.setMa20(new BigDecimal("13.20"));
        dailyIndex.setMa100(new BigDecimal("14.20"));
        dailyIndex.setMa500(new BigDecimal("15.20"));
        dailyIndexMapper.updateById(dailyIndex);


//        stockService.updateStock4xueqiu();
//        stockService.saveDailyRecord4xueqiu();
//        stockServiceImpl.doSaveDailyRecord("SZ300100","双林股份");
//        stockService.saveDailyRecord4EastMoney();
//        stockService.monitorStock();
    }

    @Test
    void UtilTest() {
        DingdingUtil.sendMsg("tingnichui");
    }

    public static void main(String[] args) {
        DateTime dateTime = DateUtil.date(Long.parseLong("1660233600000"));
        System.err.println(dateTime);
    }
}
