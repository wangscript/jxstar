/*
 * ActionHelper.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.control.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.DefaultFileItemFactory;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;
import org.jxstar.util.resource.JsMessage;
import org.jxstar.util.resource.JsParam;

/**
 * Action对象的助手对象，负责把HttpServletRequest对象的值封装为RequestContext对象。
 * 把应用程序在服务器中的绝对路径保存到请求对象了。
 * 如果是multipart格式的请求，则前台发送的请求编码格式是gbk，需要转换为utf-8。
 * 以上两项在TOMCAT服务器测试通过，要注意在其它中间件服务器中是否正确。
 *
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class ActionHelper {
	private static final Log _log = Log.getInstance();
	
	/**
	 * 创建RequestContext，封装了HttpServletRequest请求值，并添加了两个系统参数值：
	 * 系统的绝对路径[sys.realpath]与客户端的IP地址[sys.realpath]
	 * @param request
	 * @return
	 */
    public static RequestContext getRequestContext(
						HttpServletRequest request) throws ActionException {
		Map<String,Object> requestMap;
		boolean isMultipart = DiskFileUpload.isMultipartContent(request);
		_log.showDebug("request is have file upload: " + isMultipart);
		if (isMultipart) {
			requestMap = parseMultiRequest(request);
		} else {
			requestMap = parseRequestParam(request);
		}
		_log.showDebug("------------request param=" + requestMap.toString());
		//保存系统路径
		requestMap.put(JsParam.REALPATH, SystemVar.REALPATH);
		
		//请求参数对象
		RequestContext requestContext = new RequestContext(requestMap);
		//客户端相关信息，是一个Map<String,String>对象，包含所有请求头参数、client_ip、session_id
		requestContext.setClientInfo(getClientInfo(request));
		
		//取功能ID保存到Context中
		String funid = requestContext.getRequestValue(JsParam.FUNID);
		requestContext.setFunID(funid);
		
		//取页面类型
		String pageType = requestContext.getRequestValue(JsParam.PAGETYPE);
		requestContext.setPageType(pageType);
		
		//取eventCode，取请求中参数名称为eventCode的值
		String eventCode = requestContext.getRequestValue(JsParam.EVENTCODE);
		requestContext.setEventCode(eventCode);
		
		return requestContext;
	}
	
	private static Map<String,Object> parseRequestParam(
						HttpServletRequest request) throws ActionException {
		Map<String,Object> requestMap = FactoryUtil.newMap();
		Enumeration<String> paramNames = request.getParameterNames();
		
		//所有参数存到map中
		while (paramNames.hasMoreElements()) {
			String name = paramNames.nextElement();
			String[] value = request.getParameterValues(name);
			
			//把表名中的__转换为.，作为参数保存
			name = name.replace("__", ".");
			if (value == null) {
				requestMap.put(name, "");
			} else if (value.length == 1) {
				requestMap.put(name, value[0]);
			} else {
				requestMap.put(name, value);
			}
		}
		
		return requestMap;
	}
	
	/**
	 * 解析请求对象中的上传文件内容
	 * @param request
	 * @return
	 */
    private static Map<String,Object> parseMultiRequest(
						HttpServletRequest request) throws ActionException {
		//创建一个基于磁盘文件的工厂
		DefaultFileItemFactory factory = new DefaultFileItemFactory();
		//创建一个文件处理器
		DiskFileUpload upload = new DiskFileUpload(factory);
		//必须设置解析编码，否则汉字会乱码
		upload.setHeaderEncoding("utf-8");
		//设置最大上传文件，缺省值为10M
		String maxSize = SystemVar.getValue("upload.file.maxsize", "10");
		upload.setSizeMax(1000*1000*Integer.parseInt(maxSize));
		//解析请求
		List<FileItem> items = null;
		try {
			items = upload.parseRequest(request);
		} catch (FileUploadException e) {
			_log.showError(e);
			throw new ActionException(JsMessage.getValue("fileaction.overmaxsize"), maxSize);
		}
		_log.showDebug("request item size=" + items.size());

		//请求对象中的值
		Map<String,Object> requestMap = FactoryUtil.newMap();
		// 处理文件上传项
		Iterator<FileItem> iter = items.iterator();
		while (iter.hasNext()) {
			FileItem item = iter.next();
			if (item.isFormField()) {
	    	    String key = item.getFieldName();
	    	    //必须要设置编码，否则取值会乱码
	    	    String value = "";
				try {
					value = item.getString("utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
	    	    if (key == null || key.length() == 0) continue;
	    	    requestMap.put(key, value);
			} else {
	    	    String key = item.getFieldName();
	    	    requestMap.put(key, item);
	    	    //需要转码
	    	    String fileName = item.getName();
	    	    String contentType = item.getContentType();
	    	    long fileSize = item.getSize();
	    	    _log.showDebug("request filename=" + fileName + ";fileSize=" + fileSize + ";contentType=" +contentType);
			}
		}
		
		return requestMap;
	}
    
    /**
     * 把请求头中相关信息写入
     * @param request
     */
    private static Map<String,String> getClientInfo(HttpServletRequest request) {
    	Map<String,String> mpInfo = FactoryUtil.newMap();
    	
    	//保存会话ID
    	HttpSession session = request.getSession();
    	mpInfo.put("session_id", session.getId());
    	//保存客户端信息
    	mpInfo.put("client_ip", request.getRemoteAddr());
    	
    	//保存请求头信息
    	@SuppressWarnings("rawtypes")
		Enumeration em = request.getHeaderNames();
    	while(em.hasMoreElements()) {
    		String name = (String) em.nextElement();
    		String value = request.getHeader(name);
    		mpInfo.put(name.toLowerCase(), value);
    	}
    	
    	return mpInfo;
    }
	
	/**
	 * 把请求对象中的响应结果反馈到响应对象中
	 * 
	 * @param request - 请求环境对象
	 * @param response - 响应对象
	 */
	public static void contextToResponse(RequestContext request, 
			ResponseContext response) {
		if (request == null) return;
		if (response == null) return;
		
		response.setMessage(request.getMessage());
		response.setResponseData(request.getReturnData());
	}
	
	/**
	 * 根据浏览器类型返回附件文件名编码后的字符串。
	 * @param userAgent -- 浏览器类型
	 * @param fileName -- 附件文件名
	 * @return
	 */
	public static String getAttachName(String userAgent, String fileName) {
		if (userAgent == null || fileName == null) {
			return fileName;
		}
		
		userAgent = userAgent.toLowerCase();
		try {
			if (userAgent.indexOf("msie") > -1){
				fileName = URLEncoder.encode(fileName, "utf-8");
			} else {
				fileName = new String(fileName.getBytes("utf-8"), "iso-8859-1");
				fileName = "\"" + fileName + "\"";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return fileName;
	}
}
