/*
 * StartupController.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.control;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jxstar.service.studio.LoginLogBO;
import org.jxstar.service.util.ClusterUtil;
import org.jxstar.task.load.SystemTaskLoader;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.log.Log;
import org.jxstar.util.system.SystemInitUtil;

/**
 * 系统初始化的控制器，重新装载系统所有配置。
 *
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class StartupController extends HttpServlet {
	private static final long serialVersionUID = 3080747755569542886L;
	//系统配置文件名
	private static final String SERVER_CONFIG = "/WEB-INF/conf/server.xml";
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		systemInit(false);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		String operateType = request.getParameter("isReload");
		if (operateType != null && operateType.equals("true")) {
			systemInit(true);
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		doGet(request, response);
	}
	
	//程序卸载时注销所有线程
	public void destroy() {
		System.out.println(".............StartupController.destroy!!");
		SystemTaskLoader loader = new SystemTaskLoader();
		loader.destroyAllThread();
	}

	/**
	 * 初始化系统
	 * 
	 * @param isReload 是否重新装载
	 */
	private void systemInit(boolean isReload) {
        //程序路径
        String realPath = getServletContext().getRealPath("/");
        realPath = realPath.replace('\\', '/');
        if (realPath.charAt(realPath.length()-1) == '/') {
            realPath = realPath.substring(0, realPath.length()-1);
        }
	    SystemVar.REALPATH = realPath;
		
		//获取系统核心配置文件
		String configFile = getInitParameter("config");
		if (configFile == null || configFile.length() == 0) {
			configFile = SERVER_CONFIG;
		}
		
		//初始化日志对象
		String logFile = getInitParameter("logfile");
		Log.getInstance().init(realPath + "/WEB-INF/classes/", logFile);
		Log.getInstance().showDebug("jxstar realpath=" + SystemVar.REALPATH);
		
		//初始化系统对象
		SystemInitUtil.initSystem(realPath + "/WEB-INF/classes/", configFile, isReload);
		
		//系统重启其删除所有会话用户，如果在集群环境，则通过cleanUp方法完成
		if (!ClusterUtil.isCluster()) {
			(new LoginLogBO()).delLoginUser();
		}
	}
}
