/*
 * DateUtil.java 2008-4-3
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 常用日期函数工具类
 * 
 * @author TonyTan
 * @version 1.0, 2008-4-3
 */
public class DateUtil {
	
	/**
	 * 获取指定格式的当前日期的字符串
	 * @param format
	 * @return String
	 */
	public static String getDateValue(String format) {
		return new SimpleDateFormat(format).format(new Date());
	}

	/**
	 * 获取当前日期与时间，格式为：yyyy-mm-dd hh:mi:ss.
	 * 
	 * @return String
	 */
	public static String getTodaySec() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").
					format(new Date());
	}
	
	/**
	 * 获取当前日期，格式为：yyyy-mm-dd.
	 * 
	 * @return String
	 */
	public static String getToday() {
		return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	}
	
	/**
	 * 获取当前年月，格式为：yyyy-mm.
	 * 
	 * @return String
	 */
	public static String getCurrYearMonth() {
		String today = getToday();
		return today.substring(0, today.length() - 3);
	}
	
	/**
	 * 获取月份开始日期及结束日期，开始日期是一月的第一天，结束日期是下月的第一天。
	 * 如：2011-10-01, 2011-11-01
	 * 
	 * @param month -- 月份值，格式为：yyyy-mm.
	 * @return String[] -- 月份开始日期及结束日期，格式为：yyyy-mm-dd.
	 */
	public static String[] getDateByMonth(String month){
		String[] rtn = new String[2];
		
		rtn[0] = month + "-01";
		rtn[1] = dateAddMonth(rtn[0], 1);
	
		return rtn;
	}
	
	/**
	 * 获取日期加减月份数后的日期值
	 * 
	 * @param date -- 日期值，格式为：yyyy-mm-dd.
	 * @param month -- 间隔月份，整数值
	 * @return 返回间隔月份的日期，格式为：yyyy-mm-dd.
	 */
	public static String dateAddMonth(String date, int month){
		Calendar calendar = strToCalendar(date);
		
		calendar.add(Calendar.MONTH, month);
		
		return calendarToDate(calendar);
	}
	
	/**
	 * 获取月份值加减月份数后的月份值
	 * 
	 * @param curMonth -- 当前月份值，格式为：yyyy-mm.
	 * @param month -- 间隔月份，整数值
	 * @return 返回间隔月份值，格式为：yyyy-mm.
	 */
	public static String getAddMonth(String curMonth, int month){
		String preDate = DateUtil.dateAddMonth(curMonth+"-01", month);
		String[] smonth = preDate.split("-");
		
		return smonth[0] + "-" + smonth[1];
	}
	
	/**
	 * 日期字符串转换为日历对象
	 * 
	 * @param strDate -- 日期值，格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static Calendar strToCalendar(String strDate) {
		Calendar calendar = Calendar.getInstance();
		
		//小时、分钟、秒钟
		int h = 0, m = 0, s = 0;
		
		String[] sdt = strDate.split(" ");
		if (sdt.length > 1) {
			String[] st = sdt[1].split(":");
			if (st.length > 0) h = Integer.parseInt(st[0]);
			if (st.length > 1) m = Integer.parseInt(st[1]);
			if (st.length > 2) s = Integer.parseInt(st[2]);
		}
		
		//年、月、日
		int year = 0, month = 0, day = 0;
		
		String[] sd = sdt[0].split("-");
		if (sd.length > 0) year = Integer.parseInt(sd[0]);
		if (sd.length > 1) month = Integer.parseInt(sd[1]) - 1;
		if (sd.length > 2) day = Integer.parseInt(sd[2]);
		
		//设置时间值
		calendar.set(year, month, day, h, m, s);
		
		return calendar;
	}
	
	/**
	 * 日历对象转换为日期字符串，格式：yyyy-mm-dd
	 * 
	 * @param calendar -- 日历对象
	 * @return
	 */
	public static String calendarToDate(Calendar calendar) {
		return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
	}
	
	/**
	 * 日历对象转换为日期字符串，格式：yyyy-mm-dd hh:mi:ss
	 * 
	 * @param calendar -- 日历对象
	 * @return
	 */
	public static String calendarToDateTime(Calendar calendar) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
	}
}
