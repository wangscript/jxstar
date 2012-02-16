/*
 * ProjectInfo.java 2010-2-20
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.studio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.pool.DataSourceConfigManager;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.resource.JsMessage;
import org.jxstar.util.resource.JsParam;

/**
 * 处理开发平台的数据源。
 *
 * @author TonyTan
 * @version 1.0, 2010-2-20
 */
public class ProjectInfo extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 测试数据源的正确性
	 * @param sourceID -- 数据源ID
	 * @return
	 */
	public String onTest(String sourceID) {
		Map<String, String> mpSrc = getSource("src_id", sourceID);
		if (mpSrc == null || mpSrc.isEmpty()) {//"提示：没有定义数据源！"
			setMessage(JsMessage.getValue("project.nosrc"));
			return _returnFaild;
		}
			
		String jdbcUrl = mpSrc.get("jdbcurl");
		String username = mpSrc.get("username");
		String password = mpSrc.get("password");
		String driveName = mpSrc.get("drive_path");
		
		Connection conn = null;
		try {
			Class.forName(driveName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			conn = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (conn == null) {//"提示：创建数据库连接为空！"
			setMessage(JsMessage.getValue("project.connull"));
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 选择项目的数据源进入系统。
	 * @param projectID -- 项目ID
	 * @param requestContext -- 上下文环境
	 * @return
	 */
	public String onSelect(String projectID, RequestContext requestContext) {
		Map<String, String> mpSrc = getSource("project_id", projectID);
		if (mpSrc == null || mpSrc.isEmpty()) {//"提示：没有定义数据源！"
			setMessage(JsMessage.getValue("project.nosrc"));
			return _returnFaild;
		}
		
		//修改设计项目的数据源
		String jdbcUrl = mpSrc.get("jdbcurl");
		String dbmsType = mpSrc.get("dbmstype");
		String userName = mpSrc.get("username");
		String schemaName = mpSrc.get("schemaname");
		String passWord = mpSrc.get("password");
		String driveName = mpSrc.get("drive_path");
		DataSourceConfigManager dcm = DataSourceConfigManager.getInstance();
		dcm.addDataSourceConfig("design", schemaName, driveName, jdbcUrl, userName, passWord, dbmsType);
		
		//修改设计项目的实际路径
		Map<String, String> mpPro = getSource(projectID);
		String path = mpPro.get("project_path");
		requestContext.setRequestValue(JsParam.REALPATH, path);
		
		return _returnSuccess;
	}
	
	/**
	 * 取项目数据源定义信息
	 * @param fieldName -- 字段名
	 * @param sourceID -- 数据源定义ID
	 * @return
	 */
	public Map<String, String> getSource(String fieldName, String sourceID) {
		String sql = "select src_code, host, drive_path, dbname, jdbcurl, username, password, dbmstype from project_src where "+fieldName+" = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(sourceID);
		return _dao.queryMap(param);
	}
	
	/**
	 * 取项目信息
	 * @param projectID	-- 项目ID
	 * @return
	 */
	public Map<String, String> getSource(String projectID) {
		String sql = "select project_code, project_name, project_path, project_app from project_list where project_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(projectID);
		return _dao.queryMap(param);
	}
}
