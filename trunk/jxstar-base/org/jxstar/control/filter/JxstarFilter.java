/*
 * JxstarFilter.java 2008-4-6
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.control.filter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jxstar.service.util.ClusterUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 框架公共过滤器：处理字符集；静态文件缓存；非法SQL过滤。
 * 
 * @author TonyTan
 * @version 1.0, 2008-4-6
 */
public class JxstarFilter implements Filter {
	//字符集编码
    private String _encoding = null;
    //可以缓存的文件类型，用“,”分隔
    private String _cachetype = null;
    //缓存过期天数
    private String _expires = null;
    //需要过滤的非法SQL短语，用“;”分隔
    private String _illegalsql = null;

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse rsp = (HttpServletResponse) response;
		
        if (_encoding != null){
        	req.setCharacterEncoding(_encoding);
        	rsp.setContentType("text/html; charset="+_encoding);
        }
        
        //取请求的文件路径
        String uri = req.getRequestURI();
        //检查缓存的静态文件是否发送了http请求
        /*if (uri.indexOf("graph/js/") >= 0 || uri.indexOf("NodeDefine") >= 0) {
        	System.out.println("------------uri=" + uri);
        }*/
        
        //如果是需要缓存的文件，则设置过期时间
        if (_cachetype != null && _cachetype.length() > 0 && 
        		isCacheType(uri, _cachetype)) {
        	int iday = -1;
        	try {
        		iday = Integer.parseInt(_expires);
        	} catch(Exception e) {
        		e.printStackTrace();
        		iday = -1;
        	}
        	
        	rsp.setHeader("Expires", expireValue(iday));
        } else {
        	rsp.setHeader("Expires", "0");
        }
        
        //如果设置了非法SQL短语，则需要过滤检查
        if (_illegalsql != null && _illegalsql.length() > 0) {
        	String[] words = _illegalsql.toLowerCase().split(";");
        	
        	//取where_sql参数值，如果为空，则不用处理
        	String wheresql = req.getParameter("where_sql"); 
        	if (wheresql == null) wheresql = "";
        	String whereSql = req.getParameter("whereSql");
        	if (whereSql == null) whereSql = "";
        	wheresql += whereSql;
        	if (wheresql != null && wheresql.length() > 0) {
        		wheresql = wheresql.toLowerCase();
        		
        		for (int i = 0, n = words.length; i < n; i++) {
        			if ((words[i].length() > 0) && (wheresql.indexOf(words[i]) >= 0)) {
        				rsp.sendError(401, JsMessage.getValue("jxstarfilter.hasillegalsql", words[i]));
        				return;
        			}
        		}
        	}
        }
        
        //如果是集群环境，如果没有注册服务器，则需要注册
        if (ClusterUtil.isCluster()) {
	        String serverName = ClusterUtil.getServerName();
	        if (serverName.length() == 0) {
	        	ClusterUtil.regServer(request);
	        }
        }
        
        chain.doFilter(req, rsp);
	}

	public void init(FilterConfig config) throws ServletException {
		_encoding = config.getInitParameter("encoding");
		if (_encoding != null) _encoding = _encoding.trim();
		
		_cachetype = config.getInitParameter("cachetype");
		if (_cachetype != null) _cachetype = _cachetype.trim();
		
		_expires = config.getInitParameter("expires");
		if (_expires != null) _expires = _expires.trim();
		
		_illegalsql = config.getInitParameter("illegalsql");
		if (_illegalsql != null) _illegalsql = _illegalsql.trim();
	}
	
	public void destroy() {
		_encoding = null;
		_cachetype = null;
		_expires = null;
		_illegalsql = null;
	}
	
	/**
	 * 判断是否为缓存类型的文件
	 * @param uri -- 文件名
	 * @param cachetype -- 缓存文件类型，格式为：js,css,jpg,png,gif,ico，用,分隔
	 * @return
	 */
	private boolean isCacheType(String uri, String cachetype) {
		if (uri == null || uri.length() == 0 ||
				cachetype == null || cachetype.length() == 0) {
			return false;
		}
		
		String[] fs = uri.split("\\.");
		if (fs.length < 2) return false;
		
		//取文件扩展名
		String ext = fs[1].toLowerCase();
		
		//如果存在文件扩展名，则返回
		String[] ct = cachetype.toLowerCase().split(",");
		for (int i = 0; i < ct.length; i++) {
			if (ct[i].equals(ext)) return true;
		}
		
		return false;
	}
	
	/**
	 * 取间隔天数后的时间值，格式为：MMM dd yyyy HH:mm:ss 'GMT'
	 * @param day -- 间隔天数
	 * @return
	 */
	private String expireValue(int day) {
		Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DAY_OF_MONTH, day);
    	
    	//月份必须是英文格式
    	SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy HH:mm:ss 'GMT'", Locale.US);
    	return sdf.format(cal.getTime());
	}
}
