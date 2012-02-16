package org.jxstar.service.studio;

import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.key.KeyCreator;

/**
 * 处理用户登录日志；
 * 此业务类忽略错误信息，防止登录不成功；
 * @author TonyTan
 * @version 1.0, 2011-11-29
 */
public class LoginLogBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 处理用户登录日志信息
	 * @param mpUser -- 当前用户
	 * @return
	 */
	public String login(RequestContext request) {
		Map<String,String> mpUser = request.getUserInfo();
		if (mpUser == null || mpUser.isEmpty()) return _returnSuccess;
		
		//新增登录日志
		String userId = mpUser.get("user_id");
		String logId = insertLog(userId);
		if (logId.length() == 0) return _returnSuccess;
		
		//取会话ID、客户端IP、客户端程序名
		Map<String,String> mpInfo = request.getClientInfo();
		String sessionId = MapUtil.getValue(mpInfo, "session_id");
		String clientIp = MapUtil.getValue(mpInfo, "client_ip");
		String userAgent = MapUtil.getValue(mpInfo, "user-agent");
		if (userAgent.length() > 90) userAgent = userAgent.substring(0, 90);
		
		//新增登录用户
		insertLogin(mpUser, logId, sessionId, clientIp, userAgent);
		
		return _returnSuccess;
	}
	
	/**
	 * 处理用户登出日志信息
	 * @param mpUser -- 当前用户
	 * @return
	 */
	public String logout(RequestContext request) {
		Map<String,String> mpUser = request.getUserInfo();
		if (mpUser == null || mpUser.isEmpty()) return _returnSuccess;
		
		//取当前用户的日志ID
		String userId = mpUser.get("user_id");
		
		//取会话ID
		Map<String,String> mpInfo = request.getClientInfo();
		String sessionId = MapUtil.getValue(mpInfo, "session_id");
		
		//删除当前用户与修改退出日志
		logout(userId, sessionId);
		
		return _returnSuccess;
	}
	
	/**
	 * 删除当前在线用户信息
	 * @param userId
	 * @param sessionId
	 */
	public void logout(String userId, String sessionId) {
		String logId = getLogId(userId);
		
		//删除当前用户与修改退出日志
		deleteLogin(userId, sessionId);
		updateLog(logId);
	}
	
	/**
	 * 在系统重启时删除所有当前登录用户
	 */
	public void delLoginUser() {
		String sql = "delete from sys_user_login";
		DaoParam param = _dao.createParam(sql);
		_dao.update(param);
	}
	
	//新增登录日志
	private String insertLog(String userId) {
		String sql = "insert into sys_log_login(log_id, user_id, login_date) values(?, ?, ?)";
		
		String logId = KeyCreator.getInstance().createKey("sys_log_login");
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(logId);
		param.addStringValue(userId);
		param.addDateValue(DateUtil.getTodaySec());
		
		if (_dao.update(param)){
			return logId;
		}
		return "";
	}
	
	//保存当前登录用户
	private boolean insertLogin(Map<String,String> mpUser, String logId, 
			String sessionId, String clientIp, String userAgent) {
		String userId = mpUser.get("user_id");
		String userCode = mpUser.get("user_code");
		String userName = mpUser.get("user_name");
		String deptId = mpUser.get("dept_id");
		String deptName = mpUser.get("dept_name");
		
		String sql = "insert into sys_user_login(user_id, user_code, user_name, dept_id, dept_name, " +
				"login_date, log_id, login_id, session_id, client_ip, client_agent) " +
				"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		String loginId = KeyCreator.getInstance().createKey("sys_user_login");
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(userId);
		param.addStringValue(userCode);
		param.addStringValue(userName);
		param.addStringValue(deptId);
		param.addStringValue(deptName);
		param.addDateValue(DateUtil.getTodaySec());
		param.addStringValue(logId);
		param.addStringValue(loginId);
		param.addStringValue(sessionId);
		param.addStringValue(clientIp);
		param.addStringValue(userAgent);
		
		return _dao.update(param);
	}
	
	//取当前用户的日志ID
	private String getLogId(String userId) {
		String sql = "select log_id from sys_user_login where user_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(userId);
		
		Map<String,String> mpData = _dao.queryMap(param);
		return MapUtil.getValue(mpData, "log_id");
	}
	
	//删除当前登录用户，同用户都删除掉
	private boolean deleteLogin(String userId, String sessionId) {
		String sql = "delete from sys_user_login where user_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(userId);
		//param.addStringValue(sessionId);
		
		return _dao.update(param);
	}
	
	//修改登录日志中的退出时间
	private boolean updateLog(String logId) {
		String sql = "update sys_log_login set logout_date = ? where log_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addDateValue(DateUtil.getTodaySec());
		param.addStringValue(logId);
		
		return _dao.update(param);
	}
}
