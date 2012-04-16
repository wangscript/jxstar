/*
 * RequestContext.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.control.action;

import java.util.Map;

import org.jxstar.util.MapUtil;
import org.jxstar.util.factory.FactoryUtil;


/**
 * 请求参数对象，与协议无关，方法模仿ServletRequest对象。
 *
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class RequestContext {
	//保存功能ID
	private String _funID = "";	
	//保存页面类型
	private String _pageType = "";	
	//保存事件代码
	private String _eventCode = "";
	//响应提示信息内容
	private String _message = "";
	//给前台的数据信息
	private StringBuilder _returnValue = new StringBuilder();
	//会话中用户信息
	private Map<String,String> _userInfo = null;
	//request中的客户端信息
	private Map<String,String> _clientInfo = null;
	//保存请求参数名称与请求参数对象
	private Map<String,Object> _requestMap = null;
	//给前台返回二进制数据，主要用于返回文件的字节信息
	private byte[] _returnBytes = null;
	//给前台返回文件对象，主要用于返回xls文件对象
	private Object _returnObject = null;
	
	public RequestContext(Map<String, Object> requestMap) {
		if (requestMap != null) {
			_requestMap = requestMap;
		} else {
			_requestMap = FactoryUtil.newMap();
		}
	}
	
	/**
	 * 根据名称取请求参数中的大对象
	 * @param name
	 * @return
	 */
	public Object getRequestObject(String name) {
		return _requestMap.get(name);
	}
	
	/**
	 * 根据名称取请求参数值
	 * @param name
	 * @return
	 */
	public String getRequestValue(String name) {
		return MapUtil.getValue(_requestMap, name);
	}
	
	/**
	 * 设置环境对象的参数值，主要用于传递后台构建的参数值，如：
	 * 新增单条或多条件记录的ID值，在其他定义的业务组件中要用，就通过设置参数实现。
	 * @param name -- 参数名
	 * @param value -- 参数值，可以是字符串或数组
	 * @return
	 */
	public void setRequestValue(String name, Object value) {
		_requestMap.put(name, value);
	}
	
	/**
	 * 根据名称取请求参数值，如果为空则取缺省值
	 * @param name
	 * @param defval
	 * @return
	 */
	public String getRequestValue(String name, String defval) {
		return MapUtil.getValue(_requestMap, name, defval);
	}	
	
	/**
	 * 根据名称取请求参数数组
	 * @param name
	 * @return
	 */
	public String[] getRequestValues(String name) {
		return MapUtil.getValues(_requestMap, name);
	}
	
	/**
	 * 取参数名称数组
	 * @return
	 */
	public String[] getRequestNames() {
		return MapUtil.getParameterNames(_requestMap);
	}
	
	public void setFunID(String funid) {
		_funID = funid;
	}	
	
	public String getPageType() {
		return _pageType;
	}

	public void setPageType(String type) {
		_pageType = type;
	}	
	
	public String getEventCode() {
		return _eventCode;
	}

	public void setEventCode(String code) {
		_eventCode = code;
	}	
	
	public String getFunID() {
		return _funID;
	}
	
	public Map<String, Object> getRequestMap() {
		return _requestMap;
	}

	public void setRequestMap(Map<String, Object> map) {
		_requestMap = map;
	}
	
	public Map<String, String> getUserInfo() {
		return _userInfo;
	}

	public void setUserInfo(Map<String, String> info) {
		_userInfo = info;
	}	
	
	public Map<String, String> getClientInfo() {
		return _clientInfo;
	}

	public void setClientInfo(Map<String, String> info) {
		_clientInfo = info;
	}
	
	public String getMessage() {
		return _message;
	}

	public void setMessage(String message) {
		this._message = message;
	}

	/**
	 * 取返回值，是JSON对象。
	 * @return
	 */
	public String getReturnData() {
		if (_returnValue.length() == 0) {
			_returnValue.append("{}");
		}
		
		//如果是传递参数需要加{}
		char c = _returnValue.charAt(0);
		if (c != '{' && c != '[') {
			_returnValue.insert(0, "{");
			_returnValue.append("}");			
		}
		
		return _returnValue.toString();
	}

	/**
	 * 设置完整的返回JSON对象，可以是{}或[]格式，并把原返回值清空。
	 * 
	 * @param data
	 */
	public void setReturnData(String data) {
		_returnValue.delete(0, _returnValue.length());
		_returnValue.append(data);
	}
	
	/**
	 * 设置返回值，合并为JSON对象。
	 * @param name -- 属性名
	 * @param data -- 属性值
	 */
	public void setReturnValue(String name, String data) {
		if (_returnValue.length() > 0) {
			_returnValue.append(",");
		}
		_returnValue.append("'" + name)
			.append("':'").append(data).append("'");
	}

	/**
	 * 取后台返回的二进制数据，一般用于返回二进制文件，可以在BO中取到文件内容，
	 * 由out对象输出文件到页面，这样的业务类要采用FileAction。
	 * setRequestValue在后台设置参数，ContentType响应头类型，Attachment附件文件名
	 * 
	 * @return
	 */
	public byte[] getReturnBytes() {
		return _returnBytes;
	}

	public void setReturnBytes(byte[] bytes) {
		_returnBytes = bytes;
	}
	
	/**
	 * 取后台返回的文件对象，一般用于返回xls文件对象HSSFWorkbook，可以在BO中取到文件内容，
	 * 由HSSFWorkbook.write(out)输出到页面，这样的业务类要采用FileAction。
	 * setRequestValue在后台设置参数，ContentType响应头类型，Attachment附件文件名
	 * 
	 * @return
	 */
	public Object getReturnObject() {
		return _returnObject;
	}

	public void setReturnObject(Object object) {
		_returnObject = object;
	}
}
