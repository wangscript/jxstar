/*
 * ResponseContext.java 2009-6-14
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.control.action;

import org.jxstar.util.StringUtil;

/**
 * 响应信息对象。
 *
 * @author TonyTan
 * @version 1.0, 2009-6-14
 */
public class ResponseContext {
	//执行是否成功
	private boolean _succeed = true;
	//给前台的提示信息
	private String _message = "";
	//给前台的数据信息
	private StringBuilder _responseData = new StringBuilder("");
	
	public ResponseContext() {
	}
	
	public ResponseContext(boolean succeed) {
		this._succeed = succeed;
	}
	
	//返回文本格式的数据
	public String reponseText() {
		String msg = StringUtil.strForJson(getMessage());
		
		StringBuilder sbres = new StringBuilder();
		sbres.append("{success:" + isSucceed() + ", ");
		sbres.append("message:'" + msg + "', ");
		sbres.append("data:" + getResponseData());
		sbres.append("}");
		
		return sbres.toString();
	}
	
	//返回XML格式的数据
	public String reponseXml() {
		String xml = getResponseData();
		xml = xml.substring(1, xml.length()-1);
		
		StringBuilder sbres = new StringBuilder();
		sbres.append("<?xml version='1.0' encoding='utf-8'?>\n");
		sbres.append("<result>\n");
		sbres.append("\t<success>" + isSucceed() + "</success>\n");
		sbres.append("\t<message>" + getMessage() + "</message>\n");
		sbres.append("\t<data><![CDATA[");
		sbres.append(xml);
		sbres.append("]]>\n</data>\n");
		sbres.append("</result>\n");
		
		return sbres.toString();
	}

	public boolean isSucceed() {
		return _succeed;
	}

	public void setSuccessed(boolean succeed) {
		this._succeed = succeed;
	}

	public String getMessage() {
		return _message;
	}

	public void setMessage(String message) {
		this._message = message;
	}

	public String getResponseData() {
		if (_responseData.length() == 0) {
			_responseData.append("{}");
		}
		
		return _responseData.toString();
	}

	public void setResponseData(String data) {
		_responseData.append(data);
	}
}
