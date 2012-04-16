/*
 * CompareCfgBO.java 2010-12-24
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.studio;

import java.util.List;

import org.jxstar.dm.DmException;
import org.jxstar.dm.compare.CompareDB;
import org.jxstar.dm.compare.CompareData;
import org.jxstar.dm.util.DmConfig;
import org.jxstar.dm.util.DmUtil;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.ArrayUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 对比配置表与原表中的配置信息是否相同：
 * 1、根据主键值查找，把配置表中
 * 对比配置表中每个字段的值是否相等，如果不等，则生成update语句，更新原表中的数据；
 *
 * @author TonyTan
 * @version 1.0, 2010-12-24
 */
public class CompareCfgBO extends BusinessObject {
	private static final long serialVersionUID = 6699436549388549668L;
	
	/**
	 * 比较配置表与数据库对象之间的差别。
	 * @return
	 */
	public String compareDbCfg() {
		List<String> lssql = null;
		try {
			CompareDB compare = new CompareDB();
			lssql = compare.compareTable();
		} catch (DmException e) {
			_log.showError(e);
			setMessage(e.getMessage());
			return _returnFaild;
		}
		String sql = ArrayUtil.listToString(lssql, "");
		//_log.showDebug("----------sql=" + sql);
		
		//返回验证SQL到前台
		sql = StringUtil.strForJson(sql);
		setReturnData("{sql:'"+ sql +"'}");
		
		return _returnSuccess;
	}
	
	/**
	 * 提交更新sql
	 * @param sql -- SQL语句
	 * @return
	 */
	public String commitSQL() {
		List<String> lssql = null;
		try {
			lssql = getCompareSQL();
		} catch (DmException e) {
			_log.showError(e);
			setMessage(e.getMessage());
			return _returnFaild;
		}
		//_log.showDebug("----------sql=" + sql);
		
		try {
			DmUtil.executeSQL(lssql, "default");
		} catch (DmException e) {
			_log.showError(e);
			setMessage(e.getMessage());
			return _returnFaild;
		}
		
		return _returnSuccess;
	}

	/**
	 * 比较三个配置表中的数据是否相同，如果不等则生成更新SQL。
	 * @return
	 */
	public String compareCfg() {
		List<String> lssql = null;
		try {
			lssql = getCompareSQL();
		} catch (DmException e) {
			_log.showError(e);
			setMessage(e.getMessage());
			return _returnFaild;
		}
		String sql = ArrayUtil.listToString(lssql, "");
		//_log.showDebug("----------sql=" + sql);
		
		//返回验证SQL到前台
		sql = StringUtil.strForJson(sql);
		setReturnData("{sql:'"+ sql +"'}");
		
		return _returnSuccess;
	}
	
	/**
	 * 取差异SQL
	 * @return
	 */
	private List<String> getCompareSQL() throws DmException {
		//配置表中不能有非“完成”状态的记录，这样可以防止正式表中出现非“完成”状态的记录
		if (DmConfig.hasNotComplete("dm_tablecfg")) {
			//"配置表【{0}】中存在未“完成”状态的记录，不能比较！"
			throw new DmException(JsMessage.getValue("comparecfgbo.hasover"), "dm_tablecfg");
		}
		if (DmConfig.hasNotComplete("dm_fieldcfg")) {
			throw new DmException(JsMessage.getValue("comparecfgbo.hasover"), "dm_fieldcfg");
		}
		if (DmConfig.hasNotComplete("dm_indexcfg")) {
			throw new DmException(JsMessage.getValue("comparecfgbo.hasover"), "dm_indexcfg");
		}
		
		CompareData compare = new CompareData();
		List<String> lssql = FactoryUtil.newList();
		//比较{0}与{1}表中的数据差异
		lssql.add("--"+ JsMessage.getValue("comparecfgbo.cfgdiff", "dm_tablecfg", "dm_table") +"\r\n");
		lssql.addAll(compare.compareSQL("dm_tablecfg", "default", "dm_table", "default"));
		
		lssql.add("--"+ JsMessage.getValue("comparecfgbo.cfgdiff", "dm_fieldcfg", "dm_field") +"\r\n");
		lssql.addAll(compare.compareSQL("dm_fieldcfg", "default", "dm_field", "default"));
		
		lssql.add("--"+ JsMessage.getValue("comparecfgbo.cfgdiff", "dm_indexcfg", "dm_index") +"\r\n");
		lssql.addAll(compare.compareSQL("dm_indexcfg", "default", "dm_index", "default"));
		
		return lssql;
	}
	
}
