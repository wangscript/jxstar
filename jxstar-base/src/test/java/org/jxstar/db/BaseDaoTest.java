/*
 * BaseDaoTest.java 2010-12-17
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.db;

import java.util.Map;


import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.transaction.TransactionException;
import org.jxstar.dao.transaction.TransactionManager;
import org.jxstar.test.AbstractTest;
import org.jxstar.util.factory.SystemFactory;

/**
 * 
 *
 * @author TonyTan
 * @version 1.0, 2010-12-17
 */
public class BaseDaoTest extends AbstractTest {
	private static BaseDao _dao = BaseDao.getInstance();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TransactionManager _tranMng = (TransactionManager) SystemFactory.createSystemObject("TransactionManager");
		//开始一个事务
		_tranMng.startTran();
		try {
			DaoParam param = _dao.createParam("update dm_tablecfg set table_title = table_title||'12' where table_name = 'test_table' ");
			_dao.update(param);
			
			DaoParam param1 = _dao.createParam("select table_title from dm_tablecfg where table_name = 'test_table' ");
			Map<String,String> mp = _dao.queryMap(param1);
			System.out.println("table_title=" + mp.get("table_title"));
			_tranMng.commitTran();
		} catch (TransactionException e) {
			e.printStackTrace();
		}
	}
}
