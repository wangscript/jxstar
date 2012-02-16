/*
 * SqlFilter.java 2011-3-1
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.util;

/**
 * SQL语句过滤处理，处理SQL安全问题。
 *
 * @author TonyTan
 * @version 1.0, 2011-3-1
 */
public class SqlFilter {

	/**
	 * 过滤掉查询WHERESQL中的特殊关键字：
	 * system.
	 * sys.
	 * drop
	 * delete
	 * update
	 * create
	 * 
	 * @param whereSql -- 指查询WHERESQL
	 * @return
	 */
	public static String filter(String whereSql) {
		if (whereSql == null || whereSql.length() == 0) return whereSql;
		
		//保存原wheresql
		String oldWhere = whereSql;
		boolean isError = false;
		
		whereSql = whereSql.toLowerCase();
		if (whereSql.indexOf("system.") >= 0) {
			isError = true;
			whereSql = whereSql.replaceAll("system\\.", "!!system. illegal!!");
		}
		if (whereSql.indexOf("sys.") >= 0) {
			isError = true;
			whereSql = whereSql.replaceAll("sys\\.", "!!sys. illegal!!");
		}
		if (whereSql.indexOf("drop ") >= 0) {
			isError = true;
			whereSql = whereSql.replaceAll("drop ", "!!drop illegal!!");
		}
		if (whereSql.indexOf("delete ") >= 0) {
			whereSql = whereSql.replaceAll("delete ", "!!delete illegal!!");
		}
		if (whereSql.indexOf("update ") >= 0) {
			isError = true;
			whereSql = whereSql.replaceAll("update ", "!!update illegal!!");
		}
		if (whereSql.indexOf("create ") >= 0) {
			isError = true;
			whereSql = whereSql.replaceAll("create ", "!!create illegal!!");
		}
		
		//如果有非法关键字，则返回非法SQL，否则返回原SQL
		return isError ? whereSql : oldWhere;
	}
}
