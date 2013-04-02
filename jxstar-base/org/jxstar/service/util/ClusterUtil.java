/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.util;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.util.DateUtil;
import org.jxstar.util.config.SystemConfig;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.key.KeyCreator;

/**
 * 集群服务器信息管理工具类；
 * 增加系统变量：sys.iscluster 如果设置为1，表示是集群环境，需要注册集群服务器信息；
 * 服务器的IP、端口信息在JxstarFilter过滤器中获取；
 * 
 * 集群服务器信息注册暂时没用，除非需要支持同步几个服务器中的缓存信息，
 * 也可以采用手工重启所有服务器的方法;
 *
 * @author TonyTan
 * @version 1.0, 2012-9-4
 */
public class ClusterUtil {
	public final static String SERVER_NAME = "server.name"; 
	
	private static BaseDao _dao = BaseDao.getInstance();
	//标记清除遗漏在线用户与遗漏操作记录，只执行一次
	private static boolean _hasClean = false;
	
	/**
	 * 是否是集群环境
	 * @return
	 */
	public static boolean isCluster() {
		return SystemVar.getValue("sys.iscluster").equals("1");
	}
	
	/**
	 * 获取当前缓存的服务器名
	 * @return
	 */
	public static String getServerName() {
		return SystemVar.getValue(SERVER_NAME);
	}
	
	/**
	 * 注册一个服务器，在JxstarFilter调用
	 * 
	 * @param request
	 */
	public static synchronized void regServer(ServletRequest request) {
		String serverName = getServerName(request);
		//初始化操作
		SystemVar.setValue(SERVER_NAME, serverName);
		cleanUp();
		
		//如果没有注册服务器，则注册一个
		if (!hasServer(serverName)) {
			String sql = "insert into sys_cluster(cluster_id, server_name, app_ip, app_port, " +
					"app_path, start_date, run_task) values(?, ?, ?, ?, ?, ?, ?)";
			
			String clustId = KeyCreator.getInstance().createKey("sys_cluster");
			//getServerName()取到的可能是域名或IP，可能存在服务器分发的情况
			String appIp = request.getServerName();
			//getServerPort()取到的可能不是真实端口
			String appPort = Integer.toString(request.getServerPort());
			String appPath = ((HttpServletRequest)request).getContextPath();
			String startDate = DateUtil.getTodaySec();
			String runTask = hasTaskStartup() ? "1" : "0";
			
			DaoParam param = _dao.createParam(sql);
			param.addStringValue(clustId);
			param.addStringValue(serverName);
			param.addStringValue(appIp);
			param.addStringValue(appPort);
			param.addStringValue(appPath);
			param.addDateValue(startDate);
			param.addStringValue(runTask);
			
			_dao.update(param);
		}
	}
	
	/**
	 * 删除当前服务注册信息
	 */
	public static void removeServer() {
		String serverName = getServerName();
		String sql = "delete from sys_cluster where server_name = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(serverName);
		
		_dao.update(param);
	}
	
	/**
	 * 删除遗漏的在线用户与遗漏操作，只能执行一次
	 */
	public static void cleanUp() {
		if (_hasClean) return;
		_hasClean = true;
		
		String serverName = getServerName();
		String[] sqls = {"delete from sys_user_login where server_name = ?", 
				         "delete from sys_doing where server_name = ?", 
				         "delete from sys_cluster where server_name = ?"};
		
		for (String sql : sqls) {
			DaoParam param = _dao.createParam(sql);
			param.addStringValue(serverName);
			_dao.update(param);
		}
	}
	
	/**
	 * 获取当前请求的服务器名
	 * @param request
	 * @return
	 */
	private static String getServerName(ServletRequest request) {
		return request.getServerName() + ":" + request.getServerPort();
	}
	
	/**
	 * 是否已注册当前服务器
	 * @param serverName
	 * @return
	 */
	private static boolean hasServer(String serverName) {
		String sql = "select count(*) as cnt from sys_cluster where server_name = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(serverName);
		Map<String,String> mpCnt = _dao.queryMap(param);
		
		return !(mpCnt.get("cnt").equals("0"));
	}
	
	/**
	 * 判断后台任务是否加载
	 * @return
	 */
	private static boolean hasTaskStartup() {
		List<Map<String,String>> lsTasks = SystemConfig.
		getConfigListByKey("SystemTasks");
		if (lsTasks.isEmpty()) return false;
		
		for (Map<String,String> mpTask : lsTasks) {
			if ("SystemTaskLoader".equals(mpTask.get("classname"))) {
				if ("true".equals(mpTask.get("startup"))) return true;
			}
		}
		
		return false;
	}
}
