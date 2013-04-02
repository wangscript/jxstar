/*
 * Copyright(c) 2013 Donghong Inc.
 */
package org.jxstar.dao;

import java.sql.Types;
import java.util.List;

import org.jxstar.util.factory.FactoryUtil;

/**
 * 处理存储过程返回值。
 *
 * @author TonyTan
 * @version 1.0, 2013-3-29
 */
public class CallParam extends DaoParam {
	//输出参数类型
	private List<Integer> _lsOutType = FactoryUtil.newList();
	//输出参数值
	private List<String> _lsOutValue = FactoryUtil.newList();
	
	/**
	 * 注册字符类型的参数
	 * @return
	 */
	public DaoParam regStringOutParam() {
		_lsOutType.add(Types.VARCHAR);
		return this;
	}
	/**
	 * 注册日期类型的参数
	 * @return
	 */
	public DaoParam regDateOutParam() {
		_lsOutType.add(Types.DATE);
		return this;
	}
	/**
	 * 注册数字类型的参数
	 * @return
	 */
	public DaoParam regDoubleOutParam() {
		_lsOutType.add(Types.DOUBLE);
		return this;
	}
	/**
	 * 注册整数类型的参数
	 * @return
	 */
	public DaoParam regIntOutParam() {
		_lsOutType.add(Types.INTEGER);
		return this;
	}
	
	/**
	 * 获取注册的输出参数
	 * @return
	 */
	public List<Integer> getOutType() {
		return _lsOutType;
	}
	
	/**
	 * 获取输出参数值
	 * @param index -- 从0开始，与注册的输出参数序号相同
	 * @return
	 */
	public String getOutValue(int index) {
		if (index >= _lsOutValue.size()) return "";
		
		String value = _lsOutValue.get(index);
		if (value == null) value = "";
		
		return value;
	}
	
	/**
	 * 设置输出参数的值
	 * @param value
	 */
	public void setOutValue(String value) {
		_lsOutValue.add(value);
	}
}
