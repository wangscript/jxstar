/*
 * WarnTaskTest.java 2011-3-18
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */

package org.jxstar.service.warn;

import java.util.Calendar;

import org.jxstar.task.TaskException;
import org.jxstar.test.AbstractTest;
import org.jxstar.util.DateUtil;

/**
 * 上报任务组件测试类。
 *
 * @author TonyTan
 * @version 1.0, 2011-3-18
 */
public class WarnTaskTest extends AbstractTest {

	public static void main(String[] args) {
		
		//WarnTaskTest.intervalDate();
		
		//WarnTaskTest.parseDataWhere();
		
		WarnTask task = new WarnTask();
		try {
			task.execute();
		} catch (TaskException e) {
			e.printStackTrace();
		}
	}

	public static void intervalDate() {
		Calendar cal1 = WarnUtil.intervalDate(Calendar.getInstance(), "10d");
		System.out.println("计算后的时间值=" + DateUtil.calendarToDateTime(cal1));
		
		Calendar cal2 = WarnUtil.intervalDate(Calendar.getInstance(), "10h");
		System.out.println("计算后的时间值=" + DateUtil.calendarToDateTime(cal2));
		
		Calendar cal3 = WarnUtil.intervalDate(Calendar.getInstance(), "10m");
		System.out.println("计算后的时间值=" + DateUtil.calendarToDateTime(cal3));
	}
	
	public static void parseDataWhere() {
		String where = "run_mal_record.dept_id like '1001%' and run_mal_record.add_userid = '1233'";
		where = WarnUtil.parseDataWhere(where);
		System.out.println("解析后台where=" + where);
	}
}
