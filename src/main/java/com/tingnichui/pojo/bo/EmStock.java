package com.tingnichui.pojo.bo;

import com.tingnichui.pojo.po.DailyIndex;
import com.tingnichui.pojo.po.StockInfo;
import lombok.Data;

/**
 * @author Geng Hui
 * @version 1.0
 * @date 2022/8/21 19:42
 */
@Data
public class EmStock {

    private StockInfo stockInfo;

    private DailyIndex dailyIndex;

}
