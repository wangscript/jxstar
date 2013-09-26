/*
 * Copyright(c) 2013 Donghong Inc.
 */
package org.jxstar.control.login;

import java.util.Calendar;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.util.SysUserUtil;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.key.KeyCreator;
import org.jxstar.util.log.Log;
import org.jxstar.util.resource.JsParam;

/**
 * 代理工作处理类。
 * 进入代理状态后，会话信息中会多出下面的属性：
 * proxy_user_id、proxy_user_code、proxy_user_name、proxy_id
 *
 * @author TonyTan
 * @version 1.0, 2013-1-4
 */
public class ProxyLogin {
	private static Log _log = Log.getInstance();
	private static BaseDao _dao = BaseDao.getInstance();
	
	/**
	 * 处理代理工作。
	 * @param request
	 * @param response
	 */
	public static void process(HttpServletRequest request, HttpServletResponse response) {
		//是否进入代理工作
		String proxy_in = getParam(request, "proxy_in").toLowerCase();
		if (proxy_in.equals("1") || proxy_in.equals("true")) {
			inProcess(request, response);
		} else {
			outProcess(request, response);
		}
	}
	
	/**
	 * 结束代理工作处理：
	 * 1、先校验会话信息；
	 * 2、获取代理人的用户信息，写入后台会话中；
	 * 3、并记录代理日志；
	 * 4、并修改前台JS中的会话信息。
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	public static void outProcess(HttpServletRequest request, HttpServletResponse response) {
		//1、先校验会话信息
		Map<String,String> mpUser = (Map<String,String>) request.getSession().
									getAttribute(JsParam.CURRUSER);
		if (mpUser == null || mpUser.isEmpty()) {
			String text = reponseText(false, "当前会话信息无效！", "");
			reponseOut(response, text);
			return;
		}
		
		//2、获取原用户信息，写入后台会话中
		String proxy_userid = getParam(request, "proxy_userid");//原用户
		_log.showDebug(".........proxy param: proxy_userid="+proxy_userid);
		Map<String, String> mpOldUser = SysUserUtil.getUserById(proxy_userid);
		if (mpOldUser.isEmpty()) {
			String text = reponseText(false, "原用户信息为空！", "");
			reponseOut(response, text);
			return;
		}
		String proxyId = getParam(request, "proxy_id");
		if (proxyId.length() == 0) {
			String text = reponseText(false, "没有找到原代理设置信息！", "");
			reponseOut(response, text);
			return;
		}
		
		String roleId = SysUserUtil.getRoleID(proxy_userid);
		if (roleId.length() == 0) {
			String text = reponseText(false, "当前用户角色为空！", "");
			reponseOut(response, text);
			return;
		}
		//保存角色ID
		mpOldUser.put("role_id", roleId);
		mpOldUser.put("project_path", SystemVar.REALPATH);
		//添加组织机构信息
		mpUser = OneLoginUtil.addOrgData(mpUser);
		//代理用户信息扩展类
		String processClass = SystemVar.getValue("one.proxyprocess.class");
		if (processClass.length() > 0) {
			ProxyLoginProcess process = (ProxyLoginProcess) SystemFactory.createObject(processClass);
			process.getUserMap(mpOldUser);
		}
		request.getSession().setAttribute(JsParam.CURRUSER, mpOldUser);
		
		//3、并记录代理日志
		writeLog(proxyId, "退出代理工作。");
		
		//4、并修改前台JS中的会话信息
		String json = MapUtil.toJson(mpOldUser);
		String text = reponseText(true, "成功退出代理", json);
		_log.showDebug(".........out proxy user json:" + text);
		reponseOut(response, text);
	}
	
	/**
	 * 开始代理工作处理：
	 * 1、先校验会话信息；
	 * 2、获取被代理人的用户信息，写入后台会话中；
	 * 3、在用户信息增加原用户信息：proxy_user_id、proxy_user_code、proxy_user_name、proxy_id属性；
	 * 4、并记录代理日志；
	 * 5、并修改前台JS中的会话信息。
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	public static void inProcess(HttpServletRequest request, HttpServletResponse response) {
		//1、先校验会话信息
		Map<String,String> mpUser = (Map<String,String>) request.getSession().
									getAttribute(JsParam.CURRUSER);
		if (mpUser == null || mpUser.isEmpty()) {
			String text = reponseText(false, "当前会话信息无效！", "");
			reponseOut(response, text);
			return;
		}
		
		//2、获取被代理人的用户信息，写入后台会话中
		String to_userid = getParam(request, "to_userid");//被代理人
		Map<String, String> mpToUser = SysUserUtil.getUserById(to_userid);
		String proxy_userid = getParam(request, "proxy_userid");//当前用户
		_log.showDebug(".........proxy param: to_userid="+to_userid+"&proxy_userid="+proxy_userid+"&mpuser_size="+mpToUser.size());
		
		if (to_userid.length() == 0 || proxy_userid.length() == 0
				|| mpToUser.isEmpty()) {
			String text = reponseText(false, "当前用户与被代理人信息为空！", "");
			reponseOut(response, text);
			return;
		}
		String proxyId = checkValid(to_userid, proxy_userid);
		if (proxyId.length() == 0) {
			String text = reponseText(false, "没有找到有效的代理设置信息！", "");
			reponseOut(response, text);
			return;
		}
		
		String roleId = SysUserUtil.getRoleID(to_userid);
		if (roleId.length() == 0) {
			String text = reponseText(false, "被代理人的角色为空！", "");
			reponseOut(response, text);
			return;
		}
		//保存角色ID
		mpToUser.put("role_id", roleId);
		mpToUser.put("project_path", SystemVar.REALPATH);
		//添加组织机构信息
		mpUser = OneLoginUtil.addOrgData(mpUser);
		
		//3、在用户信息增加原用户信息：proxy_user_id、proxy_user_code、proxy_user_name、proxy_id属性
		Map<String, String> mpProxyUser = SysUserUtil.getUserById(proxy_userid);
		mpToUser.put("proxy_user_id", MapUtil.getValue(mpProxyUser, "user_id"));
		mpToUser.put("proxy_user_code", MapUtil.getValue(mpProxyUser, "user_code"));
		mpToUser.put("proxy_user_name", MapUtil.getValue(mpProxyUser, "user_name"));
		mpToUser.put("proxy_id", proxyId);
		
		//代理用户信息扩展类
		String processClass = SystemVar.getValue("one.proxyprocess.class");
		if (processClass.length() > 0) {
			ProxyLoginProcess process = (ProxyLoginProcess) SystemFactory.createObject(processClass);
			process.getUserMap(mpToUser);
		}
		request.getSession().setAttribute(JsParam.CURRUSER, mpToUser);
		
		//4、并记录代理日志
		writeLog(proxyId, "开始代理工作。");
		
		//5、并修改前台JS中的会话信息
		String json = MapUtil.toJson(mpToUser);
		String text = reponseText(true, "代理成功", json);
		_log.showDebug(".........proxy user json:" + text);
		reponseOut(response, text);
	}
	
	//检查是否符合代理条件，返回代理设置ID
	private static String checkValid(String to_userid, String proxy_userid) {
		String sql = "select proxy_id from sys_proxy where to_user_id = ? and user_id = ? and auditing = '1' and end_date > ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(to_userid);
		param.addStringValue(proxy_userid);
		//取当前日期-1天
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		String date = DateUtil.calendarToDate(calendar);
		_log.showDebug(".........today-1=" + date);
		param.addDateValue(date);
		
		Map<String,String> mp = _dao.queryMap(param);
		return MapUtil.getValue(mp, "proxy_id");
	}
	
	//记录代理日志
	private static boolean writeLog(String proxyId, String message) {
		String log_id = KeyCreator.getInstance().createKey("sys_proxy_log");
		
		String sql = "insert into sys_proxy_log(log_date, user_id, user_code, user_name, " +
			"to_user_id, to_user_code, to_user_name, proxy_id, log_id, log_memo) " + 
			"select ?, user_id, user_code, user_name, to_user_id, to_user_code, " +
			"to_user_name, proxy_id, ?, ? from sys_proxy where proxy_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addDateValue(DateUtil.getTodaySec());
		param.addStringValue(log_id);
		param.addStringValue(message);
		param.addStringValue(proxyId);
		
		return _dao.update(param);
	}
	
	//返回文本格式的数据
	private static String reponseText(boolean result, String message, String data) {
		if (data == null || data.length() == 0) { 
			data = "{}";
		}
		StringBuilder sbres = new StringBuilder();
		sbres.append("{success:" + result + ", ");
		sbres.append("message:'" + message + "', ");
		sbres.append("data:" + data);
		sbres.append("}");
		
		return sbres.toString();
	}
	
	//响应输出结果
	private static void reponseOut(HttpServletResponse response, String text) {
		try {
			response.getWriter().write(text);
		} catch (Exception e) {
			_log.showError(e);
		}
	}
	
	//获取请求参数
	private static String getParam(HttpServletRequest request, String name) {
		String value = request.getParameter(name);
		if (value == null) value = "";
		return value;
	}
}
