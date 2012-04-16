/*
 * BusinessObject.java 2009-6-23
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service;

import java.io.Serializable;
import java.text.MessageFormat;


import org.jxstar.dao.BaseDao;
import org.jxstar.util.log.Log;

/**
 * 业务逻辑组件基类，所有自定义业务处理类都必须继承该对象。
 * 业务处理类中主要使用_log, _dao, setMessage, setReturnData方法与服务层交互。
 *
 * @author TonyTan 2009-6-23
 */
abstract public class BusinessObject implements Serializable {
	private static final long serialVersionUID = 1L;
	//返回给服务层的提示信息
	private String _message = "";
	//返回给服务层的数据
	private String _returnData = "";
	
	/**
	 * 业务方法执行成功的返回值
	 */
	protected static String _returnSuccess = "true";
	/**
	 * 业务方法执行失败的返回值
	 */
	protected static String _returnFaild = "false";
	/**
	 * 业务方法中可以直接使用的LOG对象
	 */
	protected Log _log = Log.getInstance();
	/**
	 * 业务方法中可以直接使用的DAO对象
	 */
	protected BaseDao _dao = BaseDao.getInstance();
	
	/**
	 * 设置返回给服务层的提示信息
	 * 
	 * @param sMsg - 提示信息
	 */
	protected void setMessage(String sMsg) {
		_message = sMsg;
	}
	
	/**
	 * 设置返回给服务层的提示信息，可以带参数{0}{1}...
	 * 
	 * @param sMsg - 提示信息
	 */
	protected void setMessage(String sMsg, Object ... param) {
		sMsg = MessageFormat.format(sMsg, param);
		_message = sMsg;
	}
	
	/**
	 * 获取返回给服务层的提示信息
	 */
	public String getMessage() {
		return _message;
	}
	
	/**
	 * 设置返回给服务层的数据，必须采用JSON格式与XML格式
	 * 
	 * @param sData - 返回的数据，数据格式采用JSON格式
	 */
	protected void setReturnData(String sData) {
		_returnData = sData;
	}
	
	/**
	 * 获取返回给服务层的数据，必须采用JSON格式与XML格式
	 */
	public String getReturnData() {
		return _returnData;
	}
}
