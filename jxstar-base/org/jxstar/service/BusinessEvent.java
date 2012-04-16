/*
 * BusinessEvent.java 2009-5-29
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service;

import java.util.Map;


import org.jxstar.control.action.RequestContext;
import org.jxstar.service.define.FunctionDefine;
import org.jxstar.service.define.FunctionDefineManger;

/**
 * 业务事件基础对象。
 *
 * @author TonyTan
 * @version 1.0, 2009-5-29
 */
public abstract class BusinessEvent extends BusinessObject {
	private static final long serialVersionUID = 3080747755569542886L;
	//复制标志
	protected static final String COPYFLAG = "-";
	
	//当前用户信息,有user_id, user_name, dept_id, dept_code
	protected Map<String,String> _userInfo = null;
	//当前功能ID
	protected String _funID = null;
	//当前功能的数据源名
	protected String _dsName = null;
	//当前功能操作的数据表名
	protected String _tableName = null;
	//当前功能的主键
	protected String _pkColName = null;
	//当前功能的编码列
	protected String _codeColName = null;	
	//当前功能的定义对象
	protected FunctionDefine _funObject = null;
	//功能对象管理器
	protected FunctionDefineManger _funManger = FunctionDefineManger.getInstance();
	
	public void init(RequestContext requestContext) 
		throws BoException {	
		//从上下文信息中取功能ID
		_funID = requestContext.getFunID();
		if (_funID == null || _funID.length() == 0) {
			throw new BoException("_funID is null! ");
		}
		//从上下文信息中取当前用户信息
		_userInfo = requestContext.getUserInfo();
		if (_userInfo == null || _userInfo.isEmpty()) {
			throw new BoException("_userInfo is null! ");
		}
		
		//获取功能定义对象
		_funObject = _funManger.getDefine(_funID);
		if (_funObject == null) {
			throw new BoException("_funObject is null! ");
		}
		//取功能的一些基础信息
		_dsName = _funObject.getElement("ds_name");
		_tableName = _funObject.getElement("table_name");
		_pkColName = _funObject.getElement("pk_col");
		_codeColName = _funObject.getElement("code_col");
	}
}
