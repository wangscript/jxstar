/*
 * DatePlan.java 2011-1-13
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.task;

import java.util.Calendar;
import java.util.Date;

import org.jxstar.util.resource.JsMessage;

/**
 * 判断是否满足时间计划的条件，计划字符如下：
 * 	min=nm
	hour=nh,nm
	day=nd,nt
	week=nw,nd,nt
	month=nm,nd,nt
	monthw=nm,nw,nd,nt
	year=ny,nm,nd,nt
	yearw=ny,nw,nd,nt
	对应的中文解释是，上面的n对应下面的【】：
	每【】分钟
	每【】小时，第【】分钟
	每【】天，时间是【】
	每【】周，星期几【】，时间是【】
	每【】个月，日期是【】，时间是【】
	每【】个月，哪一周【】，星期几【】，时间是【】
	每【】年，月份是【】，日期是【】，时间是【】
	每【】年，哪一周【】，星期几【】，时间是【】
	
 * 不同类型的时间计划将采用不同的方法判断是否有效；
 * 比较的开始时间点为间隔单位初始值，如：间隔天数，则开始点是当前日期的零点；
 * 间隔周数，则开始点是周日的零点...
 * 当前时间与开始时间点的间隔超过计划时，也算有效；
 *
 * @author TonyTan
 * @version 1.0, 2011-1-13
 */
public class DatePlan {
	//检查间隔，在判断时间是否生效时将忽略时间，单位：分钟
	//private int _checkDiff = 1;
	
	/**
	 * 是否到间隔时间
	 * @param startDate -- 开始时间
	 * @param datePlan -- 执行计划
	 * @return
	 */
	public boolean isValid(Date startDate, String datePlan) throws TaskException {
		String planFlag = datePlan.split("=")[0];
		
		if (planFlag.equals("min")) {
			return minuteDiff(startDate, datePlan);
		} else if (planFlag.equals("hour")) {
			return hourDiff(startDate, datePlan);
		} else if (planFlag.equals("day")) {
			return dayDiff(startDate, datePlan);
		} else if (planFlag.equals("week")) {
			return weekDiff(startDate, datePlan);
		} else if (planFlag.equals("month")) {
			return monthDiff(startDate, datePlan);
		} else if (planFlag.equals("monthw")) {
			return monthwDiff(startDate, datePlan);
		} else if (planFlag.equals("year")) {
			return yearDiff(startDate, datePlan);
		} else if (planFlag.equals("yearw")) {
			return yearwDiff(startDate, datePlan);
		}
		
		return false;
	}
	
	/**
	 * 取下一个有效时间
	 * @param startDate -- 开始时间
	 * @param datePlan -- 执行计划
	 * @return
	 */
	public Date nextValidDate(Date startDate, String datePlan) throws TaskException {
		String planFlag = datePlan.split("=")[0];
		
		if (planFlag.equals("min")) {
			return minuteNextDate(startDate, datePlan);
		} else if (planFlag.equals("hour")) {
			return hourNextDate(startDate, datePlan);
		} else if (planFlag.equals("day")) {
			return dayNextDate(startDate, datePlan);
		} else if (planFlag.equals("week")) {
			return weekNextDate(startDate, datePlan);
		} else if (planFlag.equals("month")) {
			return monthNextDate(startDate, datePlan);
		} else if (planFlag.equals("monthw")) {
			return monthwNextDate(startDate, datePlan);
		} else if (planFlag.equals("year")) {
			return yearNextDate(startDate, datePlan);
		} else if (planFlag.equals("yearw")) {
			return yearwNextDate(startDate, datePlan);
		}
		
		return new Date();
	}

	/**
	 * 设置：每【】分钟
	 * 解释：判断是否间隔n分钟
	 * @param startDate -- 开始时间
	 * @param dataPlan -- 时间计划，格式：min=nm
	 * @return
	 */
	private boolean minuteDiff(Date startDate, String dataPlan) throws TaskException {
		//取分钟数
		int value = Integer.parseInt(getValues(dataPlan)[0]);
		
		//取当前时间
		Date curDate = new Date();
		
		//取两个时间差，单位毫秒
		long diff = curDate.getTime() - startDate.getTime();
		//计算间隔分钟数
		int dm = (int) diff/1000/60;
		
		//System.out.println("=========dm=" + dm + "; value=" + value);
				
		return dm >= value;
	}
	
