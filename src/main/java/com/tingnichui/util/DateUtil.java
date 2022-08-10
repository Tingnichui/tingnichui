package com.tingnichui.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	
    private final static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss:SSS";

    private static final long ONE_MINUTE = 60000L;
    private static final long ONE_HOUR = 3600000L;
    private static final long ONE_DAY = 86400000L;
    private static final long ONE_WEEK = 604800000L;

    private static final String ONE_SECOND_AGO = "秒前";
    private static final String ONE_MINUTE_AGO = "分钟前";
    private static final String ONE_HOUR_AGO = "小时前";
    private static final String ONE_DAY_AGO = "天前";
    private static final String ONE_MONTH_AGO = "月前";
    private static final String ONE_YEAR_AGO = "年前";

    /**
     * 获取时间字符串
     */
    public static String format(Date date, String pattern) {
        if(date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            return df.format(date);
        }
        return null;
    }

    /**
     * 计算2个日期之间的差值
     * @param type s秒，m分钟，h小时，d天
     * @return
     */
    public static int getDateDiff(Date beforeDate, Date afterDate, String type) {
        if(beforeDate != null && afterDate != null) {
            if ("s".equals(type)) return ((Long)((afterDate.getTime() - beforeDate.getTime()) / (1000))).intValue();
            if ("m".equals(type)) return ((Long)((afterDate.getTime() - beforeDate.getTime()) / (1000 * 60))).intValue();
            if ("h".equals(type)) return ((Long)((afterDate.getTime() - beforeDate.getTime()) / (1000 * 60 * 60))).intValue();
            if ("d".equals(type)) return ((Long)((afterDate.getTime() - beforeDate.getTime()) / (1000 * 60 * 60 * 24))).intValue();
        }
        return 0;
    }



    /**
     * 根据字符串获取日期
     */
    public static Date getDate(String dateStr) throws ParseException {
        if(dateStr != null) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return df.parse(dateStr);
        }
        return null;
    }

    /**
     * 根据字符串获取日期
     */
    public static Date getDate(String dateStr,String pattern) throws ParseException {
        if(dateStr != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            return df.parse(dateStr);
        }
        return null;
    }
    
    /**
     * @param date 初始日期
     * @param days 新增天数
     */
    public static Date addDays(String date,Integer days) throws Exception {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cd = Calendar.getInstance();
		cd.setTime(df.parse(date));
		cd.add(Calendar.DATE,days);
		return cd.getTime();
    }

    /**
     * @param date 初始日期
     * @param days 新增天数
     */
    public static Date addDays(Date date,Integer days) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        cd.add(Calendar.DATE,days);
        return cd.getTime();
    }

    /**
     * @param date 初始日期
     * @param months 新增月数
     */
    public static Date addDate(Date date,Integer months) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        cd.add(Calendar.MONTH,months);
        return cd.getTime();
    }

    /**
     * @param date 初始日期
     * @param minutes 新增分钟数
     */
    public static Date addMinutes(Date date,Integer minutes) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        cd.add(Calendar.MINUTE,minutes);
        return cd.getTime();
    }

    /**
     * 获取本月最后一天
     * @return
     */
    public static Date getMonthLastDay(Date date) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        cd.set(Calendar.DAY_OF_MONTH, cd.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cd.getTime();
    }

    /**
     * 获取本月第一天
     * @return
     */
    public static Date getMonthFirstDay(Date date) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        cd.set(Calendar.DAY_OF_MONTH, cd.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cd.getTime();
    }

    public static void main(String[] args) throws ParseException {
        System.out.println(format(getMonthLastDay(DateUtil.addDate(new Date(),1)),"yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 多久之前
     */
    public static String howlong(Date date) {
        long delta = new Date().getTime() - date.getTime();
        if (delta < 1L * ONE_MINUTE) {
            long seconds = toSeconds(delta);
            return (seconds <= 0 ? 1 : seconds) + ONE_SECOND_AGO;
        }
        if (delta < 45L * ONE_MINUTE) {
            long minutes = toMinutes(delta);
            return (minutes <= 0 ? 1 : minutes) + ONE_MINUTE_AGO;
        }
        if (delta < 24L * ONE_HOUR) {
            long hours = toHours(delta);
            return (hours <= 0 ? 1 : hours) + ONE_HOUR_AGO;
        }
        if (delta < 48L * ONE_HOUR) {
            return "昨天";
        }
        if (delta < 30L * ONE_DAY) {
            long days = toDays(delta);
            return (days <= 0 ? 1 : days) + ONE_DAY_AGO;
        }
        if (delta < 12L * 4L * ONE_WEEK) {
            long months = toMonths(delta);
            return (months <= 0 ? 1 : months) + ONE_MONTH_AGO;
        } else {
            long years = toYears(delta);
            return (years <= 0 ? 1 : years) + ONE_YEAR_AGO;
        }
    }
    private static long toSeconds(long date) {
        return date / 1000L;
    }
    private static long toMinutes(long date) {
        return toSeconds(date) / 60L;
    }
    private static long toHours(long date) {
        return toMinutes(date) / 60L;
    }
    private static long toDays(long date) {
        return toHours(date) / 24L;
    }
    private static long toMonths(long date) {
        return toDays(date) / 30L;
    }
    private static long toYears(long date) {
        return toMonths(date) / 365L;
    }





}
