/*
 * DaoParam.java 2009-10-6
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao;

import java.util.List;


import org.jxstar.dao.pool.DataSourceConfig;
import org.jxstar.dao.util.SQLParseException;
import org.jxstar.dao.util.SqlParser;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.factory.SystemFactory;

/**
 * DAO的参数对象，用于给PreparedStatement对象设置参数。
 * 业务对象中常用的方法是：
 * addStringValue, addDateValue, addDoubleValue, addIntValue
 *
 * @author TonyTan
 * @version 1.0, 2009-10-6
 */
public class DaoParam {
	//数据源名
	private String _dsName = "";
	//SQL是否需要解析
	private boolean _useParse = false;
	//是否支持公共事务
	private boolean _useTransaction = true; 
	//执行SQL
	private String _sql = "";
	//参数类型
	private List<String> _lsType = null;
	//参数值
	private List<String> _lsValue = null;
	
	public DaoParam() {
		_lsType = FactoryUtil.newList();
		_lsValue = FactoryUtil.newList();
	}
	
	/**
	 * 如果数据源名为空，则取server.xml中系统设置的缺省数据源名。
	 * @return
	 */
	public String getDsName() {
		if (_dsName == null || _dsName.length() == 0) {
			_dsName = DataSourceConfig.getDefaultName();
		}
		
		return _dsName;
	}
	
	/**
	 * 取SQL语句，先判断是否需要解析{function}，如果需要则先解析SQL中的函数名
	 * @return
	 */
	public String getSql() {
		if (_useParse) {
			SqlParser parser = (SqlParser) SystemFactory.createSystemObject("SqlParser");
			try {
				_sql = parser.parse(_sql);
			} catch (SQLParseException e) {
				e.printStackTrace();
				return "";
			}
		}
		return _sql;
	}

	public void setSql(String sql) {
		this._sql = sql;
	}
	
	/**
	 * 设置是否需要解析SQL语句。
	 * @return
	 */
	public boolean isUseParse() {
		return _useParse;
	}
	
	/**
	 * 如果设置为是，则getSql时会处理SQL语句中的跨数据库的函数标识，缺省值为false。
	 * 
	 * @param useParse -- 是否需要解析SQL语句
	 */
	public void setUseParse(boolean useParse) {
		_useParse = useParse;
	}
	
	/**
	 * 是否采用公共事务处理。
	 * @return
	 */
	public boolean isUseTransaction() {
		return _useTransaction;
	}
	
	/**
	 * 如果设置为是，系统数据库执行采用公共事务处理，一个完整的业务操作都在一个事务中；
	 * 如果设置为否，则当前数据库操作不在公共事务中，而是由一个新的数据库连接处理操作；
	 * 缺省设置为是，因为绝对大多少数据库操作都要求在一个事务中，暂时还未用到该参数。
	 * 
	 * @param transaction -- 是否采用公共事务操作
	 */
	public void setUseTransaction(boolean transaction) {
		_useTransaction = transaction;
	}
	
	public List<String> getType() {
		return _lsType;
	}
	public List<String> getValue() {
		return _lsValue;
	}
	
	public DaoParam setDsName(String name) {
		_dsName = name;
		return this;
	}
	public DaoParam addType(String type) {
		_lsType.add(type);
		return this;
	}
	public DaoParam addValue(String value) {
		_lsValue.add(value);
		return this;
	}
	
	/**
	 * 添加字符类型的参数
	 * @param value
	 * @return
	 */
	public DaoParam addStringValue(String value) {
		_lsValue.add(value);
		_lsType.add("string");
		return this;
	}
	/**
	 * 添加日期类型的参数
	 * @param value
	 * @return
	 */
	public DaoParam addDateValue(String value) {
		_lsValue.add(value);
		_lsType.add("date");
		return this;
	}
	/**
	 * 添加数字类型的参数
	 * @param value
	 * @return
	 */
	public DaoParam addDoubleValue(String value) {
		_lsValue.add(value);
		_lsType.add("double");
		return this;
	}
	/**
	 * 添加整数类型的参数
	 * @param value
	 * @return
	 */
	public DaoParam addIntValue(String value) {
		_lsValue.add(value);
		_lsType.add("int");
		return this;
	}
	/**
	 * 设置多个参数类型，以;分隔，如：string;string;date
	 * @param type
	 * @return
	 */
	public DaoParam setType(String type) {
		if (type == null || type.length() == 0) 
			return this;
		
		return setTypes(type.split(";"));
	}
	/**
	 * 设置多个参数值，以;分隔，如：abc;bcd;2012-11-11
	 * @param value
	 * @return
	 */
	public DaoParam setValue(String value) {
		if (value == null || value.length() == 0) 
			return this;
		
		return setValues(value.split(";"));
	}
	/**
	 * 设置多个参数类型
	 * @param atype
	 * @return
	 */
	public DaoParam setTypes(String[] atype) {
		if (atype == null || atype.length == 0) 
			return this;
		
		for (int i = 0, n = atype.length; i < n; i++) {
			_lsType.add(atype[i]);
		}
		return this;
	}
	/**
	 * 设置多个参数值
	 * @param avalue
	 * @return
	 */
	public DaoParam setValues(String[] avalue) {
		if (avalue == null || avalue.length == 0) 
			return this;
		
		for (int i = 0, n = avalue.length; i < n; i++) {
			_lsValue.add(avalue[i]);
		}
		return this;
	}
	/**
	 * 清除所有参数
	 */
	public DaoParam clearParam() {
		_lsType.clear();
		_lsValue.clear();
		_dsName = "";
		return this;
	}
}