	private Date minuteNextDate(Date startDate, String dataPlan) throws TaskException {
		//取分钟数
		int value = Integer.parseInt(getValues(dataPlan)[0]);

		//转换为毫秒值
		int dm = (int) value*1000*60;
		//下一个时间值
		long dv = startDate.getTime() + dm;
		
		//取下一个时间
		Date nextDate = new Date();
		nextDate.setTime(dv);
		
		return nextDate;
	}
	
	/**
	 * 设置：每【】小时，第【】分钟 
	 * 解释：判断是否间隔n小时，到第n分钟
	 * @param startDate -- 开始时间
	 * @param dataPlan -- 时间计划，格式：hour=nh,nm
	 * @return
	 */
	private boolean hourDiff(Date startDate, String dataPlan) throws TaskException {
		//取时间
		String[] times = getValues(dataPlan);
		int hour = Integer.parseInt(times[0]);		//小时数
		int minute = Integer.parseInt(times[1]);	//分钟数
		if (minute < 0 || minute > 59) {
			//"分钟数【{0}】设置错误！"
			throw new TaskException(JsMessage.getValue("dateplan.minuteerror", minute));
		}
		
		//初始化开始时间
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		startCal.set(Calendar.MINUTE, 0);
		
		//取当前时间
		Calendar curCal = Calendar.getInstance();
		
		//取两个时间差，单位毫秒
		long diff = curCal.getTime().getTime() - startCal.getTime().getTime();
		//计算间隔小时数
		int dh = (int) diff/1000/60/60;
		
		//间隔小时数条件满足，判断是否到分钟数
		if (dh >= hour) {
			//取当前时间的分钟数
			int dm = curCal.get(Calendar.MINUTE);
			//判断分钟数大于或等于设置值
			if (dm >= minute) return true;
		}
		
		return false;
	}
	
	private Date hourNextDate(Date startDate, String dataPlan) throws TaskException {
		//取时间
		String[] times = getValues(dataPlan);
		int hour = Integer.parseInt(times[0]);		//小时数
		int minute = Integer.parseInt(times[1]);	//分钟数
		if (minute < 0 || minute > 59) {
			throw new TaskException(JsMessage.getValue("dateplan.minuteerror", minute));
		}

		//初始化开始时间
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		startCal.set(Calendar.MINUTE, 0);
		
		//添加指定的小时数
		long dh = hour*1000*60*60;
		long dv = startCal.getTime().getTime() + dh;
		Date nextDate = startCal.getTime();
		nextDate.setTime(dv);
		startCal.setTime(nextDate);
		//指定分钟数
		startCal.set(Calendar.MINUTE, minute);
		
		return startCal.getTime();
	}
	
	/**
	 * 设置：每【】天，时间是【】
	 * 解释：判断是否间隔n天，到指定时间点
	 * @param startDate -- 开始时间
	 * @param dataPlan -- 时间计划，格式：day=nd,nt
	 * @return
	 */
	private boolean dayDiff(Date startDate, String dataPlan) throws TaskException {
		//取时间点
		String[] days = getValues(dataPlan);
		int day = Integer.parseInt(days[0]);		//间隔天数
		String[] times = days[1].split(":");		//时间是
		int hour = Integer.parseInt(times[0]);		//小时数
		int minute = Integer.parseInt(times[1]);	//分钟数
		if (hour < 0 || hour > 23) {
			//"小时数【{0}】设置错误！"
			throw new TaskException(JsMessage.getValue("dateplan.hourerror", hour));
		}
		if (minute < 0 || minute > 59) {
			throw new TaskException(JsMessage.getValue("dateplan.minuteerror", minute));
		}
		
		//初始化开始时间到零点
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		startCal.set(Calendar.HOUR_OF_DAY, 0);
		startCal.set(Calendar.MINUTE, 0);
		
		//取当前时间
		Calendar curCal = Calendar.getInstance();
		
		//取两个时间差，单位毫秒
		long diff = curCal.getTime().getTime() - startCal.getTime().getTime();
		//计算间隔天数
		int dd = (int) diff/1000/60/60/24;
		
		//间隔小时数条件满足，判断是否到分钟数
		if (dd >= day) {
			//取当前时间的小时数
			int dh = curCal.get(Calendar.HOUR_OF_DAY);
			if (dh >= hour) {
				if (dh > hour) return true;
				//取当前时间的分钟数
				int dm = curCal.get(Calendar.MINUTE);
				//判断分钟数大于或等于设置值
				if (dm >= minute) return true;
			}
		}
		
		return false;
	}
	
