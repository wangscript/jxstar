/*
 * ModifyDataIdTest.java 2010-11-25
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;

import java.util.List;
import java.util.Map;


import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.transaction.TransactionException;
import org.jxstar.dao.transaction.TransactionManager;
import org.jxstar.test.AbstractTest;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.key.KeyCreator;

/**
 * 修改系统数据ID的处理类。
 *
 * @author TonyTan
 * @version 1.0, 2010-11-25
 */
public class ModifyDataIdTest extends AbstractTest {
	private static BaseDao _dao = BaseDao.getInstance();
	private static KeyCreator _key = KeyCreator.getInstance();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TransactionManager _tranMng = (TransactionManager) SystemFactory.createSystemObject("TransactionManager");
		
		_tranMng.startTran();
		try {
		ModifyDataIdTest.funall_domain_event();
		}catch(Exception e) {
			try {
				_tranMng.commitTran();
			} catch (TransactionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try {
			_tranMng.commitTran();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void funall_domain_event() {
		String table = "fun_rule_param";
		String pkname = "param_id";
		_dao.update(_dao.createParam("update sys_tableid set max_value = 0 where table_name = '"+table+"'"));
		
		//---------------------------------------------------------
		String sql = "select * from "+table;
					 //+" where fun_id in ('sys_control', 'fun_tree', 'fun_colext', 'project_list', 'project_src', 'rule_param', 'rule_route') ";
					 //+" order by event_id";
		//---------------------------------------------------------
		
		String usql = "update "+table+" set "+pkname+" = ? where "+pkname+" = ?";
		DaoParam param = _dao.createParam(usql);
		
		List<Map<String, String>> ls = _dao.query(_dao.createParam(sql));
		for (int i = 0, n = ls.size(); i < n; i++) {
			Map<String, String> mp = ls.get(i);
			
			String keyid = mp.get(pkname);
			String newid = _key.createKey(table);
			param.addStringValue(newid);
			param.addStringValue(keyid);
			
			_dao.update(param);
			
			param.clearParam();
		}
	}

	public static void funall_control() {
		String table = "funall_control";
		String pkname = "control_id";
		_dao.update(_dao.createParam("update sys_tableid set max_value = 0 where table_name = '"+table+"'"));
		
		//---------------------------------------------------------
		String sql = "select * from funall_control where "+
					 "control_id like 'jxstar%' or control_id like 'tm%' "+
					 "order by control_code, control_index";
		//---------------------------------------------------------
		
		String usql = "update "+table+" set "+pkname+" = ? where "+pkname+" = ?";
		DaoParam param = _dao.createParam(usql);
		
		List<Map<String, String>> ls = _dao.query(_dao.createParam(sql));
		for (int i = 0, n = ls.size(); i < n; i++) {
			Map<String, String> mp = ls.get(i);
			
			String keyid = mp.get(pkname);
			String newid = _key.createKey(table);
			param.addStringValue(newid);
			param.addStringValue(keyid);
			
			_dao.update(param);
			
			param.clearParam();
		}
	}
}
