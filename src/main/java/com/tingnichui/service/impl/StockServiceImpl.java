package com.tingnichui.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.NamedThreadFactory;
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
import org.apache.commons.lang3.StringUtils;
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
            JSONArray list = data.getJSONArray("item");
            if (CollectionUtils.isNotEmpty(list)) {
                for (Object o : list) {
                    JSONArray array = (JSONArray) o;
                    // [(日期)1660147200000,(代码)11153736,(开盘价)12.4,(最高价)12.47,(最低价)12.24,(收盘价)12.39,()0.03,(涨幅)0.24,()1.88,()1.38009823E8,()500,()6195.0,()25.068,()3.395,()3.7807135754226975,()-67.82428147894169,()7.319522583E9,null,null,null,null,null,null,null]
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
    public Result saveDailyRecord4xueqiu() {
        if (DateUtil.hour(new Date(), true) >= 15) {
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
            this.updateStock4xueqiu();
        } else {
            return ResultGenerator.genSuccessResult("还未收盘");
            // 15点以前实时监控涨跌
//            this.updateStock();
        }

        return ResultGenerator.genSuccessResult("雪球-更新股票每日成交数据完成！");
    }

    @Resource
    private DailyIndexMapper dailyIndexMapper;

    @Resource
    private StockInfoMapper stockInfoMapper;

    @Override
    public Result saveDailyRecord4EastMoney() {
        // 获取退市的股票和指数
        List<StockInfo> list = stockInfoMapper.selectList(null).stream()
                .filter(stockInfo -> (stockInfo.getType() == 0 || stockInfo.getType() == 1) && stockInfo.getState() == 2)
                .collect(Collectors.toList());

        // 获取当天的日线
        List<DailyIndex> dailyIndexList = dailyIndexMapper.selectList(new LambdaQueryWrapper<DailyIndex>().eq(DailyIndex::getDate, new java.sql.Date(System.currentTimeMillis())));
        // 获取代码股票代码集合
        List<String> codeList = dailyIndexList.stream().map(DailyIndex::getCode).collect(Collectors.toList());
        // 剔除退市股票
        list = list.stream().filter(v -> !codeList.contains(this.getFullCode(v))).collect(Collectors.toList());


        crawDailyIndexFromSina(list.stream().filter(s -> s.getType() == 1).collect(Collectors.toList()));
        crawDailyIndexFromEastMoney(list);
        return ResultGenerator.genSuccessResult("东方财富-更新股票每日成交数据完成！");
    }

    private void crawDailyIndexFromSina(List<StockInfo> list) {
        final int tCount = 500;
        ArrayList<String> stockCodeList = new ArrayList<>(tCount);
        for (StockInfo stockInfo : list) {
            stockCodeList.add(this.getFullCode(stockInfo));
            if (stockCodeList.size() == tCount) {
                saveDailyIndex(stockCodeList);
                stockCodeList.clear();
            }
        }

        if (!stockCodeList.isEmpty()) {
            saveDailyIndex(stockCodeList);
        }
    }

    private String getFullCode(StockInfo stockInfo) {
        return stockInfo.getExchange() + stockInfo.getCode();
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
            return this.parseDailyIndexList(content);
        }
        return Collections.emptyList();
    }

    public List<DailyIndex> parseDailyIndexList(String content) {
        String[] str = content.split("\n");
        ArrayList<DailyIndex> list = new ArrayList<>();
        for (String c : str) {
            c = c.trim();
            if (c.length() > 0) {
                DailyIndex dailyIndex = parseDailyIndex(c);
                if (dailyIndex != null) {
                    list.add(dailyIndex);
                }
            }
        }
        return list;
    }

    /*
     * 0：新晨科技, 股票名字; 1：27.55″, 今日开盘价; 2：27.25″, 昨日收盘价; 3：26.91″, 当前价格; 4：27.55″,
     * 今日最高价; 5：26.20″, 今日最低价; 6：26.91″, 竞买价, 即“买一报价; 7：26.92″, 竞卖价, 即“卖一报价;
     * 8：22114263″ 成交金额
     */
    private DailyIndex parseDailyIndex(String content) {
        String[] strs = content.split(",");
        if (strs.length <= 1) {
            return null;
        }
        String code = strs[0].substring(strs[0].lastIndexOf('_') + 1, strs[0].lastIndexOf('='));
        BigDecimal openingPrice = new BigDecimal(strs[1]);
        BigDecimal preClosingPrice = new BigDecimal(strs[2]);
        BigDecimal closingPrice = new BigDecimal(strs[3]);
        BigDecimal highestPrice = new BigDecimal(strs[4]);
        BigDecimal lowestPrice = new BigDecimal(strs[5]);
        long tradingVolume = Long.parseLong(strs[8]);
        BigDecimal tradingValue = new BigDecimal(strs[9]);
        Date date;
        try {
            date = DateUtils.parseDate(strs[30], "yyyy-MM-dd" );
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
        DailyIndex dailyIndex = new DailyIndex();
        dailyIndex.setCode(code);
        dailyIndex.setOpeningPrice(openingPrice);
        dailyIndex.setPreClosingPrice(preClosingPrice);
        dailyIndex.setClosingPrice(closingPrice);
        dailyIndex.setHighestPrice(highestPrice);
        dailyIndex.setLowestPrice(lowestPrice);
        dailyIndex.setTradingVolume(tradingVolume);
        dailyIndex.setTradingValue(tradingValue);
        dailyIndex.setRurnoverRate(BigDecimal.ZERO);
        dailyIndex.setDate(new java.sql.Date(date.getTime()));

        return dailyIndex;
    }


    private void crawDailyIndexFromEastMoney(List<StockInfo> list){
        List<DailyIndex> dailyIndexList = this.getDailyIndexFromEastMoney();
        dailyIndexList = dailyIndexList.stream().filter(d -> list.stream().anyMatch(s -> d.getCode().equals(this.getFullCode(s)))).collect(Collectors.toList());
        List<DailyIndex> dailyIndices = filterInvalid(dailyIndexList);

        this.saveDailyIndex(dailyIndices);

    }

    private void saveDailyIndex(List<DailyIndex> dailyIndices) {
        for (DailyIndex dailyIndex : dailyIndices){
            dailyIndexMapper.insert(dailyIndex);
        }
    }

    public List<DailyIndex> getDailyIndexFromEastMoney() {
        List<EmStock> list = this.getStockList("f2,f5,f6,f8,f12,f13,f15,f16,f17,f18");
        return list.stream().map(EmStock::getDailyIndex).collect(Collectors.toList());
    }

    private List<EmStock> getStockList(String fields) {
        String content = HttpUtil.get("http://20.push2.eastmoney.com/api/qt/clist/get?pn=1&pz=10000000&np=1&fid=f3&fields=" + fields + "&fs=m:0+t:6,m:0+t:13,m:0+t:80,m:0+t:81+s:2048,m:1+t:2,m:1+t:23,b:MK0021,b:MK0022,b:MK0023,b:MK0024");
        if (content != null) {
            List<EmStock> list = this.parseStockInfoList(content);
            list = list.stream().filter(v -> v.getStockInfo().getExchange() != null).collect(Collectors.toList());
            return list;
        }
        return Collections.emptyList();
    }

    private List<DailyIndex> filterInvalid(List<DailyIndex> dailyIndexList) {
        final String currentDateStr = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
        return dailyIndexList.stream().filter(dailyIndex ->
                DecimalUtil.bg(dailyIndex.getOpeningPrice(), BigDecimal.ZERO)
                        && dailyIndex.getTradingVolume() > 0
                        && DecimalUtil.bg(dailyIndex.getTradingValue(), BigDecimal.ZERO)
                        && currentDateStr.equals(DateFormatUtils.format(dailyIndex.getDate(), "yyyy-MM-dd"))
        ).collect(Collectors.toList());
    }

    public List<EmStock> parseStockInfoList(String content) {
        char[] chArr = content.toCharArray();
        char[] newCharArr = new char[chArr.length];
        int i = 0;
        for (char ch : chArr) {
            if (ch == ' ') {
                continue;
            }
            if (ch == 'Ａ') {
                ch = 'A';
            } else if (ch == 'Ｂ') {
                ch = 'B';
            }
            newCharArr[i++] = ch;
        }

        StockResultVo stockResultVo = JSON.parseObject(new String(newCharArr, 0, i), StockResultVo.class);

        // {"f12":"000718","f13":0,"f14":"苏宁环球"}
        return stockResultVo.getData().getDiff().stream().map(v -> {
            String code = v.getF12();
            EmStock emStock = new EmStock();

            StockInfo stockInfo = new StockInfo();
            String exchange = v.getF13() == 0 ? StockUtil.getExchange(code) : StockConsts.Exchange.SH.getName();
            int type = StockUtil.getStockType(exchange, code);
            stockInfo.setExchange(exchange);
            stockInfo.setName(v.getF14());
            stockInfo.setCode(code);
            stockInfo.setExchange(exchange);
            stockInfo.setType(type);

            DailyIndex dailyIndex = new DailyIndex();
            dailyIndex.setDate(new java.sql.Date(System.currentTimeMillis()));
            dailyIndex.setCode(stockInfo.getExchange() + stockInfo.getCode());
            dailyIndex.setClosingPrice(new BigDecimal(v.getF2()).movePointLeft(2));
            dailyIndex.setTradingVolume(v.getF5() * 100);
            dailyIndex.setTradingValue(new BigDecimal(v.getF6()));
            dailyIndex.setRurnoverRate(new BigDecimal(v.getF8()).movePointLeft(2));
            dailyIndex.setHighestPrice(new BigDecimal(v.getF15()).movePointLeft(2));
            dailyIndex.setLowestPrice(new BigDecimal(v.getF16()).movePointLeft(2));
            dailyIndex.setOpeningPrice(new BigDecimal(v.getF17()).movePointLeft(2));
            dailyIndex.setPreClosingPrice(new BigDecimal(v.getF18()).movePointLeft(2));

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

    public static class StockResultVo {

        private StockResultDataVo data;

        public StockResultDataVo getData() {
            return data;
        }

        public void setData(StockResultDataVo data) {
            this.data = data;
        }
    }

    public static class StockResultDataVo {

        private List<StockResultDiffVo> diff;

        public List<StockResultDiffVo> getDiff() {
            return diff;
        }

        public void setDiff(List<StockResultDiffVo> diff) {
            this.diff = diff;
        }

    }

    public static class StockResultDiffVo {

        private int f1;
        private int f2;
        private int f3;
        private int f4;
        private long f5;
        private double f6;
        private int f7;
        private int f8;
        private int f9;
        private int f10;
        private int f11;
        private String f12;
        private int f13;
        private String f14;
        private int f15;
        private int f16;
        private int f17;
        private int f18;
        private int f20;
        private int f21;
        private int f22;
        private int f23;
        private int f24;
        private int f25;
        private double f62;

        public int getF1() {
            return f1;
        }

        public void setF1(int f1) {
            this.f1 = f1;
        }

        public int getF2() {
            return f2;
        }

        public void setF2(int f2) {
            this.f2 = f2;
        }

        public int getF3() {
            return f3;
        }

        public void setF3(int f3) {
            this.f3 = f3;
        }

        public int getF4() {
            return f4;
        }

        public void setF4(int f4) {
            this.f4 = f4;
        }

        public long getF5() {
            return f5;
        }

        public void setF5(long f5) {
            this.f5 = f5;
        }

        public double getF6() {
            return f6;
        }

        public void setF6(double f6) {
            this.f6 = f6;
        }

        public int getF7() {
            return f7;
        }

        public void setF7(int f7) {
            this.f7 = f7;
        }

        public int getF8() {
            return f8;
        }

        public void setF8(int f8) {
            this.f8 = f8;
        }

        public int getF9() {
            return f9;
        }

        public void setF9(int f9) {
            this.f9 = f9;
        }

        public int getF10() {
            return f10;
        }

        public void setF10(int f10) {
            this.f10 = f10;
        }

        public int getF11() {
            return f11;
        }

        public void setF11(int f11) {
            this.f11 = f11;
        }

        public String getF12() {
            return f12;
        }

        public void setF12(String f12) {
            this.f12 = f12;
        }

        public int getF13() {
            return f13;
        }

        public void setF13(int f13) {
            this.f13 = f13;
        }

        public String getF14() {
            return f14;
        }

        public void setF14(String f14) {
            this.f14 = f14;
        }

        public int getF15() {
            return f15;
        }

        public void setF15(int f15) {
            this.f15 = f15;
        }

        public int getF16() {
            return f16;
        }

        public void setF16(int f16) {
            this.f16 = f16;
        }

        public int getF17() {
            return f17;
        }

        public void setF17(int f17) {
            this.f17 = f17;
        }

        public int getF18() {
            return f18;
        }

        public void setF18(int f18) {
            this.f18 = f18;
        }

        public int getF20() {
            return f20;
        }

        public void setF20(int f20) {
            this.f20 = f20;
        }

        public int getF21() {
            return f21;
        }

        public void setF21(int f21) {
            this.f21 = f21;
        }

        public int getF22() {
            return f22;
        }

        public void setF22(int f22) {
            this.f22 = f22;
        }

        public int getF23() {
            return f23;
        }

        public void setF23(int f23) {
            this.f23 = f23;
        }

        public int getF24() {
            return f24;
        }

        public void setF24(int f24) {
            this.f24 = f24;
        }

        public int getF25() {
            return f25;
        }

        public void setF25(int f25) {
            this.f25 = f25;
        }

        public double getF62() {
            return f62;
        }

        public void setF62(double f62) {
            this.f62 = f62;
        }

    }

}