	private Date dayNextDate(Date startDate, String dataPlan) throws TaskException {
		//取时间点
		String[] days = getValues(dataPlan);
		int day = Integer.parseInt(days[0]);		//间隔天数
		String[] times = days[1].split(":");		//时间是
		int hour = Integer.parseInt(times[0]);		//小时数
		int minute = Integer.parseInt(times[1]);	//分钟数
		if (hour < 0 || hour > 23) {
			throw new TaskException(JsMessage.getValue("dateplan.hourerror", hour));
		}
		if (minute < 0 || minute > 59) {
			throw new TaskException(JsMessage.getValue("dateplan.minuteerror", minute));
		}
		
		//初始化开始时间到零点
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		startCal.set(Calendar.HOUR_OF_DAY, 0);
		startCal.set(Calendar.MINUTE, 0);
		
		//添加指定的天数
		long dh = day*1000*60*60*24;
		long dv = startCal.getTime().getTime() + dh;
		Date nextDate = startCal.getTime();
		nextDate.setTime(dv);
		startCal.setTime(nextDate);
		//指定时间值
		startCal.set(Calendar.HOUR_OF_DAY, hour);
		startCal.set(Calendar.MINUTE, minute);
		
		return startCal.getTime();
	}
	
	/**
	 * 设置：每【】周，星期几【】，时间是【】
	 * 解释：判断是否间隔n周，到指定时间点
	 * @param startDate -- 开始时间
	 * @param dataPlan -- 时间计划，格式：week=nw,nd,nt
	 * @return
	 */
	private boolean weekDiff(Date startDate, String dataPlan) throws TaskException {
		//取时间点
		String[] weeks = getValues(dataPlan);
		int week = Integer.parseInt(weeks[0]);		//间隔周数
		int day = Integer.parseInt(weeks[1]);		//星期几
		String[] times = weeks[2].split(":");		//时间是
		int hour = Integer.parseInt(times[0]);		//小时数
		int minute = Integer.parseInt(times[1]);	//分钟数
		if (hour < 0 || hour > 23) {
			throw new TaskException(JsMessage.getValue("dateplan.hourerror", hour));
		}
		if (minute < 0 || minute > 59) {
			throw new TaskException(JsMessage.getValue("dateplan.minuteerror", minute));
		}
		
		//初始化开始时间到本周日零点
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		startCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		startCal.set(Calendar.HOUR_OF_DAY, 0);
		startCal.set(Calendar.MINUTE, 0);
		
		//取当前时间
		Calendar curCal = Calendar.getInstance();
		
		//取两个时间差，单位毫秒
		long diff = curCal.getTime().getTime() - startCal.getTime().getTime();
		//计算间隔周数
		int dw = (int) diff/1000/60/60/24/7;
		//当前是星期几
		int dd = curCal.get(Calendar.DAY_OF_WEEK);
		
		//间隔周数与星期几是否满足条件
		if (dw >= week && day == dd) {
			//取当前时间的小时数
			int dh = curCal.get(Calendar.HOUR_OF_DAY);
			if (dh >= hour) {
				if (dh > hour) return true;
				//取当前时间的分钟数
				int dm = curCal.get(Calendar.MINUTE);
				//判断分钟数大于或等于设置值
				if (dm >= minute) return true;
			}
		}
		
		return false;
	}
	
