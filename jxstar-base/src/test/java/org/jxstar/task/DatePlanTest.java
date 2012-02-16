/*
 * DatePlanTest.java 2011-1-14
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.task;


import org.jxstar.service.studio.TaskPlanBO;
import org.jxstar.test.AbstractTest;

/**
 * 日期执行计划测试类
 *
 * @author TonyTan
 * @version 1.0, 2011-1-14
 */
public class DatePlanTest extends AbstractTest {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*DatePlanTest.testGetValue();
		
		DatePlanTest.getFirstWeekofMonth();
		
		DatePlanTest.getLastWeekofMonth();
		
		DatePlanTest.getFirstWeekofYear();
		
		DatePlanTest.getLastWeekofYear();
		
		DatePlanTest.yearwDiff();*/
		TaskPlanBO plan = new TaskPlanBO();
		plan.viewPlan("2011-1-14 18:28:30", "yearw=1y,1w,2d,1:10t");
	}
	/*
	public static void yearwDiff() {
		String datePlan = "year=1y,1w,6d,10:34t";
		Calendar startCal = Calendar.getInstance();
		startCal.set(Calendar.YEAR, 2010);
		Date startDate = startCal.getTime(); 
		p("startDate:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate));
		
		DatePlan plan = new DatePlan(startDate, datePlan);
		
		try {
			boolean bret = plan.yearwDiff(startDate, datePlan);
			p("yearwDiff="+bret);
		} catch (TaskException e) {
			e.printStackTrace();
		}
	}
	
	public static void yearDiff() {
		String datePlan = "year=1y,2m,28d,10:34t";
		Calendar startCal = Calendar.getInstance();
		startCal.set(Calendar.YEAR, 2010);
		startCal.set(Calendar.MONTH, 0);
		Date startDate = startCal.getTime(); 
		p("startDate:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate));
		
		DatePlan plan = new DatePlan(startDate, datePlan);
		
		try {
			boolean bret = plan.yearDiff(startDate, datePlan);
			p("yearDiff="+bret);
		} catch (TaskException e) {
			e.printStackTrace();
		}
	}
	
	public static void monthwDiff() {
		String datePlan = "month=1m,1w,6d,10:21t";
		Calendar startCal = Calendar.getInstance();
		startCal.set(Calendar.YEAR, 2010);
		startCal.set(Calendar.MONTH, 11);
		Date startDate = startCal.getTime(); 
		p("startDate:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate));
		
		DatePlan plan = new DatePlan(startDate, datePlan);
		
		try {
			boolean bret = plan.monthwDiff(startDate, datePlan);
			p("monthwDiff="+bret);
		} catch (TaskException e) {
			e.printStackTrace();
		}
	}
	
	public static void monthDiff() {
		String datePlan = "month=1m,14d,10:21t";
		Calendar startCal = Calendar.getInstance();
		startCal.set(Calendar.YEAR, 2010);
		startCal.set(Calendar.MONTH, 10);
		Date startDate = startCal.getTime(); 
		p("startDate:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate));
		
		DatePlan plan = new DatePlan(startDate, datePlan);
		
		try {
			boolean bret = plan.monthDiff(startDate, datePlan);
			p("monthDiff="+bret);
		} catch (TaskException e) {
			e.printStackTrace();
		}
	}
	
	public static void weekDiff() {
		String datePlan = "week=1w,6d,10:19t";
		Calendar startCal = Calendar.getInstance();
		startCal.set(Calendar.DAY_OF_MONTH, 9);
		Date startDate = startCal.getTime(); 
		p("startDate:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate));
		
		DatePlan plan = new DatePlan(startDate, datePlan);
		
		try {
			boolean bret = plan.weekDiff(startDate, datePlan);
			p("weekDiff="+bret);
		} catch (TaskException e) {
			e.printStackTrace();
		}
	}
	
	public static void dayDiff() {
		String datePlan = "day=2d,10:16t";
		Calendar startCal = Calendar.getInstance();
		startCal.set(Calendar.DAY_OF_MONTH, 13);
		Date startDate = startCal.getTime(); 
		p("startDate:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate));
		
		DatePlan plan = new DatePlan(startDate, datePlan);
		
		try {
			boolean bret = plan.dayDiff(startDate, datePlan);
			p("dayDiff="+bret);
		} catch (TaskException e) {
			e.printStackTrace();
		}
	}
	
	public static void hourDiff() {
		String datePlan = "hour=1h,4m";
		Calendar startCal = Calendar.getInstance();
		startCal.set(Calendar.HOUR_OF_DAY, 8);
		startCal.set(Calendar.MINUTE, 53);
		Date startDate = startCal.getTime(); 
		p("startDate:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate));
		
		DatePlan plan = new DatePlan(startDate, datePlan);
		
		try {
			boolean bret = plan.hourDiff(startDate, datePlan);
			p("hourDiff="+bret);
		} catch (TaskException e) {
			e.printStackTrace();
		}
	}
	
	public static void minuteDiff() {
		String datePlan = "min=10m";
		Calendar startCal = Calendar.getInstance();
		startCal.set(Calendar.HOUR_OF_DAY, 9);
		startCal.set(Calendar.MINUTE, 53);
		Date startDate = startCal.getTime(); 
		p("startDate:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate));
		
		DatePlan plan = new DatePlan(startDate, datePlan);
		
		try {
			boolean bret = plan.minuteDiff(startDate, datePlan);
			p("minuteDiff="+bret);
		} catch (TaskException e) {
			e.printStackTrace();
		}
	}
	
	public static void getFirstWeekofMonth() {
		p("getFirstWeekofMonth:");
		DatePlan plan = new DatePlan(null, null);
		
		p("day="+plan.getFirstWeekofMonth(2));
	}
	
	public static void getLastWeekofMonth() {
		p("getLastWeekofMonth:");
		DatePlan plan = new DatePlan(null, null);
		
		p("day="+plan.getLastWeekofMonth(2));
	}
	
	public static void getFirstWeekofYear() {
		p("getFirstWeekofYear:");
		DatePlan plan = new DatePlan(null, null);
		
		p("day="+plan.getFirstWeekofYear(2));
	}
	
	public static void getLastWeekofYear() {
		p("getLastWeekofYear:");
		DatePlan plan = new DatePlan(null, null);
		
		p("day="+plan.getLastWeekofYear(2));
	}

	public static void testGetValue() {
		p("testGetValue:");
		DatePlan plan = new DatePlan(null, null);
		
		String value = "23d";
		p(plan.getValue(value));
	}*/
	
	public static void p(String s) {
		System.out.println(s);
	}
}
