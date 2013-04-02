/*
 * PageSQL.java 2008-5-18
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.util;

import org.jxstar.util.log.Log;


/**
 * 取加分页WHERE子句的SQL的工具类。
 * 
 * @author TonyTan
 * @version 1.0, 2008-5-18
 */
public class PageSQL {	
	private static Log _log = Log.getInstance();
	
	/**
	 * 取加分页WHERE子句的SQL。
	 * 
	 * @param sql - 原SQL
	 * @param dbtype - 数据库类型
	 * @param start 读取数据的开始位置，第一条数据为0
	 * @param limit 可以读取记录的条数
	 * @return String
	 */
	public static String getPageSQL(String sql, String dbtype, int start, int limit) {
		if (dbtype.equals("oracle")) {
			return getOracleSQL(sql, start, limit);
		} else if (dbtype.equals("sqlserver")) {
			return getServer2005SQL(sql, start, limit);
		} else if (dbtype.equals("mysql")) {
			return getMysqlSQL(sql, start, limit);
		} else if (dbtype.equals("db2")) {
			return getDB2SQL(sql, start, limit);
		}
		_log.showWarn("Does not support the [{0}] database type, not page sql！", dbtype);
		
		return "";
	}
	
	/**
	 * 取原记录集记录总数的SQL。
	 * 
	 * @param sql - 原SQL
	 * @return String
	 */
	public static String getCountSQL(String sql) {
		String newsql = "select count(*) as cnt from ("+ sql +") t";
		
		return newsql;
	}
	
	/**
	 * ORACLE数据库：从数据库表中第M条记录开始检索N条记录：
	 * select * from (select rownum r, t1.* from (原SQL) t1 where rownum < m + n + 1) t2
     * where t2.r > m
	 * 
	 * @param sql
	 * @param m
	 * @param n
	 * @return
	 */
	private static String getOracleSQL(String sql, int m, int n) {
		StringBuilder sb = new StringBuilder("select * from ");
			sb.append("(select t1.*, rownum r from ( ");
			sb.append(sql);
			sb.append(") t1 where rownum < "+ (m + n + 1) +") t2 ");
			sb.append("where t2.r > " + m);
		
		return sb.toString();
	}
	
	/**
	 * SQL数据库：从数据库表中第M条记录开始检索N条记录：
	 * select top n * from table_name where field_id not in 
	 * (select top m+n field_id from table_name where where_sql order by field_name)
	 * and where_sql order by field_name
	 * 
	 * @param sql
	 * @param m
	 * @param n
	 * @return
	 */
	/*private static String getServer2000SQL(String sql, int m, int n) {
		//替换头部的select
		sql = sql.trim();
		if (sql.length() > 7 && sql.substring(0, 7).equalsIgnoreCase("select ")) {
			sql = sql.replaceFirst("select ", "select top ("+ (m + n) +") ");
		}
		
		StringBuilder sb = new StringBuilder("select top "+ n +" * from (");
			sb.append(sql);
			sb.append(") t1");
		
		return sb.toString();
	}*/
	
	/**
	 * 默认sql中最后一段为排序字段
	 * select * from (
	 * select t.*, row_number()over(order by _tc_) as _rn_ from (
	 * {select} top m+n {原SQL字段}, 0 as _tc_ {from table_name order by field_name}) t
	 * ) tt where _rn_ > m and _rn_ < m+n+1
	 * 
	 * @param sql -- 原查询SQL
	 * @param m -- 开始位置，0为第一个位置
	 * @param n -- 取记录条数
	 * @return
	 */
	private static String getServer2005SQL(String sql, int m, int n) {
		if (sql.length() < 10) return sql;
		
		//都转为小写
		sql = sql.toLowerCase();
		if (sql.substring(0, 7).equals("select ") == false) return sql;
		
		//在from前插入{, 0 as _tc_}
		if (sql.indexOf(" from ") > 0) {
			sql = sql.replaceFirst(" from ", ", 0 as _tc_ from ");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select * from (");
		sb.append("select t.*, row_number()over(order by _tc_) as _rn_ from (");
		sb.append("select top " + (m+n) + " ");
		sb.append(sql.substring(7, sql.length()));
		sb.append(") t) tt where _rn_ > "+ m +" and _rn_ < "+ (m+n+1));
		
		return sb.toString();
	}
	
	/**
	 * MYSQL数据库：从数据库表中第M条记录开始检索N条记录：
	 * select * from (原SQL) limit m,n
	 * 
	 * @param sql
	 * @param m
	 * @param n
	 * @return
	 */
	private static String getMysqlSQL(String sql, int m, int n) {
		m = m < 0 ? 0 : m;
		n = n < 0 ? 50 : n;
		
		StringBuilder sb = new StringBuilder("select * from (");
			sb.append(sql);
			sb.append(") t1 limit " + m + ", " + n);
		
		return sb.toString();
	}
	
	/**
	 * DB2数据库：从数据库表中第M条记录开始检索N条记录，0为第一条：
	 * select * from (select t1.*, rownumber() over() as rownum from (原SQL)
     *  as t1) as t2 where t2.rownum between m and m+n
	 * 
	 * @param sql
	 * @param m
	 * @param n
	 * @return
	 */
	private static String getDB2SQL(String sql, int m, int n) {
		StringBuilder sb = new StringBuilder("select * from ");
			sb.append("(select t1.*, rownumber() over() as rownum from (");
			sb.append(sql);
			sb.append(") as t1) as t2 where t2.rownum between "+ (m+1) +" and " + (m+n));
			
		return sb.toString();
	}
}
