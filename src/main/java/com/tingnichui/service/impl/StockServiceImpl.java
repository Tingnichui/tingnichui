package com.tingnichui.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.NamedThreadFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tingnichui.dao.DailyRecordMapper;
import com.tingnichui.dao.StockMapper;
import com.tingnichui.pojo.po.DailyRecord;
import com.tingnichui.pojo.po.Stock;
import com.tingnichui.pojo.vo.Result;
import com.tingnichui.service.StockService;
import com.tingnichui.util.ResultGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Geng Hui
 * @version 1.0
 * @date 2022/8/10 21:22
 */
@Slf4j
@Service
public class StockServiceImpl implements StockService {

    @Resource
    private StockMapper stockMapper;

    @Resource
    private DailyRecordMapper dailyRecordMapper;

    public static String getHttpGetResponseString(String url, String cookie) {
        String body = "";
        try {
            DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(URI.create(url));
            if (Objects.nonNull(cookie)) {
                httpGet.setHeader("Cookie", cookie);
            }
            CloseableHttpResponse response = defaultHttpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                body = EntityUtils.toString(entity, "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }

    private String xueQiuUrl = "http://xueqiu.com/service/screener/screen?category=CN&exchange=sh_sz&areacode=&indcode=&order_by=symbol&order=desc&page=1&size=5000&only_count=0&current=&pct=&mc=&volume=&_=1637244021109";
    private String xueQiuCookie = "__utma=1.465353408.1597642113.1597642113.1597642113.1; device_id=0e9b5598ead36baaeaafc82e3d3ad790; s=by149wapcc; acw_tc=2760827016601384264196751e96b9bb03e403e947653a8379c4f3f7de6c4e; xq_a_token=bf75ab4bcea18c79de253cb841f2b27e248d8948; xq_r_token=c7d30dc738a77dd909a8228f3053679e86bf104b; xq_id_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOi0xLCJpc3MiOiJ1YyIsImV4cCI6MTY2MTgxNjI0MSwiY3RtIjoxNjYwMTM4NDA0NzkwLCJjaWQiOiJkOWQwbjRBWnVwIn0.adqS0ILb7mTZNReAmMIAVw5G6lgyLUpI-PWT9om3S1kujV7xLKjN6iIVB4ESeUVxFuTHwVQPBzuxpyLgR7dwhJ8kAW3vRm9jb9cOtTnipilI-l1MprMSPxLeTt3rKuOjgyyL4pXTRQtWjHhgny0PSHTOVJUEa9ggzZRImw_6I5oqLxF5jPv8CnXuLzovQPg73rOW1bp9PD9Z6OsbE6pXpPb6P06H94GBv_iO2rpQIneXkIlXEmATNbP0kj09i57sT0yP6W_QGwvDa0MAyYWJwu2vroF4ikFyXsk49txJdytU5CUe0cfh_FKddVOvkEGE7dWanE5ugpcaQ2txQYa0lQ; SL_G_WPT_TO=zh-CN; SL_GWPT_Show_Hide_tmp=1; SL_wptGlobTipTmp=1";

    @Override
    public Result updateStock() {
        try {
            String body =
                    getHttpGetResponseString(xueQiuUrl, xueQiuCookie);
            JSONObject data = JSON.parseObject(body).getJSONObject("data");
            JSONArray list = data.getJSONArray("list");
            Date today = new Date();
            BigDecimal zero = new BigDecimal(0);
            if (CollectionUtils.isNotEmpty(list)) {
                for (Object o : list) {
                    data = (JSONObject) o;
                    // volume 值不准确忽略
                    BigDecimal current = data.getBigDecimal("current");
                    BigDecimal volume = data.getBigDecimal("volume");
                    if (Objects.nonNull(current) && Objects.nonNull(volume)) {
                        Stock exist = stockMapper.selectById(data.getString("symbol"));
                        if (Objects.nonNull(exist)) {
                            List<DailyRecord> dailyRecords = dailyRecordMapper
                                    .selectPage(new Page<>(1, 20),
                                            new QueryWrapper<DailyRecord>().eq("code", data.getString("symbol"))
                                                    .ge("date", DateUtils.addDays(new Date(), -40)).orderByDesc("date"))
                                    .getRecords();
                            exist.setVolume(volume.longValue());
                            exist.setModifyTime(today);
                            exist.setCurrentPrice(current);
                            exist.setTransactionAmount(current.multiply(volume));
                            if (dailyRecords.size() >= 20) {
                                exist.setMa5(BigDecimal
                                        .valueOf(dailyRecords.subList(0, 5).stream().map(DailyRecord::getClosePrice)
                                                .collect(Collectors.averagingDouble(BigDecimal::doubleValue))));
                                exist.setMa10(BigDecimal
                                        .valueOf(dailyRecords.subList(0, 10).stream().map(DailyRecord::getClosePrice)
                                                .collect(Collectors.averagingDouble(BigDecimal::doubleValue))));
                                exist.setMa20(BigDecimal
                                        .valueOf(dailyRecords.subList(0, 20).stream().map(DailyRecord::getClosePrice)
                                                .collect(Collectors.averagingDouble(BigDecimal::doubleValue))));
                            }
                            stockMapper.updateById(exist);
                        } else {
                            stockMapper.insert(Stock.builder().code(data.getString("symbol"))
                                    .name(data.getString("name")).marketValue(data.getLongValue("mc")).currentPrice(current)
                                    .volume(volume.longValue()).ma5(zero).ma10(zero).ma20(zero)
                                    .transactionAmount(current.multiply(volume)).modifyTime(today).track(false)
                                    .shareholding(false).focus(false).classification("").build());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultGenerator.genFailResult("未成功爬取数据！");
        }
        return ResultGenerator.genSuccessResult("股票每日成交量、MA5、MA10、MA20更新完成！");
    }


    private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(5000), new NamedThreadFactory("每日交易数据线程-", false));

    public void toSaveDailyRecord(HashMap<String, String> map) {
        Integer count = dailyRecordMapper.selectCount(new QueryWrapper<DailyRecord>().ge("date", checkDailyRecord()));
        if (count > 0) {
            System.err.println();
            return;
        }
        log.warn("开始更新股票每日成交数据！");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            executorService.submit(() -> this.saveDailyRecord(entry.getKey(), entry.getValue()));
        }
        while (executorService.getQueue().size() != 0) {
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
        log.warn("更新股票每日成交数据完成！");
    }

    private String xueQiuDetailUrl = "http://stock.xueqiu.com/v5/stock/chart/kline.json?symbol={code}&begin={time}&period=day&type=before&count=-{recentDayNumber}&indicator=kline,pe,pb,ps,pcf,market_capital,agt,ggt,balance";
    @Override
    public Result saveDailyRecord(String code, String name) {
        try {
            String url = xueQiuDetailUrl.replace("{code}", code)
                    .replace("{time}", String.valueOf(System.currentTimeMillis()))
                    .replace("{recentDayNumber}", String.valueOf(1));
            String body = getHttpGetResponseString(url, xueQiuCookie);
            JSONObject data = JSON.parseObject(body).getJSONObject("data");
            JSONArray list = data.getJSONArray("item");
            if (CollectionUtils.isNotEmpty(list)) {
                JSONArray array;
                for (Object o : list) {
                    array = (JSONArray) o;
                    DailyRecord dailyRecord = new DailyRecord();
                    dailyRecord.setDate(new Date(array.getLongValue(0)));
                    dailyRecord.setCode(code);
                    dailyRecord.setName(name);
                    dailyRecord.setOpenPrice(BigDecimal.valueOf(array.getDoubleValue(2)));
                    dailyRecord.setHighest(BigDecimal.valueOf(array.getDoubleValue(3)));
                    dailyRecord.setLowest(BigDecimal.valueOf(array.getDoubleValue(4)));
                    dailyRecord.setClosePrice(BigDecimal.valueOf(array.getDoubleValue(5)));
                    dailyRecord.setIncreaseRate(BigDecimal.valueOf(array.getDoubleValue(7)));
                    dailyRecord.setAmount(array.getLongValue(9) / 10000);
                    dailyRecordMapper.insert(dailyRecord);
                }
            }
            Thread.sleep(3000);
        } catch (Exception e) {
            return ResultGenerator.genFailResult(name + "未成功保存日记录");
        }
        return ResultGenerator.genSuccessResult("更新[ " + name + " ]近日成交数据完成！");
    }

    private Date checkDailyRecord() {
        String url = xueQiuUrl.replace("{code}", "SH600519")
                .replace("{time}", String.valueOf(System.currentTimeMillis())).replace("{recentDayNumber}", "1");
        String body = getHttpGetResponseString(url, xueQiuCookie);
        JSONObject data = JSON.parseObject(body).getJSONObject("data");
        JSONArray list = data.getJSONArray("item");
        if (CollectionUtils.isNotEmpty(list)) {
            JSONArray array;
            for (Object o : list) {
                array = (JSONArray) o;
                return new Date(array.getLongValue(0));
            }
        }
        return new Date();
    }
    /**
     * 所有股票 key-code value-name
     */
    public static final HashMap<String, String> STOCK_MAP = new HashMap<>();
    /**
     * 需要监控关注的票 key-name value-Stock
     */
    public static final HashMap<String, Stock> TRACK_STOCK_MAP = new HashMap<>();

    @Override
    public Result run() {
        List<Stock> stocks = stockMapper.selectList(new QueryWrapper<Stock>().notLike("name", "%ST%")
                .notLike("name", "%st%").notLike("name", "%A%").notLike("name", "%C%").notLike("name", "%N%")
                .notLike("name", "%U%").notLike("name", "%W%").notLike("code", "%BJ%").notLike("code", "%688%"));
        stocks.forEach(e -> {
            if (!e.getIgnoreMonitor() && (e.getShareholding() || e.getTrack())) {
                TRACK_STOCK_MAP.put(e.getName(), e);
            }
            STOCK_MAP.put(e.getCode(), e.getName());
        });
        // 补充写入某只股票的历史交易数据
        // dailyRecordProcessor.run("SZ300015", "爱尔眼科");
        if (DateUtil.hour(new Date(), true) >= 15) {
//            executorService.submit(this::queryMainFundData);
            // 15点后读取当日交易数据
            this.toSaveDailyRecord(STOCK_MAP);
            // 更新每只股票收盘价，当日成交量，MA5 MA10 MA20
            this.updateStock();
            // 更新 外资+基金 持仓 只更新到最新季度报告的汇总表上 基金季报有滞后性，外资持仓则是实时计算，每天更新的
//            updateForeignFundShareholding(202201);
            // 分析连板数据
//            analyzePlank();
            // 分析主力流入数据
//            analyzeMainFund();
            // 分析上升趋势的股票
//            analyzeUpwardTrend();
            // 爆量回踩
//            screeningStocks.explosiveVolumeBack(new Date());
            // 分析红三兵股票
//            screeningStocks.checkRedThreeSoldiersStock(new Date());
        } else {
            this.toSaveDailyRecord(STOCK_MAP);
            // 15点以前实时监控涨跌
//            this.updateStock();
        }

        return ResultGenerator.genSuccessResult();
    }

}
