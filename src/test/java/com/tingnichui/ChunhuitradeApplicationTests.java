package com.tingnichui;

import com.tingnichui.dao.UserMapper;
import com.tingnichui.service.StockService;
import com.tingnichui.service.impl.StockServiceImpl;
import com.tingnichui.util.BaiduUtil;
import com.tingnichui.util.DingdingUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
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

    @Test
    void BaiduUtilTest() {
        System.err.println(baiduUtil.accurate());
    }

    @Test
    void stockServiceTest() {


//        stockServiceImpl.doSaveDailyRecord("SZ300100","双林股份");
//        stockService.saveDailyRecord4EastMoney();
        stockService.monitorStock();
    }

    @Test
    void UtilTest() {
        DingdingUtil.sendMsg("tingnichui");
    }
}
