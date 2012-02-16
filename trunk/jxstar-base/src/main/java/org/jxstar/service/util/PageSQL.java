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
			return getOracleSQL(sql, start+1, limit);
		} else if (dbtype.equals("sqlserver")) {
			return getServerSQL(sql, start+1, limit);
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
	 * select * from (select rownum r, t1.* from (原SQL) t1 where rownum < m + n) t2
     * where t2.r >= m
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
			sb.append(") t1 where rownum < "+ (m + n) +") t2 ");
			sb.append("where t2.r >= " + m);
		
		return sb.toString();
	}
	
	/**
	 * SQL数据库：从数据库表中第M条记录开始检索N条记录：
	 * select top n * from (select top (m + n - 1) * from (原SQL)) t1
	 * 
	 * @param sql
	 * @param m
	 * @param n
	 * @return
	 */
	private static String getServerSQL(String sql, int m, int n) {
		StringBuilder sb = new StringBuilder("select top "+ n +" * from ");
			sb.append("(select top ("+ (m + n) +" - 1) * from ( ");
			sb.append(sql);
			sb.append(")) t1");
		
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
     *  as t1) as t2 where t2.rownum between m-1 and m+n-1
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
