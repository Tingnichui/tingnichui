package com.tingnichui.util;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author Geng Hui
 * @date 2022/8/25 9:34
 */
@Component
public class StockUtil {

    @Resource
    private RedisUtil redisUtil;

    private static final List<String> CODES_SH_A = Arrays.asList("600", "601", "603", "605", "688", "689");
    private static final List<String> CODES_SH_INDEX = Arrays.asList("000001");
    private static final List<String> CODES_SH_ETF = Arrays.asList("51", "56", "58");

    private static final List<String> CODES_SZ_A = Arrays.asList("000", "001", "002", "003", "004", "300", "301");
    private static final List<String> CODES_SZ_INDEX = Arrays.asList("399001", "399006");
    private static final List<String> CODES_SZ_ETF = Arrays.asList("15");

    private static final List<String> CODES_BJ_A = Arrays.asList("83", "87", "43");
    private static final List<String> CODES_BJ_INDEX = Arrays.asList();
    private static final List<String> CODES_BJ_ETF = Arrays.asList();

    private StockUtil() {
    }

    public boolean isStockTradeTime(Date date) {
        if (date == null) {
            date = new Date();
        }

        boolean isBusinessDate = this.isStockTradeDate(date);
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

    public boolean isStockTradeDate(Date date) {

        if (date == null) {
            date = new Date();
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int day = c.get(Calendar.DAY_OF_WEEK);
        if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
            return false;
        }
        List<String> dateList = this.getHolidayList();

        return !dateList.contains(DateUtil.format(date, "yyyyMMdd"));
    }


    private List<String> getHolidayList() {

        List<String> holidayList = redisUtil.getCacheObject("HOLIDAY_LIST");

        if (Objects.nonNull(holidayList)) {
            return holidayList;
        }

        int year = Calendar.getInstance().get(Calendar.YEAR);
        String content = HttpUtil.get("http://tool.bitefu.net/jiari/?d=" + year);
        if (org.apache.commons.lang3.StringUtils.isBlank(content)) {
            return Collections.emptyList();
        }
        JSONObject jsonObject = JSON.parseObject(content);

        Map<String, Integer> dateInfo = (Map<String, Integer>) jsonObject.get(String.valueOf(year));
        holidayList = dateInfo.entrySet().stream().filter(entry -> entry.getValue() != 0).map(entry -> {
            Date date;
            try {
                date = DateUtils.parseDate(year + entry.getKey(), "yyyyMMdd");
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
            return DateUtil.format(date, "yyyyMMdd");
        }).collect(Collectors.toList());

        if (!holidayList.isEmpty()) {
            redisUtil.setCacheObject("HOLIDAY_LIST", holidayList, 10, TimeUnit.HOURS);
        }

        return holidayList;
    }


    public static String getExchange(String code) {
        if (!StringUtils.hasLength(code)) {
            return null;
        }
        if (isCodeStart(code, CODES_SH_A, CODES_SH_ETF)) {
            return StockConsts.Exchange.SH.getName();
        }
        if (isCodeStart(code, CODES_SZ_A, CODES_SZ_ETF)) {
            return StockConsts.Exchange.SZ.getName();
        }
        if (isCodeStart(code, CODES_BJ_A, CODES_BJ_ETF)) {
            return StockConsts.Exchange.BJ.getName();
        }
        return null;
    }

    public static String getFullCode(String code) {
        if (!StringUtils.hasLength(code)) {
            return null;
        }
        String exchange = StockUtil.getExchange(code);
        if (exchange == null) {
            return code;
        }
        return exchange + code;
    }

    public static int getStockType(String exchange, String code) {
        if (exchange == null) {
            exchange = StockUtil.getExchange(code);
        }
        if (StockConsts.Exchange.valueOfName(exchange).isSh()) {
            if (CODES_SH_INDEX.contains(code)) {
                return StockConsts.StockType.Index.value();
            }
        } else if (StockConsts.Exchange.valueOfName(exchange).isSz()) {
            if (CODES_SZ_INDEX.contains(code)) {
                return StockConsts.StockType.Index.value();
            }
        } else {
            if (CODES_BJ_INDEX.contains(code)) {
                return StockConsts.StockType.Index.value();
            }
        }

        if (isCodeStart(code, CODES_SH_A, CODES_SZ_A, CODES_BJ_A)) {
            return StockConsts.StockType.A.value();
        }
        if (isCodeStart(code, CODES_SH_ETF, CODES_SZ_ETF, CODES_BJ_ETF)) {
            return StockConsts.StockType.ETF.value();
        }
        throw new NoSuchElementException("no stock type exchange " + exchange + ", code " + code);
    }


    public static BigDecimal calcIncreaseRate(BigDecimal a, BigDecimal b) {
        return a.subtract(b).divide(b, 6, BigDecimal.ROUND_HALF_UP);
    }

    public static boolean isOriName(String name) {
        for (String namePrefix : Arrays.asList("N", "XD", "XR", "DR")) {
            if (name.startsWith(namePrefix)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isCodeStart(String code, List<String> list) {
        return list.stream().anyMatch(code::startsWith);
    }

    private static boolean isCodeStart(String code, List<String> list01, List<String> list02) {
        return isCodeStart(code, list01) || isCodeStart(code, list02);
    }

    private static boolean isCodeStart(String code, List<String> list01, List<String> list02, List<String> list03) {
        return isCodeStart(code, list01, list02) || isCodeStart(code, list03);
    }

}