	private Date weekNextDate(Date startDate, String dataPlan) throws TaskException {
		//取时间点
		String[] weeks = getValues(dataPlan);
		int week = Integer.parseInt(weeks[0]);		//间隔周数
		int day = Integer.parseInt(weeks[1]);		//星期几
		String[] times = weeks[2].split(":");		//时间是
		int hour = Integer.parseInt(times[0]);		//小时数
		int minute = Integer.parseInt(times[1]);	//分钟数
		if (hour < 0 || hour > 23) {
			throw new TaskException(JsMessage.getValue("dateplan.hourerror", hour));
		}
		if (minute < 0 || minute > 59) {
			throw new TaskException(JsMessage.getValue("dateplan.minuteerror", minute));
		}
		
		//初始化开始时间到本周日零点
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		startCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		startCal.set(Calendar.HOUR_OF_DAY, 0);
		startCal.set(Calendar.MINUTE, 0);
		
		//添加指定的周数
		long dh = week*1000*60*60*24*7;
		long dv = startCal.getTime().getTime() + dh;
		Date nextDate = startCal.getTime();
		nextDate.setTime(dv);
		startCal.setTime(nextDate);
		//指定星期几
		startCal.set(Calendar.DAY_OF_WEEK, day);
		//指定时间值
		startCal.set(Calendar.HOUR_OF_DAY, hour);
		startCal.set(Calendar.MINUTE, minute);
		
		return startCal.getTime();
	}
	
