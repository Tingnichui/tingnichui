package com.tingnichui;

import com.tingnichui.dao.UserMapper;
import com.tingnichui.util.BaiduUtil;
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

    @Test
    void BaiduUtilTest() {
        System.err.println(baiduUtil.accurate());
    }

}
