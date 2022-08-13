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
import com.tingnichui.dao.DailyRecordMapper;
import com.tingnichui.dao.StockInfoMapper;
import com.tingnichui.dao.StockMapper;
import com.tingnichui.pojo.po.DailyIndex;
import com.tingnichui.pojo.po.DailyRecord;
import com.tingnichui.pojo.po.Stock;
import com.tingnichui.pojo.po.StockInfo;
import com.tingnichui.pojo.vo.Result;
import com.tingnichui.service.StockService;
import com.tingnichui.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
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
            String url = xueQiuDetailUrl.replace("{code}", code)
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
                    DailyRecord dailyRecord = new DailyRecord();
                    dailyRecord.setDate(new Date((Long) map.get("timestamp")));
                    dailyRecord.setCode(code);
                    dailyRecord.setName(name);
                    dailyRecord.setOpenPrice((BigDecimal) map.get("open"));
                    dailyRecord.setHighest((BigDecimal) map.get("high"));
                    dailyRecord.setLowest((BigDecimal) map.get("low"));
                    dailyRecord.setClosePrice((BigDecimal) map.get("close"));
                    dailyRecord.setIncreaseRate((BigDecimal) map.get("percent"));
                    dailyRecord.setAmount(NumberUtil.div((BigDecimal) map.get("amount"), new BigDecimal(10000), 2));
                    dailyRecordMapper.insert(dailyRecord);
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
    public static final HashMap<String, Stock> TRACK_STOCK_MAP = new HashMap<>();

    @Override
    public Result saveDailyRecord4xueqiu() {
//        if (DateUtil.hour(new Date(), true) >= 15) {
        List<Stock> stocks = stockMapper.selectList(new QueryWrapper<Stock>().notLike("name", "%ST%")
                .notLike("name", "%st%").notLike("name", "%A%").notLike("name", "%C%").notLike("name", "%N%")
                .notLike("name", "%U%").notLike("name", "%W%").notLike("code", "%BJ%").notLike("code", "%688%"));
        stocks.forEach(e -> {
            if (!e.getIgnoreMonitor() && (e.getShareholding() || e.getTrack())) {
                TRACK_STOCK_MAP.put(e.getName(), e);
            }
            STOCK_MAP.put(e.getCode(), e.getName());
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

    @Resource
    private DailyIndexMapper dailyIndexMapper;

    @Resource
    private StockInfoMapper stockInfoMapper;

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


        crawDailyIndexFromSina(stockInfoList.stream().filter(s -> s.getStockType() == 1).collect(Collectors.toList()));
        crawDailyIndexFromEastMoney(stockInfoList);
        return ResultGenerator.genSuccessResult("东方财富-更新股票每日成交数据完成！");
    }

    private Map<String, BigDecimal> lastPriceMap = new HashMap<>();

    @Override
    public Result monitorStock() {
        // 新浪
        List<StockInfo> selectList = stockInfoMapper.selectList(new LambdaQueryWrapper<StockInfo>().le(StockInfo::getId, 221));
        List<String> codeList = selectList.stream().map(v -> StockUtil.getFullCode(v.getStockCode())).collect(Collectors.toList());
        List<DailyIndex> dailyIndexList = this.getDailyIndex(codeList);


        StringBuilder sb = new StringBuilder();
        for (StockInfo stockInfo : selectList) {
            String code = stockInfo.getStockCode();
            DailyIndex dailyIndex = dailyIndexList.stream().filter(d -> d.getStockCode().contains(stockInfo.getStockCode())).findAny().orElse(null);
            if (dailyIndex == null) {
                continue;
            }
            if (lastPriceMap.containsKey(code)) {
                BigDecimal lastPrice = lastPriceMap.get(code);
                double rate = Math.abs(StockUtil.calcIncreaseRate(dailyIndex.getClosePrice(), lastPrice).doubleValue());
//                if (Double.compare(rate, stockInfo.getRate().doubleValue()) >= 0) {
                lastPriceMap.put(code, dailyIndex.getClosePrice());
                String name = "";
                String body = String.format("%s:当前价格:%.02f, 涨幅%.02f%%", name,
                        dailyIndex.getClosePrice().doubleValue(),
                        StockUtil.calcIncreaseRate(dailyIndex.getClosePrice(),
                                dailyIndex.getPreClosePrice()).movePointRight(2).doubleValue());
                sb.append(body + "\n");
//                }
            } else {
                lastPriceMap.put(code, dailyIndex.getPreClosePrice());
                String name = "";
                String body = String.format("%s:当前价格:%.02f", name, dailyIndex.getClosePrice().doubleValue());
                sb.append(body + "\n");
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
            DingdingUtil.sendMsg(sb.toString());
        }
        return ResultGenerator.genSuccessResult("东方财富-股价实时检测中！");
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
        this.saveDailyIndex(filterInvalid(dailyIndexList));
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
                long tradingVolume = Long.parseLong(strs[8]);
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
        long tradingVolume = Long.parseLong(strs[8]);
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
        List<DailyIndex> dailyIndexList = this.getDailyIndexFromEastMoney();
        dailyIndexList = dailyIndexList.stream().filter(dailyIndex -> stockInfoList.stream().anyMatch(stockInfo -> dailyIndex.getStockCode().equals(stockInfo.getStockCode()))).collect(Collectors.toList());
        List<DailyIndex> dailyIndices = filterInvalid(dailyIndexList);

        this.saveDailyIndex(dailyIndices);

    }

    private void saveDailyIndex(List<DailyIndex> dailyIndices) {
        for (DailyIndex dailyIndex : dailyIndices) {
            dailyIndexMapper.insert(dailyIndex);
        }
    }

    public List<DailyIndex> getDailyIndexFromEastMoney() {
        List<EmStock> list = this.getStockList("f2,f5,f6,f8,f12,f13,f14,f15,f16,f17,f18");
        return list.stream().map(EmStock::getDailyIndex).collect(Collectors.toList());
    }

    private List<EmStock> getStockList(String fields) {
        String content = HttpUtil.get("http://20.push2.eastmoney.com/api/qt/clist/get?pn=1&pz=10000000&np=1&fid=f3&fields=" + fields + "&fs=m:0+t:6,m:0+t:13,m:0+t:80,m:0+t:81+s:2048,m:1+t:2,m:1+t:23,b:MK0021,b:MK0022,b:MK0023,b:MK0024");
        if (content != null) {
            List<EmStock> list = this.parseStockInfoList(content);
            // 保存stockInfo
//            for (EmStock emStock : list) {
//                stockInfoMapper.insert(emStock.getStockInfo());
//            }

            list = list.stream().filter(v -> v.getStockInfo().getStockExchange() != null).collect(Collectors.toList());
            return list;
        }
        return Collections.emptyList();
    }

    private List<DailyIndex> filterInvalid(List<DailyIndex> dailyIndexList) {
        final String currentDateStr = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
        return dailyIndexList.stream().filter(dailyIndex ->
                DecimalUtil.bg(dailyIndex.getOpenPrice(), BigDecimal.ZERO)
                        && dailyIndex.getTradeVolume() > 0
                        && DecimalUtil.bg(dailyIndex.getTradeAmount(), BigDecimal.ZERO)
                        && currentDateStr.equals(DateFormatUtils.format(dailyIndex.getStockDate(), "yyyy-MM-dd"))
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
            DailyIndex dailyIndex = new DailyIndex();
            dailyIndex.setStockDate(new java.sql.Date(System.currentTimeMillis()));
            dailyIndex.setStockCode(stockInfo.getStockCode());
            dailyIndex.setClosePrice(new BigDecimal(item.getInteger("f2").toString()).movePointLeft(2));
            dailyIndex.setTradeVolume((long) (item.getInteger("f5") * 100));
            dailyIndex.setTradeAmount(new BigDecimal(item.getInteger("f6").toString()));
            dailyIndex.setRurnoverRate(new BigDecimal(item.getInteger("f8").toString()).movePointLeft(2));
            dailyIndex.setHighestPrice(new BigDecimal(item.getInteger("f15").toString()).movePointLeft(2));
            dailyIndex.setLowestPrice(new BigDecimal(item.getInteger("f16").toString()).movePointLeft(2));
            dailyIndex.setOpenPrice(new BigDecimal(item.getInteger("f17").toString()).movePointLeft(2));
            dailyIndex.setPreClosePrice(new BigDecimal(item.getInteger("f18").toString()).movePointLeft(2));

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