	/**
	 * 设置：每【】个月，日期是【】，时间是【】
	 * 解释：判断是否间隔n月，到指定时间点
	 * @param startDate -- 开始时间
	 * @param dataPlan -- 时间计划，格式：month=nm,nd,nt
	 * @return
	 */
	private boolean monthDiff(Date startDate, String dataPlan) throws TaskException {
		//取时间点
		String[] months = getValues(dataPlan);
		int month = Integer.parseInt(months[0]);	//间隔月数
		int day = Integer.parseInt(months[1]);		//日期
		if (day < 0 || day > 32) {
			//"日期值【{0}】设置错误！"
			throw new TaskException(JsMessage.getValue("dateplan.dateerror", day));
		}
		String[] times = months[2].split(":");		//时间是
		int hour = Integer.parseInt(times[0]);		//小时数
		int minute = Integer.parseInt(times[1]);	//分钟数
		if (hour < 0 || hour > 23) {
			throw new TaskException(JsMessage.getValue("dateplan.hourerror", hour));
		}
		if (minute < 0 || minute > 59) {
			throw new TaskException(JsMessage.getValue("dateplan.minuteerror", minute));
		}
		
		//初始化开始时间
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		
		//取当前时间
		Calendar curCal = Calendar.getInstance();
		
		//计算间隔月数
		int dm = (curCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR))*12 + 
				 (curCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH));
		//当前日期值
		int dd = curCal.get(Calendar.DAY_OF_MONTH);
		
		//间隔月数与日期值是否满足条件
		if (dm >= month && day == dd) {
			//取当前时间的小时数
			int dh = curCal.get(Calendar.HOUR_OF_DAY);
			if (dh >= hour) {
				if (dh > hour) return true;
				//取当前时间的分钟数
				int dmin = curCal.get(Calendar.MINUTE);
				//判断分钟数大于或等于设置值
				if (dmin >= minute) return true;
			}
		}
		
		return false;
	}
	
	private Date monthNextDate(Date startDate, String dataPlan) throws TaskException {
		//取时间点
		String[] months = getValues(dataPlan);
		int month = Integer.parseInt(months[0]);	//间隔月数
		int day = Integer.parseInt(months[1]);		//日期
		if (day < 0 || day > 32) {
			throw new TaskException(JsMessage.getValue("dateplan.dateerror", day));
		}
		String[] times = months[2].split(":");		//时间是
		int hour = Integer.parseInt(times[0]);		//小时数
		int minute = Integer.parseInt(times[1]);	//分钟数
		if (hour < 0 || hour > 23) {
			throw new TaskException(JsMessage.getValue("dateplan.hourerror", hour));
		}
		if (minute < 0 || minute > 59) {
			throw new TaskException(JsMessage.getValue("dateplan.minuteerror", minute));
		}
		
		//初始化开始时间
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		
		//添加指定的月份数
		int dh = startCal.get(Calendar.MONTH) + month;
		startCal.set(Calendar.DAY_OF_MONTH, 1);//先初始化到月初否则会出错
		startCal.set(Calendar.MONTH, dh);
		//指定日期值
		startCal.set(Calendar.DAY_OF_MONTH, day);
		//指定时间值
		startCal.set(Calendar.HOUR_OF_DAY, hour);
		startCal.set(Calendar.MINUTE, minute);
		
		return startCal.getTime();
	}
	
	/**
	 * 设置：每【】个月，哪一周【】，星期几【】，时间是【】
	 * 解释：判断是否间隔n月，到指定时间点
	 * @param startDate -- 开始时间
	 * @param dataPlan -- 时间计划，格式：monthw=nm,nw,nd,nt
	 * @return
	 */
	private boolean monthwDiff(Date startDate, String dataPlan) throws TaskException {
		//取时间点
		String[] months = getValues(dataPlan);
		int month = Integer.parseInt(months[0]);	//间隔月数
		int week = Integer.parseInt(months[1]);		//0--第一个星期 1--最后一个星期
		int dayweek = Integer.parseInt(months[2]);	//星期几
		String[] times = months[3].split(":");		//时间是
		int hour = Integer.parseInt(times[0]);		//小时数
		int minute = Integer.parseInt(times[1]);	//分钟数
		if (hour < 0 || hour > 23) {
			throw new TaskException(JsMessage.getValue("dateplan.hourerror", hour));
		}
		if (minute < 0 || minute > 59) {
			throw new TaskException(JsMessage.getValue("dateplan.minuteerror", minute));
		}
		
		//初始化开始时间
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		//取当前月份第一周或最后一周指定的星期几
		int day = (week == 0) ? getFirstWeekofMonth(startDate, dayweek) : getLastWeekofMonth(startDate, dayweek);
		
		//取当前时间
		Calendar curCal = Calendar.getInstance();
		
		//计算间隔月数
		int dm = (curCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR))*12 + 
				 (curCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH));
		//取日期值
		int dd = curCal.get(Calendar.DAY_OF_MONTH);
		
		//间隔月数与日期值是否满足条件
		if (dm >= month && day == dd) {
			//取当前时间的小时数
			int dh = curCal.get(Calendar.HOUR_OF_DAY);
			if (dh >= hour) {
				if (dh > hour) return true;
				//取当前时间的分钟数
				int dmin = curCal.get(Calendar.MINUTE);
				//判断分钟数大于或等于设置值
				if (dmin >= minute) return true;
			}
		}
		
		return false;
	}
	
	private Date monthwNextDate(Date startDate, String dataPlan) throws TaskException {
		//取时间点
		String[] months = getValues(dataPlan);
		int month = Integer.parseInt(months[0]);	//间隔月数
		int week = Integer.parseInt(months[1]);		//0--第一个星期 1--最后一个星期
		int dayweek = Integer.parseInt(months[2]);	//星期几
		String[] times = months[3].split(":");		//时间是
		int hour = Integer.parseInt(times[0]);		//小时数
		int minute = Integer.parseInt(times[1]);	//分钟数
		if (hour < 0 || hour > 23) {
			throw new TaskException(JsMessage.getValue("dateplan.hourerror", hour));
		}
		if (minute < 0 || minute > 59) {
			throw new TaskException(JsMessage.getValue("dateplan.minuteerror", minute));
		}
		
		//初始化开始时间
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		
		//添加指定的月份数
		int dh = startCal.get(Calendar.MONTH) + month;
		startCal.set(Calendar.DAY_OF_MONTH, 1);//先初始化到月初否则会出错，如当前日为30，当设置为2月时，日期会跳到3月份
		startCal.set(Calendar.MONTH, dh);
		
		//取当前月份第一周或最后一周指定的星期几
		int day = (week == 0) ? getFirstWeekofMonth(startCal.getTime(), dayweek) : 
								getLastWeekofMonth(startCal.getTime(), dayweek);
		
		//指定日期值
		startCal.set(Calendar.DAY_OF_MONTH, day);
		//指定时间值
		startCal.set(Calendar.HOUR_OF_DAY, hour);
		startCal.set(Calendar.MINUTE, minute);
		
		return startCal.getTime();
	}
	
	/**
	 * 设置：每【】年，月份是【】，日期是【】，时间是【】
	 * 解释：判断是否间隔n年，到指定时间点
	 * @param startDate -- 开始时间
	 * @param dataPlan -- 时间计划，格式：year=ny,nm,nd,nt
	 * @return
	 */
	private boolean yearDiff(Date startDate, String dataPlan) throws TaskException {
		//取时间点
		String[] years = getValues(dataPlan);
		int year = Integer.parseInt(years[0]);		//间隔年数
		int month = Integer.parseInt(years[1]);		//月份值
		if (month < 1 || month > 12) {
			throw new TaskException(JsMessage.getValue("dateplan.montherror", month));
		}
		int day = Integer.parseInt(years[2]);		//日期
		if (day < 0 || day > 32) {
			throw new TaskException(JsMessage.getValue("dateplan.dateerror", day));
		}
		String[] times = years[3].split(":");		//时间是
		int hour = Integer.parseInt(times[0]);		//小时数
		int minute = Integer.parseInt(times[1]);	//分钟数
		if (hour < 0 || hour > 23) {
			throw new TaskException(JsMessage.getValue("dateplan.hourerror", hour));
		}
		if (minute < 0 || minute > 59) {
			throw new TaskException(JsMessage.getValue("dateplan.minuteerror", minute));
		}
		
		//初始化开始时间
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		
		//取当前时间
		Calendar curCal = Calendar.getInstance();
		
		//计算间隔年数
		int dy = curCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR);
		//当前月份值
		int dm = curCal.get(Calendar.MONTH) + 1;
		//当前日期值
		int dd = curCal.get(Calendar.DAY_OF_MONTH);
		
		//间隔年数与月份值、日期值是否满足条件
		if (dy >= year && dm == month && day == dd) {
			//取当前时间的小时数
			int dh = curCal.get(Calendar.HOUR_OF_DAY);
			if (dh >= hour) {
				if (dh > hour) return true;
				//取当前时间的分钟数
				int dmin = curCal.get(Calendar.MINUTE);
				//判断分钟数大于或等于设置值
				if (dmin >= minute) return true;
			}
		}
		
		return false;
	}
	
	private Date yearNextDate(Date startDate, String dataPlan) throws TaskException {
		//取时间点
		String[] years = getValues(dataPlan);
		int year = Integer.parseInt(years[0]);		//间隔年数
		int month = Integer.parseInt(years[1]);		//月份值
		if (month < 1 || month > 12) {
			throw new TaskException(JsMessage.getValue("dateplan.montherror", month));
		}
		int day = Integer.parseInt(years[2]);		//日期
		if (day < 0 || day > 32) {
			throw new TaskException(JsMessage.getValue("dateplan.dateerror", day));
		}
		String[] times = years[3].split(":");		//时间是
		int hour = Integer.parseInt(times[0]);		//小时数
		int minute = Integer.parseInt(times[1]);	//分钟数
		if (hour < 0 || hour > 23) {
			throw new TaskException(JsMessage.getValue("dateplan.hourerror", hour));
		}
		if (minute < 0 || minute > 59) {
			throw new TaskException(JsMessage.getValue("dateplan.minuteerror", minute));
		}
		
		//初始化开始时间
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		
		//添加指定的年份数
		int dh = startCal.get(Calendar.YEAR) + year;
		startCal.set(Calendar.YEAR, dh);
		//指定月份值
		startCal.set(Calendar.DAY_OF_MONTH, 1);//先初始化到月初否则会出错
		startCal.set(Calendar.MONTH, month-1);
		//指定日期值
		startCal.set(Calendar.DAY_OF_MONTH, day);
		//指定时间值
		startCal.set(Calendar.HOUR_OF_DAY, hour);
		startCal.set(Calendar.MINUTE, minute);
		
		return startCal.getTime();
	}
	
	/**
	 * 设置：每【】年，哪一周【】，星期几【】，时间是【】
	 * 解释：判断是否间隔n年，到指定时间点
	 * @param startDate -- 开始时间
	 * @param dataPlan -- 时间计划，格式：yearw=ny,nw,nd,nt
	 * @return
	 */
	private boolean yearwDiff(Date startDate, String dataPlan) throws TaskException {
		//取时间点
		String[] years = getValues(dataPlan);
		int year = Integer.parseInt(years[0]);		//间隔年数
		int week = Integer.parseInt(years[1]);		//0--第一个星期 1--最后一个星期
		int dayweek = Integer.parseInt(years[2]);	//星期几
		String[] times = years[3].split(":");		//时间是
		int hour = Integer.parseInt(times[0]);		//小时数
		int minute = Integer.parseInt(times[1]);	//分钟数
		if (hour < 0 || hour > 23) {
			throw new TaskException(JsMessage.getValue("dateplan.hourerror", hour));
		}
		if (minute < 0 || minute > 59) {
			throw new TaskException(JsMessage.getValue("dateplan.minuteerror", minute));
		}
		
		//初始化开始时间
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		//取当前年份第一周或最后一周指定的星期几
		int day = (week == 0) ? getFirstWeekofYear(startDate, dayweek) : getLastWeekofYear(startDate, dayweek);
		//取目标月份值
		int month = (week == 0) ? 1 : 12;
		
		//取当前时间
		Calendar curCal = Calendar.getInstance();
		
		//计算间隔年数
		int dy = curCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR);
		//取日期值
		int dd = curCal.get(Calendar.DAY_OF_MONTH);
		//取月份值
		int dm = curCal.get(Calendar.MONTH) + 1;
		
		//间隔年数与日期值是否满足条件
		if (dy >= year && dm == month && dd == day) {
			//取当前时间的小时数
			int dh = curCal.get(Calendar.HOUR_OF_DAY);
			if (dh >= hour) {
				if (dh > hour) return true;
				//取当前时间的分钟数
				int dmin = curCal.get(Calendar.MINUTE);
				//判断分钟数大于或等于设置值
				if (dmin >= minute) return true;
			}
		}
		
		return false;
	}
	
	private Date yearwNextDate(Date startDate, String dataPlan) throws TaskException {
		//取时间点
		String[] years = getValues(dataPlan);
		int year = Integer.parseInt(years[0]);		//间隔年数
		int week = Integer.parseInt(years[1]);		//0--第一个星期 1--最后一个星期
		int dayweek = Integer.parseInt(years[2]);	//星期几
		String[] times = years[3].split(":");		//时间是
		int hour = Integer.parseInt(times[0]);		//小时数
		int minute = Integer.parseInt(times[1]);	//分钟数
		if (hour < 0 || hour > 23) {
			throw new TaskException(JsMessage.getValue("dateplan.hourerror", hour));
		}
		if (minute < 0 || minute > 59) {
			throw new TaskException(JsMessage.getValue("dateplan.minuteerror", minute));
		}
		
		//初始化开始时间
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		
		//取目标月份值
		int month = (week == 0) ? 1 : 12;
		
		//添加指定的年份数
		int dh = startCal.get(Calendar.YEAR) + year;
		startCal.set(Calendar.YEAR, dh);
		//指定月份值
		startCal.set(Calendar.DAY_OF_MONTH, 1);//先初始化到月初否则会出错
		startCal.set(Calendar.MONTH, month-1);
		//取当前年份第一周或最后一周指定的星期几
		int day = (week == 0) ? getFirstWeekofYear(startCal.getTime(), dayweek) : 
								getLastWeekofYear(startCal.getTime(), dayweek);
		
		//指定日期值
		startCal.set(Calendar.DAY_OF_MONTH, day);
		//指定时间值
		startCal.set(Calendar.HOUR_OF_DAY, hour);
		startCal.set(Calendar.MINUTE, minute);
		
		return startCal.getTime();
	}
	
	/**
	 * 取当月第一周指定日期
	 * @param curDate -- 指定日期
	 * @param day -- 星期几
	 * @return
	 */
	private int getFirstWeekofMonth(Date curDate, int day) {
		//取当前时间
		Calendar curCal = Calendar.getInstance();
		curCal.setTime(curDate);
		//取当前月份值
		int curMonth = curCal.get(Calendar.MONTH);
		
		//设置为第1周
		curCal.set(Calendar.WEEK_OF_MONTH, 1);
		//设置为星期几
		curCal.set(Calendar.DAY_OF_WEEK, day);
		
		//如果第1周没有指定的星期数，则取下一周的日期
		int month = curCal.get(Calendar.MONTH);
		if (curMonth != month) {
			int d = curCal.get(Calendar.DAY_OF_MONTH);
			curCal.set(Calendar.DAY_OF_MONTH, d+7);
		}
		
		return curCal.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * 取当月最后一周指定日期
	 * @param curDate -- 指定日期
	 * @param day -- 星期几
	 * @return
	 */
	private int getLastWeekofMonth(Date curDate, int day) {
		//取当前时间
		Calendar curCal = Calendar.getInstance();
		curCal.setTime(curDate);
		//取当前月份值
		int curMonth = curCal.get(Calendar.MONTH);
		
		//设置为最后一周
		curCal.set(Calendar.WEEK_OF_MONTH, 5);
		//设置为星期几
		curCal.set(Calendar.DAY_OF_WEEK, day);
		
		//如果最后一周没有指定的星期数，则取上一周的日期
		int month = curCal.get(Calendar.MONTH);
		if (curMonth != month) {
			int d = curCal.get(Calendar.DAY_OF_MONTH);
			curCal.set(Calendar.DAY_OF_MONTH, d-7);
		}
		
		return curCal.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * 取当年第一周指定日期
	 * @param curDate -- 指定日期
	 * @param day -- 星期几
	 * @return
	 */
	private int getFirstWeekofYear(Date curDate, int day) {
		//取当前时间
		Calendar curCal = Calendar.getInstance();
		curCal.setTime(curDate);
		//设置为1月份
		curCal.set(Calendar.MONTH, 0);
		
		return getFirstWeekofMonth(curCal.getTime(), day);
	}
	
	/**
	 * 取当年最后一周指定日期
	 * @param curDate -- 指定日期
	 * @param day -- 星期几
	 * @return
	 */
	private int getLastWeekofYear(Date curDate, int day) {
		//取当前时间
		Calendar curCal = Calendar.getInstance();
		curCal.setTime(curDate);
		//设置为12月份
		curCal.set(Calendar.MONTH, 11);
		
		return getLastWeekofMonth(curCal.getTime(), day);
	}
	
	/**
	 * 从执行计划中取设置值
	 * @param dataPlan -- 计划串
	 * @return
	 */
	private String[] getValues(String dataPlan) throws TaskException {
		String[] values;
		try {
			String planValue = dataPlan.split("=")[1];
			
			values = planValue.trim().split(",");
			for (int i = 0, n = values.length; i < n; i++) {
				values[i] = getValue(values[i].trim());
			}
		} catch (Exception e) {
			//"任务执行计划字符串格式错误！"
			throw new TaskException(JsMessage.getValue("dateplan.planstrerror"));
		}
		
		return values;
	}
	
	/**
	 * 去掉值最后一个字符，最后一个是“单位”标志
	 * @param value -- 计划值，带单位
	 * @return
	 */
	private String getValue(String value) {
		return value.substring(0, value.length()-1);
	}
}
