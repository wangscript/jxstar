/*
 * DdlTableTest.java 2010-12-17
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm;


import java.util.List;
import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dm.ddl.MysqlDdlTable;
import org.jxstar.dm.ddl.OracleDdlTable;
import org.jxstar.dm.ddl.SqlServerDdlTable;
import org.jxstar.test.AbstractTest;
import org.jxstar.util.ArrayUtil;
import org.jxstar.util.factory.FactoryUtil;

/**
 * ORACLE数据库对象创建类测试
 *
 * @author TonyTan
 * @version 1.0, 2010-12-17
 */
public class DdlTableTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		sqlTest();
	}

	public static void oracleTest() {
		OracleDdlTable oracle = new OracleDdlTable();
		try {
			//oracle.create("jxstar2");
			oracle.modify("jxstar3");
		} catch (DmException e) {
			e.printStackTrace();
		}
	}
	
	public static void sqlTest() {
		SqlServerDdlTable ddl = new SqlServerDdlTable();
		try {
			ddl.create("jxstar777433");
		} catch (DmException e) {
			e.printStackTrace();
		}
	}
	
	public static void mysqlTest() {
		MysqlDdlTable mysql = new MysqlDdlTable();
		try {
			mysql.create("test_table");
		} catch (DmException e) {
			e.printStackTrace();
		}
	}
	
	public static void allModifySql() {
		BaseDao dao = BaseDao.getInstance();
		String sql = "select table_id from dm_tablecfg where state = '2'";
		List<Map<String, String>> ls = dao.query(dao.createParam(sql));
		
		DdlTable ddlTable = DmFactory.getDdlTable("default");
		List<String> lssql = FactoryUtil.newList();
		
		for (Map<String, String> mp : ls) {
			String tableId = mp.get("table_id");
			try {
				lssql.addAll(ddlTable.getModifySql(tableId));
			} catch (DmException e) {
				e.printStackTrace();
			}
		}
		
		String sql1 = ArrayUtil.listToString(lssql, "");
		System.out.println("===========" + sql1);
	}
}
