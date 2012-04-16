/*
 * SqlParser.java 2008-4-8
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao.util;

/**
 * 解析SQL语句中的函数，解决SQL语句中函数的跨数据库的问题.
 * 解析说明：
 * 含自定义函数标识的SQL的样式为: 
 * select * from t1 where {TO_STR}(v1) >= '2007-09-04' and  
 * 						  {TO_STR}(v1) <= '2007-10-04'
 * 如果是ORACLE数据库,解析后的SQL为:
 * select * from t1 where to_char(v1, 'yyyy-mm-dd') >= '2007-09-04' 
 * and to_char(v1, 'yyyy-mm-dd') <= '2007-10-04'
 * 
 * 支持嵌套函数的解析，如：
 * {MONTHDIFF}({TO_DATE}({TO_STR}('2007-08-09')), v_date)
 * 如果是ORACLE数据库,解析后的SQL为:
 * months_between(to_date(to_char('2007-08-09', 'yyyy-mm-dd'), 'yyyy-mm-dd'),  v_date)
 * 
 * @author TonyTan
 * @version 1.0, 2008-4-8
 */
public interface SqlParser {
	/**
	 * 解析SQL语句中的函数.
	 * 
	 * @param sSQL - 被解析的SQL
	 * @return String - 返回解析后的SQL，如果出错了则不解析SQL
	 */
	public String parse(String sSQL) throws SQLParseException;
	
	/**
	 * 解析SQL语句中的函数.
	 * 
	 * @param sSQL - 被解析的SQL
	 * @param sExclusion - 不参与解析的字符串
	 * @return String - 返回解析后的SQL，如果出错了则不解析SQL
	 */
	public String parse(String sSQL, String sExclusion) 
					throws SQLParseException;
	
	/**
	 * 解析SQL语句中的函数.
	 * 
	 * @param sSQL - 被解析的SQL
	 * @param asExclusion - 不参与解析的字符串数组
	 * @return String - 返回解析后的SQL，如果出错了则不解析SQL
	 */
	public String parse(String sSQL, String[] asExclusion)
					throws SQLParseException;
}