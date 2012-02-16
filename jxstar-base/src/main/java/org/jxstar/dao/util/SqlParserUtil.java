/*
 * SqlParserUtil.java 2011-1-11
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao.util;

import java.util.Map;

import org.jxstar.util.MapUtil;

/**
 * SQL语句解析工具类。
 *
 * @author TonyTan
 * @version 1.0, 2011-1-11
 */
public class SqlParserUtil {

	/**
	 * 解析SQL语句中的常量值，主要是用户会话信息常量
	 * @param sql -- 待解析的SQL语句
	 * @param userInfo -- 用户信息
	 * @return
	 */
	public static String parseSQLConstant(String sql, Map<String,String> userInfo) {
		if (sql.indexOf("{CURUSERID}") > -1) {
			sql = sql.replaceAll("{CURUSERID}", MapUtil.getValue(userInfo, "user_id"));
		}
		
		if (sql.indexOf("{CURUSERCODE}") > -1) {
			sql = sql.replaceAll("{CURUSERCODE}", MapUtil.getValue(userInfo, "user_code"));
		}
		
		if (sql.indexOf("{CURUSERNAME}") > -1) {
			sql = sql.replaceAll("{CURUSERNAME}", MapUtil.getValue(userInfo, "user_name"));
		}
		
		if (sql.indexOf("{CURDEPTID}") > -1) {
			sql = sql.replaceAll("{CURDEPTID}", MapUtil.getValue(userInfo, "dept_id"));
		}
		
		if (sql.indexOf("{CURDEPTCODE}") > -1) {
			sql = sql.replaceAll("{CURDEPTCODE}", MapUtil.getValue(userInfo, "dept_code"));
		}
		
		if (sql.indexOf("{CURDEPTNAME}") > -1) {
			sql = sql.replaceAll("{CURDEPTNAME}", MapUtil.getValue(userInfo, "dept_name"));
		}
		
		return sql;
	}
}
