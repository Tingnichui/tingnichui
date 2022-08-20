package com.tingnichui.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tingnichui.dao.DailyIndexMapper;
import com.tingnichui.dao.StockInfoMapper;
import com.tingnichui.dao.StockTradeRecordMapper;
import com.tingnichui.dao.StockTradeStrategyMapper;
import com.tingnichui.pojo.po.DailyIndex;
import com.tingnichui.pojo.po.StockInfo;
import com.tingnichui.pojo.po.StockTradeRecord;
import com.tingnichui.pojo.po.StockTradeStrategy;
import com.tingnichui.pojo.vo.Result;
import com.tingnichui.service.StockService;
import com.tingnichui.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.text.ParseException;
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
    private DailyIndexMapper dailyIndexMapper;

    @Resource
    private StockInfoMapper stockInfoMapper;

    @Resource
    private StockTradeStrategyMapper stockTradeStrategyMapper;

    @Resource
    private StockTradeRecordMapper stockTradeRecordMapper;

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
    public Result updateStock4xueqiu() {
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
                        String stockCode = data.getString("symbol").substring(2);
                        StockInfo exist = stockInfoMapper.selectOne(new LambdaQueryWrapper<StockInfo>().eq(StockInfo::getStockCode, stockCode));
                        if (Objects.nonNull(exist)) {
                            List<DailyIndex> dailyIndexList = dailyIndexMapper
                                    .selectPage(new Page<>(1, 20),
                                            new QueryWrapper<DailyIndex>().eq("stock_code", stockCode)
                                                    .ge("stock_date", DateUtils.addDays(new Date(), -40)).orderByDesc("stock_date"))
                                    .getRecords();
//                            exist.setVolume(volume.longValue());
//                            exist.setModifyTime(today);
//                            exist.setCurrentPrice(current);
//                            exist.setTransactionAmount(current.multiply(volume));
                            DailyIndex dailyIndex = dailyIndexList.get(dailyIndexList.size());
                            if (dailyIndexList.size() >= 20) {
                                dailyIndex.setMa5(BigDecimal
                                        .valueOf(dailyIndexList.subList(0, 5).stream().map(DailyIndex::getClosePrice)
                                                .collect(Collectors.averagingDouble(BigDecimal::doubleValue))));
                                dailyIndex.setMa10(BigDecimal
                                        .valueOf(dailyIndexList.subList(0, 10).stream().map(DailyIndex::getClosePrice)
                                                .collect(Collectors.averagingDouble(BigDecimal::doubleValue))));
                                dailyIndex.setMa20(BigDecimal
                                        .valueOf(dailyIndexList.subList(0, 20).stream().map(DailyIndex::getClosePrice)
                                                .collect(Collectors.averagingDouble(BigDecimal::doubleValue))));
                            }
//                            stockMapper.updateById(exist);
                        } else {
//                            stockMapper.insert(Stock.builder().code(stockCode)
//                                    .name(data.getString("name")).marketValue(data.getLongValue("mc")).currentPrice(current)
//                                    .volume(volume.longValue()).ma5(zero).ma10(zero).ma20(zero)
//                                    .transactionAmount(current.multiply(volume)).modifyTime(today).track(false)
//                                    .shareholding(false).focus(false).classification("").build());
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
        Integer count = dailyIndexMapper.selectCount(new QueryWrapper<DailyIndex>().ge("stock_date", checkDailyRecord()));
        if (count > 0) {
            return;
        }
        log.warn("开始更新股票每日成交数据！");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            executorService.submit(() -> this.doSaveDailyRecord(entry.getKey(), entry.getValue()));
        }
        while (executorService.getQueue().size() != 0) {
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
    }

    private String xueQiuDetailUrl = "http://stock.xueqiu.com/v5/stock/chart/kline.json?symbol={code}&begin={time}&period=day&type=before&count=-{recentDayNumber}&indicator=kline,pe,pb,ps,pcf,market_capital,agt,ggt,balance";

    public Result doSaveDailyRecord(String code, String name) {
        try {
            String url = xueQiuDetailUrl.replace("{code}", code.toUpperCase())
                    .replace("{time}", String.valueOf(System.currentTimeMillis()))
                    .replace("{recentDayNumber}", String.valueOf(1));
            String body = getHttpGetResponseString(url, xueQiuCookie);
            JSONObject data = JSON.parseObject(body).getJSONObject("data");
            JSONArray keyList = data.getJSONArray("column");
            JSONArray valueList = data.getJSONArray("item");
            if (CollectionUtils.isNotEmpty(valueList)) {
                List<Map<String, Object>> mapLIst = new ArrayList<>();
                for (Object item : valueList) {
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 0; i < keyList.size(); i++) {
                        map.put(keyList.get(i).toString(), ((JSONArray) item).get(i));
                    }
                    mapLIst.add(map);
                }
                //{
                //    "data":
                //    {
                //        "symbol":"SH605333",
                //         "column":["timestamp","volume","open","high","low","close","chg","percent","turnoverrate","amount","volume_post","amount_post","pe","pb","ps","pcf","market_capital","balance","hold_volume_cn","hold_ratio_cn","net_volume_cn","hold_volume_hk","hold_ratio_hk","net_volume_hk"],
                //        "item":[[1660233600000,1632800,29.76,29.96,28.5,28.61,-1.15,-3.86,2.61,4.7630639E7,null,null,-10426.8295,8.6202,4.4768,-106.5401,1.249616367741E10,null,null,null,null,null,null,null]]
                //    },
                //    "error_code":0,
                //        "error_description":""
                //}
                //  {"amount":1.3627193E+8,"chg":-0.05,"ps":1.1013,"turnoverrate":3.41,"percent":-0.49,"volume":13188600,"high":10.58,"pb":1.9456,"pcf":11.6949,"low":10.13,"pe":39.3567,"market_capital":4093884892.74,"close":10.18,"open":10.18,"timestamp":1660233600000}
                for (Map<String, Object> map : mapLIst) {
                    DailyIndex dailyIndex = new DailyIndex();
                    dailyIndex.setStockDate(new java.sql.Date(DateUtil.date((Long) map.get("timestamp")).getTime()));
                    dailyIndex.setStockCode(code.substring(2));
//                    dailyIndex.set(name);
                    dailyIndex.setOpenPrice((BigDecimal) map.get("open"));
                    dailyIndex.setHighestPrice((BigDecimal) map.get("high"));
                    dailyIndex.setLowestPrice((BigDecimal) map.get("low"));
                    dailyIndex.setClosePrice((BigDecimal) map.get("close"));
//                    dailyIndex.setIncreaseRate((BigDecimal) map.get("percent"));
                    dailyIndex.setTradeAmount(NumberUtil.div((BigDecimal) map.get("amount"), new BigDecimal(10000), 2));
                    dailyIndexMapper.insert(dailyIndex);
                }
            }
            Thread.sleep(3000);
        } catch (Exception e) {
            log.warn("爬取日线失败", e);
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
    public static final HashMap<String, StockInfo> TRACK_STOCK_MAP = new HashMap<>();

    @Override
    public Result saveDailyRecord4xueqiu() {
//        if (DateUtil.hour(new Date(), true) >= 15) {
        List<StockInfo> stocks = stockInfoMapper.selectList(new LambdaQueryWrapper<StockInfo>().le(StockInfo::getStockCode, 221));
        stocks.forEach(e -> {
            TRACK_STOCK_MAP.put(e.getStockName(), e);
            STOCK_MAP.put(e.getStockExchange() + e.getStockCode(), e.getStockName());
        });
        // 15点后读取当日交易数据
        this.toSaveDailyRecord(STOCK_MAP);
        // 更新每只股票收盘价，当日成交量，MA5 MA10 MA20
//            this.updateStock4xueqiu();
//        } else {
//            return ResultGenerator.genSuccessResult("还未收盘");
        // 15点以前实时监控涨跌
//            this.updateStock();
//        }

        return ResultGenerator.genSuccessResult("雪球-更新股票每日成交数据完成！");
    }

    @Override
    public Result saveDailyRecord4EastMoney() {
        // 获取未退市的股票和指数
        List<StockInfo> stockInfoList = stockInfoMapper.selectList(null).stream().filter(stockInfo -> (stockInfo.getStockType() == 0 || stockInfo.getStockType() == 1) && 2 != stockInfo.getStockState()).collect(Collectors.toList());
        // 获取当天的日线
        List<DailyIndex> dailyIndexList = dailyIndexMapper.selectList(new LambdaQueryWrapper<DailyIndex>().eq(DailyIndex::getStockDate, new java.sql.Date(System.currentTimeMillis())));
        // 获取代码股票代码集合
        List<String> codeList = dailyIndexList.stream().map(DailyIndex::getStockCode).collect(Collectors.toList());
        // 剔除已经爬取到的股票
        stockInfoList = stockInfoList.stream().filter(v -> !codeList.contains(v.getStockCode())).collect(Collectors.toList());


        this.crawDailyIndexFromSina(stockInfoList.stream().filter(s -> s.getStockType() == 1).collect(Collectors.toList()));
        this.crawDailyIndexFromEastMoney(stockInfoList);
        return ResultGenerator.genSuccessResult("东方财富-更新股票每日成交数据完成！");
    }

    @Override
    public Result updateDailyIndexAverage() {
        // 获取今日所有的日线信息
        List<DailyIndex> dailyIndexList = dailyIndexMapper.selectList(new LambdaQueryWrapper<DailyIndex>().eq(DailyIndex::getStockDate, new java.sql.Date(System.currentTimeMillis())));
        // 遍历计算5 10 20 100 500 均线
        for (DailyIndex dailyIndex : dailyIndexList) {
            Integer count = dailyIndexMapper.selectCount(new LambdaQueryWrapper<DailyIndex>().eq(DailyIndex::getId, dailyIndex.getId()));


            if (count > 5) {
                count = count > 500 ? 500 : count;
                // 按日期倒序获得所有记录
                List<DailyIndex> dailyIndexList4calculate = dailyIndexMapper.selectPage(new Page<>(1, count),
                        new LambdaQueryWrapper<DailyIndex>().eq(DailyIndex::getStockCode, dailyIndex.getStockCode()).orderByDesc(DailyIndex::getStockDate)).getRecords();

                dailyIndex.setMa5(BigDecimal
                        .valueOf(dailyIndexList4calculate.subList(0, 5).stream().map(DailyIndex::getClosePrice)
                                .collect(Collectors.averagingDouble(BigDecimal::doubleValue))));
                if (count > 10) {
                    dailyIndex.setMa10(BigDecimal
                            .valueOf(dailyIndexList4calculate.subList(0, 10).stream().map(DailyIndex::getClosePrice)
                                    .collect(Collectors.averagingDouble(BigDecimal::doubleValue))));
                }
                if (count > 20) {
                    dailyIndex.setMa20(BigDecimal
                            .valueOf(dailyIndexList4calculate.subList(0, 20).stream().map(DailyIndex::getClosePrice)
                                    .collect(Collectors.averagingDouble(BigDecimal::doubleValue))));
                }
                if (count > 100) {
                    dailyIndex.setMa100(BigDecimal
                            .valueOf(dailyIndexList4calculate.subList(0, 100).stream().map(DailyIndex::getClosePrice)
                                    .collect(Collectors.averagingDouble(BigDecimal::doubleValue))));
                }
                if (count > 500) {
                    dailyIndex.setMa500(BigDecimal
                            .valueOf(dailyIndexList4calculate.subList(0, 500).stream().map(DailyIndex::getClosePrice)
                                    .collect(Collectors.averagingDouble(BigDecimal::doubleValue))));
                }

                dailyIndexMapper.updateById(dailyIndex);
            }

        }

        return ResultGenerator.genSuccessResult("更新股票ma5 ma10 ma20 ma100 ma500！");
    }

    private Map<String, BigDecimal> lastPriceMap = new HashMap<>();

    @Override
    public Result monitorStock() {
        // 新浪
        List<StockInfo> monitorStockList = stockInfoMapper.selectList(new LambdaQueryWrapper<StockInfo>().eq(StockInfo::getIsMonitor, Boolean.TRUE));
        List<String> codeList = monitorStockList.stream().map(v -> StockUtil.getFullCode(v.getStockCode())).collect(Collectors.toList());
        List<DailyIndex> nowDailyIndexList = this.getDailyIndex(codeList);


        StringBuilder sb = new StringBuilder();
        for (StockInfo stockInfo : monitorStockList) {

            String stockCode = stockInfo.getStockCode();
            DailyIndex dailyIndex = nowDailyIndexList.stream().filter(d -> d.getStockCode().contains(stockCode)).findAny().orElse(null);
            if (dailyIndex == null) {
                continue;
            }

            // 注：所有策略在当天只能生效一次
            // 买点
            List<StockTradeStrategy> buyStrategyList = stockTradeStrategyMapper.selectList(new LambdaQueryWrapper<StockTradeStrategy>().eq(StockTradeStrategy::getStockCode, stockCode).eq(StockTradeStrategy::getStrategyType, "buy"));
            for (StockTradeStrategy buyStrategy : buyStrategyList) {
                // 去股票交易表表查看是否有今日达成的策略交易
                Integer todayTradeCount = stockTradeRecordMapper.selectCount(new LambdaQueryWrapper<StockTradeRecord>().eq(StockTradeRecord::getTradeDate, new java.sql.Date(System.currentTimeMillis())).eq(StockTradeRecord::getStockTradeStrategyId, buyStrategy.getId()));
                if (todayTradeCount > 0) {
                    continue;
                }

                // 买点 符合买点要求就买入
                if (this.getStrategyResult(null,dailyIndex, buyStrategy)) {
                    // TODO GengHui 2022/8/20 此处应该去下单,交易成功后在插入交易列表，这里直接插入交易表 模拟交易
                    StockTradeRecord stockTradeRecord = new StockTradeRecord();
                    stockTradeRecord.setStockCode(stockCode);
                    stockTradeRecord.setStockTradeStrategyId(buyStrategy.getId());
                    stockTradeRecord.setTradeType("buy");
                    stockTradeRecord.setTradePrice(dailyIndex.getClosePrice());
                    stockTradeRecord.setTradeDate(new java.sql.Date(System.currentTimeMillis()));
                    stockTradeRecord.setTradeAmount(buyStrategy.getTragetAmount());
                    stockTradeRecord.setIsDone(false);
                    int insert = stockTradeRecordMapper.insert(stockTradeRecord);
                    if (insert > 0) {
                        String body = String.format("买入%s:当前价格:%.02f, 涨幅%.02f%%",
                                stockInfo.getStockName(),
                                dailyIndex.getClosePrice().doubleValue(),
                                StockUtil.calcIncreaseRate(dailyIndex.getClosePrice(), dailyIndex.getPreClosePrice()).movePointRight(2).doubleValue());
                        sb.append(body + "\n");

                    }
                }
            }

            // 卖点|先判断该策略下有没有建仓，已经建仓才能进行卖点判断
            List<StockTradeRecord> stockTradeRecordList = stockTradeRecordMapper.selectList(new LambdaQueryWrapper<StockTradeRecord>().eq(StockTradeRecord::getStockCode, stockCode).eq(StockTradeRecord::getTradeType, "buy").eq(StockTradeRecord::getIsDone,false));
            int buySum = stockTradeRecordList.stream().mapToInt(StockTradeRecord::getTradeAmount).sum();
            if (!stockTradeRecordList.isEmpty()) {
                // 获取卖点策略
                List<StockTradeStrategy> sellStrategyList = stockTradeStrategyMapper.selectList(new LambdaQueryWrapper<StockTradeStrategy>().eq(StockTradeStrategy::getStockCode, stockCode).eq(StockTradeStrategy::getStrategyType, "sell"));
                for (StockTradeRecord buyRecord : stockTradeRecordList) {
                    for (StockTradeStrategy sellStrategy : sellStrategyList) {
                        // 去股票交易表表查看是否有今日达成的策略交易
                        Integer todayTradeCount = stockTradeRecordMapper.selectCount(new LambdaQueryWrapper<StockTradeRecord>().eq(StockTradeRecord::getTradeDate, new java.sql.Date(System.currentTimeMillis())).eq(StockTradeRecord::getStockTradeStrategyId, sellStrategy.getId()));
                        if (todayTradeCount > 0) {
                            continue;
                        }
                        Integer sellAmount = sellStrategy.getTragetAmount();
                        if (buySum >= sellAmount && this.getStrategyResult(buyRecord,dailyIndex,sellStrategy)) {
                            // TODO GengHui 2022/8/20 此处应该去下单,交易成功后在插入交易列表，这里直接插入交易表 模拟交易
                            // 更新买入记录
                            buyRecord.setIsDone(true);
                            stockTradeRecordMapper.updateById(buyRecord);
                            // 插入卖出记录
                            StockTradeRecord sellRecord = new StockTradeRecord();
                            sellRecord.setStockCode(stockCode);
                            sellRecord.setStockTradeStrategyId(sellStrategy.getId());
                            sellRecord.setTradeType("sell");
                            sellRecord.setTradePrice(dailyIndex.getClosePrice());
                            sellRecord.setTradeDate(new java.sql.Date(System.currentTimeMillis()));
                            sellRecord.setTradeAmount(sellAmount);
                            sellRecord.setIsDone(false);
                            int insert = stockTradeRecordMapper.insert(sellRecord);
                            if (insert > 0) {
                                buySum -= sellAmount;
                                String body = String.format("卖出%s:买入价格:%.02f, 卖出价格:%.02f",
                                        stockInfo.getStockName(),
                                        buyRecord.getTradePrice(),
                                        sellRecord.getTradePrice());
                                sb.append(body + "\n");
                            }
                        }
                    }
                }
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
            DingdingUtil.sendMsg(sb.toString());
        }
        return ResultGenerator.genSuccessResult("新浪-股价实时检测中！");
    }

    private boolean getStrategyResult(StockTradeRecord stockTradeRecord,DailyIndex dailyIndex, StockTradeStrategy buyStrategy) {

        if (null == buyStrategy) {
            DingdingUtil.sendMsg("买入策略不存在");
            return Boolean.FALSE;
        }

        if (!buyStrategy.getIsWork()) {
            return Boolean.FALSE;
        }


            // 获取操作点信息
        String compareMethod = buyStrategy.getCompareMethod();
        String targetCalculationType = buyStrategy.getTargetCalculationType();
        String targetType = buyStrategy.getTargetType();
        String monitorType = buyStrategy.getMonitorType();
        BigDecimal targetValue = buyStrategy.getTargetValue();
        if (StringUtils.isBlank(targetType) || StringUtils.isBlank(targetCalculationType) || StringUtils.isBlank(monitorType) || null == targetValue) {
            DingdingUtil.sendMsg("交易策略参数不完整");
            return Boolean.FALSE;
        }

        // 根据监测类型获取当前监测值
        BigDecimal nowMonitorValue = null;
        if ("preClosePrice".equals(monitorType)) {
            nowMonitorValue = dailyIndex.getPreClosePrice();
        } else if ("openPrice".equals(monitorType)) {
            nowMonitorValue = dailyIndex.getOpenPrice();
        } else if ("closePrice".equals(monitorType)) {
            nowMonitorValue = dailyIndex.getClosePrice();
        } else if ("highestPrice".equals(monitorType)) {
            nowMonitorValue = dailyIndex.getHighestPrice();
        } else if ("lowestPrice".equals(monitorType)) {
            nowMonitorValue = dailyIndex.getLowestPrice();
        } else if ("tradeVolume".equals(monitorType)) {
            nowMonitorValue = dailyIndex.getTradeVolume();
        } else if ("tradeAmount".equals(monitorType)) {
            nowMonitorValue = dailyIndex.getTradeAmount();
        } else if ("rurnoverRate".equals(monitorType)) {
            nowMonitorValue = dailyIndex.getRurnoverRate();
        } else if ("ma5".equals(monitorType)) {
            nowMonitorValue = dailyIndex.getMa5();
        } else if ("ma10".equals(monitorType)) {
            nowMonitorValue = dailyIndex.getMa10();
        } else if ("ma20".equals(monitorType)) {
            nowMonitorValue = dailyIndex.getMa20();
        } else if ("ma100".equals(monitorType)) {
            nowMonitorValue = dailyIndex.getMa100();
        } else if ("ma500".equals(monitorType)) {
            nowMonitorValue = dailyIndex.getMa500();
        } else {
            DingdingUtil.sendMsg("交易策略未获取到当前值！");
            return Boolean.FALSE;
        }


        // 根据目标类型获取当前值,并计算目标值
        if ("percentage".equals(targetCalculationType)) {
            BigDecimal percentage = NumberUtil.add(NumberUtil.div(targetValue, 100), 1);
            if ("tradePrice".equals(targetType)) {
                if (null == stockTradeRecord) {
                    return Boolean.FALSE;
                } else {
                    targetValue = stockTradeRecord.getTradePrice();
                }
            }else if ("preClosePrice".equals(targetType)) {
                targetValue = dailyIndex.getPreClosePrice();
            } else if ("openPrice".equals(targetType)) {
                targetValue = dailyIndex.getOpenPrice();
            } else if ("closePrice".equals(targetType)) {
                targetValue = dailyIndex.getClosePrice();
            } else if ("highestPrice".equals(targetType)) {
                targetValue = dailyIndex.getHighestPrice();
            } else if ("lowestPrice".equals(targetType)) {
                targetValue = dailyIndex.getLowestPrice();
            } else if ("tradeVolume".equals(targetType)) {
                targetValue = dailyIndex.getTradeVolume();
            } else if ("tradeAmount".equals(targetType)) {
                targetValue = dailyIndex.getTradeAmount();
            } else if ("rurnoverRate".equals(targetType)) {
                targetValue = dailyIndex.getRurnoverRate();
            } else if ("ma5".equals(targetType)) {
                targetValue = dailyIndex.getMa5();
            } else if ("ma10".equals(targetType)) {
                targetValue = dailyIndex.getMa10();
            } else if ("ma20".equals(targetType)) {
                targetValue = dailyIndex.getMa20();
            } else if ("ma100".equals(targetType)) {
                targetValue = dailyIndex.getMa100();
            } else if ("ma500".equals(targetType)) {
                targetValue = dailyIndex.getMa500();
            } else {
                DingdingUtil.sendMsg("交易策略未获取到当前值！");
                return Boolean.FALSE;
            }
            targetValue = NumberUtil.mul(targetValue, percentage);
        } else if ("fixed".equals(targetCalculationType)) {
            targetValue = targetValue;
        } else {
            DingdingUtil.sendMsg("交易策略没有设定目标值");
            return Boolean.FALSE;
        }

        // 将对比值与目标值根据比较类型进行判断
        if ("gt".equals(compareMethod) && nowMonitorValue.compareTo(targetValue) > 0) {
            return Boolean.TRUE;
        } else if ("lt".equals(compareMethod) && nowMonitorValue.compareTo(targetValue) < 0) {
            return Boolean.TRUE;
        } else if ("eq".equals(compareMethod) && nowMonitorValue.compareTo(targetValue) == 0) {
            return Boolean.TRUE;
        } else {
            DingdingUtil.sendMsg("交易策略没有设定比较方式！");
        }

        return Boolean.FALSE;

    }

    @Override
    public Result updateStockInfo() {
        // 获取当前t_stock_info中的所有不是指数的股票
        List<StockInfo> list = stockInfoMapper.selectList(null).stream().filter(v -> v.getStockType() != 1).collect(Collectors.toList());
        // 股票名称：股票代码 为了检查爬取的股票信息是否已经存
        Map<String, List<StockInfo>> checkStockInfoMap = list.stream().collect(Collectors.groupingBy(StockInfo::getStockCode));

        ArrayList<StockInfo> needAddList = new ArrayList<>();
        ArrayList<StockInfo> needUpdateList = new ArrayList<>();
        // 爬取股票信息
        List<StockInfo> crawlerList = this.getStockList("f12,f13,f14").stream().map(EmStock::getStockInfo).collect(Collectors.toList());
        for (StockInfo stockInfo : crawlerList) {
            StockConsts.StockLogType stocLogType = null;
            // 检查是否存在 为空则需要添加，否则判断是否有变更，有的话需要更新
            List<StockInfo> existStockINfoList = checkStockInfoMap.get(stockInfo.getStockCode());
            if (existStockINfoList == null) {
                stocLogType = StockConsts.StockLogType.New;
            } else {
                StockInfo existStockInfo = existStockINfoList.get(0);
                if (!stockInfo.getStockName().equals(existStockInfo.getStockName()) && StockUtil.isOriName(stockInfo.getStockName())) {
                    stocLogType = StockConsts.StockLogType.Rename;
                    stockInfo.setId(existStockInfo.getId());
                }
            }

            if (stocLogType != null) {
                if (stocLogType == StockConsts.StockLogType.New) {
                    needAddList.add(stockInfo);
                } else {
                    needUpdateList.add(stockInfo);
                }
            }
        }

        for (StockInfo stockInfo : needAddList) {
            stockInfoMapper.insert(stockInfo);
        }

        for (StockInfo stockInfo : needUpdateList) {
            stockInfoMapper.updateById(stockInfo);
        }

        return ResultGenerator.genSuccessResult("更新股票信息完成！");
    }

    @Override
    public Result updateCurrentYear() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
//        Map<?, ?> data = restTemplate.getForObject("http://tool.bitefu.net/jiari/?d=" + year, Map.class);
        String content = HttpUtil.get("http://tool.bitefu.net/jiari/?d=" + year);
        if (StringUtils.isBlank(content)) {
            return ResultGenerator.genSuccessResult("更新每年工作日异常！");
        }

        JSONObject jsonObject = JSON.parseObject(content);

        @SuppressWarnings("unchecked")
        Map<String, Integer> dateInfo = (Map<String, Integer>) jsonObject.get(String.valueOf(year));
        List<String> list = dateInfo.entrySet().stream().filter(entry -> entry.getValue() != 0).map(entry -> {
            Date date;
            try {
                date = DateUtils.parseDate(year + entry.getKey(), "yyyyMMdd");
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
            return DateUtil.format(date, "yyyyMMdd");
        }).collect(Collectors.toList());

        return ResultGenerator.genSuccessResult(list);
    }

    @Override
    public boolean isBusinessDate(Date date) {

        if (date == null) {
            date = new Date();
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int day = c.get(Calendar.DAY_OF_WEEK);
        if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
            return false;
        }
        List<String> dateList = (List<String>) updateCurrentYear().getData();

        return !dateList.contains(DateUtil.format(date, "yyyyMMdd"));
    }

    @Override
    public boolean isBusinessTime(Date date) {
        if (date == null) {
            date = new Date();
        }

        boolean isBusinessDate = isBusinessDate(date);
        if (!isBusinessDate) {
            return false;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        if (hour < 9 || hour == 12 || hour > 14) {
            return false;
        }

        int minute = c.get(Calendar.MINUTE);
        return !(hour == 9 && minute < 30 || hour == 11 && minute > 30);
    }

    private List<StockInfo> getStockList() {
        List<EmStock> list = getStockList("f12,f13,f14");
        return list.stream().map(EmStock::getStockInfo).collect(Collectors.toList());
    }


    private void crawDailyIndexFromSina(List<StockInfo> list) {
        final int tCount = 500;
        ArrayList<String> stockCodeList = new ArrayList<>(tCount);
        for (StockInfo stockInfo : list) {
            stockCodeList.add(stockInfo.getStockExchange() + stockInfo.getStockCode());
            if (stockCodeList.size() == tCount) {
                saveDailyIndex(stockCodeList);
                stockCodeList.clear();
            }
        }

        if (!stockCodeList.isEmpty()) {
            saveDailyIndex(stockCodeList);
        }
    }

    private void saveDailyIndex(ArrayList<String> stockCodeList) {
        List<DailyIndex> dailyIndexList = this.getDailyIndex(stockCodeList);
        this.saveDailyIndex(this.filterInvalid(dailyIndexList));
    }

    public List<DailyIndex> getDailyIndex(List<String> codeList) {
        String codes = codeList.stream().map(StockUtil::getFullCode).collect(Collectors.joining(","));
        HashMap<String, String> header = new HashMap<>();
        header.put("Referer", "https://finance.sina.com.cn/");
        String content = HttpUtil.get("https://hq.sinajs.cn/list=" + codes, header, "gbk");
        if (content != null) {
            return this.parseDailyIndexList4xinlang(content);
        }
        return Collections.emptyList();
    }


    /**
     * 0：新晨科技, 股票名字; 1：27.55″, 今日开盘价; 2：27.25″, 昨日收盘价; 3：26.91″, 当前价格; 4：27.55″,
     * 今日最高价; 5：26.20″, 今日最低价; 6：26.91″, 竞买价, 即“买一报价; 7：26.92″, 竞卖价, 即“卖一报价;
     * 8：22114263″ 成交金额
     *
     * @param content var hq_str_sh601288="农业银行,2.840,2.850,2.830,2.850,2.810,2.830,2.840,270504182,765825256.000,20503200,2.830,28078500,2.820,32757300,2.810,20226900,2.800,4955800,2.790,9961400,2.840,21828600,2.850,26505900,2.860,18669000,2.870,28754100,2.880,2022-08-12,15:00:00,00,";
     * @return
     */
    //
    private List<DailyIndex> parseDailyIndexList4xinlang(String content) {
        String[] dailyIndexListStr = content.split("\n");
        ArrayList<DailyIndex> DailyList = new ArrayList<>();
        for (String dailyIndexStr : dailyIndexListStr) {
            dailyIndexStr = dailyIndexStr.trim();
            if (dailyIndexStr.length() > 0) {
                String[] strs = dailyIndexStr.split(",");
                if (strs.length <= 1) {
                    continue;
                }
                String code = strs[0].substring(strs[0].lastIndexOf('_') + 3, strs[0].lastIndexOf('='));
                BigDecimal openingPrice = new BigDecimal(strs[1]);
                BigDecimal preClosingPrice = new BigDecimal(strs[2]);
                BigDecimal closingPrice = new BigDecimal(strs[3]);
                BigDecimal highestPrice = new BigDecimal(strs[4]);
                BigDecimal lowestPrice = new BigDecimal(strs[5]);
                BigDecimal tradingVolume = new BigDecimal(strs[8]);
                BigDecimal tradingValue = new BigDecimal(strs[9]);
                Date date = DateUtil.parseDate(strs[30]);

                DailyIndex dailyIndex = new DailyIndex();
                dailyIndex.setStockCode(code);
                dailyIndex.setOpenPrice(openingPrice);
                dailyIndex.setPreClosePrice(preClosingPrice);
                dailyIndex.setClosePrice(closingPrice);
                dailyIndex.setHighestPrice(highestPrice);
                dailyIndex.setLowestPrice(lowestPrice);
                dailyIndex.setTradeVolume(tradingVolume);
                dailyIndex.setTradeAmount(tradingValue);
                dailyIndex.setRurnoverRate(BigDecimal.ZERO);
                dailyIndex.setStockDate(new java.sql.Date(date.getTime()));

                DailyList.add(dailyIndex);
            }
        }
        return DailyList;
    }


    private DailyIndex parseDailyIndex(String content) {
        String[] strs = content.split(",");
        if (strs.length <= 1) {
            return null;
        }
        String code = strs[0].substring(strs[0].lastIndexOf('_') + 3, strs[0].lastIndexOf('='));
        BigDecimal openingPrice = new BigDecimal(strs[1]);
        BigDecimal preClosingPrice = new BigDecimal(strs[2]);
        BigDecimal closingPrice = new BigDecimal(strs[3]);
        BigDecimal highestPrice = new BigDecimal(strs[4]);
        BigDecimal lowestPrice = new BigDecimal(strs[5]);
        BigDecimal tradingVolume = new BigDecimal(strs[8]);
        BigDecimal tradingValue = new BigDecimal(strs[9]);
        Date date;
        try {
            date = DateUtils.parseDate(strs[30], "yyyy-MM-dd");
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
        DailyIndex dailyIndex = new DailyIndex();
        dailyIndex.setStockCode(code);
        dailyIndex.setOpenPrice(openingPrice);
        dailyIndex.setPreClosePrice(preClosingPrice);
        dailyIndex.setClosePrice(closingPrice);
        dailyIndex.setHighestPrice(highestPrice);
        dailyIndex.setLowestPrice(lowestPrice);
        dailyIndex.setTradeVolume(tradingVolume);
        dailyIndex.setTradeAmount(tradingValue);
        dailyIndex.setRurnoverRate(BigDecimal.ZERO);
        dailyIndex.setStockDate(new java.sql.Date(date.getTime()));

        return dailyIndex;
    }


    private void crawDailyIndexFromEastMoney(List<StockInfo> stockInfoList) {
        // 获取日线信息
        List<DailyIndex> dailyIndexList = this.getStockList("f2,f5,f6,f8,f12,f13,f14,f15,f16,f17,f18").stream().map(EmStock::getDailyIndex).collect(Collectors.toList());
        // 剔除不在t_stock_info的股票日线信息
        dailyIndexList = dailyIndexList.stream().filter(dailyIndex -> stockInfoList.stream().anyMatch(stockInfo -> dailyIndex.getStockCode().equals(stockInfo.getStockCode()))).collect(Collectors.toList());

        List<DailyIndex> dailyIndices = filterInvalid(dailyIndexList);

        this.saveDailyIndex(dailyIndices);

    }

    private void saveDailyIndex(List<DailyIndex> dailyIndices) {
        for (DailyIndex dailyIndex : dailyIndices) {
            dailyIndexMapper.insert(dailyIndex);
        }
    }

    private List<EmStock> getStockList(String fields) {
        String content = HttpUtil.get("http://20.push2.eastmoney.com/api/qt/clist/get?pn=1&pz=10000000&np=1&fid=f3&fields=" + fields + "&fs=m:0+t:6,m:0+t:13,m:0+t:80,m:0+t:81+s:2048,m:1+t:2,m:1+t:23,b:MK0021,b:MK0022,b:MK0023,b:MK0024");
        if (content != null) {
            List<EmStock> list = this.parseStockInfoList(content);
            list = list.stream().filter(v -> v.getStockInfo().getStockExchange() != null).collect(Collectors.toList());
            return list;
        }
        return Collections.emptyList();
    }

    private List<DailyIndex> filterInvalid(List<DailyIndex> dailyIndexList) {
        // 过滤掉没有开盘价、交易量、交易金额、不是当天的数据
        final String currentDateStr = DateUtil.format(new Date(), "yyyy-MM-dd");
        return dailyIndexList.stream().filter(dailyIndex ->
                DecimalUtil.bg(dailyIndex.getOpenPrice(), BigDecimal.ZERO)
                        && DecimalUtil.bg(dailyIndex.getTradeVolume(), BigDecimal.ZERO)
                        && DecimalUtil.bg(dailyIndex.getTradeAmount(), BigDecimal.ZERO)
                        && currentDateStr.equals(DateUtil.format(dailyIndex.getStockDate(), "yyyy-MM-dd"))
        ).collect(Collectors.toList());
    }

    public List<EmStock> parseStockInfoList(String content) {
        JSONObject result = JSON.parseObject(content);
        JSONObject data = (JSONObject) result.get("data");
        Integer total = (Integer) data.get("total");
        JSONArray diff = (JSONArray) data.get("diff");
        return diff.stream().map(v -> {
            JSONObject item = (JSONObject) v;
            String code = item.getString("f12");
            EmStock emStock = new EmStock();


            // StockInfo
            StockInfo stockInfo = new StockInfo();
            String exchange = item.getInteger("f13") == 0 ? StockUtil.getExchange(code) : StockConsts.Exchange.SH.getName();
            int type = StockUtil.getStockType(exchange, code);
            stockInfo.setStockExchange(exchange);
            stockInfo.setStockName(item.getString("f14"));
            stockInfo.setStockCode(code);
            stockInfo.setStockType(type);

            // DailyIndex
            Integer closePrice = item.getInteger("f2");
            Integer tradeVolume = item.getInteger("f5");
            Integer tradeAmount = item.getInteger("f6");
            Integer rurnoverRate = item.getInteger("f8");
            Integer highestPrice = item.getInteger("f15");
            Integer lowestPrice = item.getInteger("f16");
            Integer openPrice = item.getInteger("f17");
            Integer preClosePrice = item.getInteger("f18");

            DailyIndex dailyIndex = new DailyIndex();
            dailyIndex.setStockDate(new java.sql.Date(System.currentTimeMillis()));
            dailyIndex.setStockCode(stockInfo.getStockCode());
            dailyIndex.setClosePrice(null == closePrice ? null : new BigDecimal(closePrice).movePointLeft(2));
            dailyIndex.setTradeVolume(null == tradeVolume ? null : NumberUtil.mul(tradeVolume.toString(), "100"));
            dailyIndex.setTradeAmount(null == tradeAmount ? null : new BigDecimal(tradeAmount));
            dailyIndex.setRurnoverRate(null == rurnoverRate ? null : new BigDecimal(rurnoverRate).movePointLeft(2));
            dailyIndex.setHighestPrice(null == highestPrice ? null : new BigDecimal(highestPrice).movePointLeft(2));
            dailyIndex.setLowestPrice(null == lowestPrice ? null : new BigDecimal(lowestPrice).movePointLeft(2));
            dailyIndex.setOpenPrice(null == openPrice ? null : new BigDecimal(openPrice).movePointLeft(2));
            dailyIndex.setPreClosePrice(null == preClosePrice ? null : new BigDecimal(preClosePrice).movePointLeft(2));

            emStock.setStockInfo(stockInfo);
            emStock.setDailyIndex(dailyIndex);

            return emStock;
        }).collect(Collectors.toList());
    }

    public static class EmStock {

        private StockInfo stockInfo;
        private DailyIndex dailyIndex;

        public StockInfo getStockInfo() {
            return stockInfo;
        }

        public void setStockInfo(StockInfo stockInfo) {
            this.stockInfo = stockInfo;
        }

        public DailyIndex getDailyIndex() {
            return dailyIndex;
        }

        public void setDailyIndex(DailyIndex dailyIndex) {
            this.dailyIndex = dailyIndex;
        }

    }

}
