/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.control;

import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.util.ClusterUtil;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.resource.JsMessage;
import org.jxstar.util.resource.JsParam;

/**
 * 服务控制器的锁，在执行同一个功能中的同一条数据时，只能有一个操作在执行；
 * 在sys_doing表中注册当前正在执行的操作；
 * 如果是同一个功能的同一条数据在做不同的操作，暂时不做控制，担心影响正常业务操作；
 * sys_doing没有主键字段，为了提高效率，主键字段没什么意义，操作太频繁，避免主键重复；
 * 
 * 使用方法：
 * 在服务控制器ServiceControllerImp中使用：checkDoing、delDoing；
 * 其它方法方便将来扩展BO中自定义调用；
 * 事件注册与删除的方法不放在公共事务中，因为没提交之前其它用不能看到此记录；
 *
 * @author TonyTan
 * @version 1.0, 2012-8-17
 */
public class ControlerLock {
	private static BaseDao _dao = BaseDao.getInstance();
	//另存当前主键值的标识
	private static final String LOCK_KEYID = "jxstar_lock_keyid";
	
	/**
	 * 检查当前正在执行的操作，如果存在就退出，并且注册当前操作
	 * @param request
	 * @return
	 */
	public static boolean checkDoing(RequestContext request) {		
		//如果是不需锁定的事件，则不处理
		if (!isCheckEvent(request)) return false;
		
		//另存当前主键值，防止在业务方法中覆盖，如GridSaveEvent.gridSave就会覆盖
		String[] keyIds = request.getRequestValues(JsParam.KEYID);
		request.setRequestValue(LOCK_KEYID, keyIds);
		
		//其它用户正在对当前选择的记录执行操作，请稍后刷新数据，再次选择执行！
		if (isDoing(request)) {
			request.setMessage(JsMessage.getValue("functionbm.checkdoing"));
			return true;
		}
		
		//注册当前操作
		regDoing(request);
		
		return false;
	}
	
	/**
	 * 删除此次事件的注册操作
	 * @param request
	 */
	public static void delDoing(RequestContext request) {
		//如果是不需锁定的事件，则不处理
		if (!isCheckEvent(request)) return;
		
		String funId = request.getFunID();
		String[] keyIds = request.getRequestValues(LOCK_KEYID);
		if (keyIds == null || keyIds.length == 0) return;
		
		for (String keyId : keyIds) {
			delDoing(funId, keyId);
		}
	}
	
	/**
	 * 是否是需要锁定的事件
	 * @param request
	 * @return
	 */
	private static boolean isCheckEvent(RequestContext request) {
		String eventCode = request.getEventCode();
		String lockEvent = SystemVar.getValue("sys.lock.eventcode");
		
		StringBuilder sbcode = new StringBuilder(",");
		sbcode.append(eventCode).append(",");
		if (lockEvent.indexOf(sbcode.toString()) >= 0) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 是否有操作正在执行
	 * @param request
	 * @return
	 */
	private static boolean isDoing(RequestContext request) {
		String funId = request.getFunID();
		String[] keyIds = request.getRequestValues(LOCK_KEYID);
		if (keyIds == null || keyIds.length == 0) return false;
		
		for (String keyId : keyIds) {
			if (isDoing(funId, keyId)) return true;
		}
		
		return false;
	}
	
	/**
	 * 是否有操作正在执行，设定范围内的事件都不能执行，去掉了event_code的判断
	 * @param funId
	 * @param keyId
	 * @return
	 */
	private static boolean isDoing(String funId, String keyId) {
		if (keyId == null || keyId.length() == 0) return false;
		
		String sql = "select count(*) as cnt from sys_doing where fun_id = ? and key_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		param.addStringValue(keyId);
		
		Map<String,String> mp = _dao.queryMap(param);
		return MapUtil.hasRecord(mp);
	}
	
	/**
	 * 注册一个用户操作
	 * @param request
	 */
	private static void regDoing(RequestContext request) {
		String funId = request.getFunID();
		String eventCode = request.getEventCode();
		String pageType = request.getPageType();
		
		String[] keyIds = request.getRequestValues(LOCK_KEYID);
		if (keyIds == null || keyIds.length == 0) return;
		
		String userId = "";
		Map<String, String> mpUser = request.getUserInfo();
		if (mpUser != null && !mpUser.isEmpty()) {
			userId = mpUser.get("user_id");
		}
		
		for (String keyId : keyIds) {
			regDoing(funId, keyId, eventCode, pageType, userId);
		}
	}
	
	/**
	 * 注册一个用户操作
	 * @param funId
	 * @param keyId
	 * @param eventCode
	 * @param pageType
	 * @param userId
	 */
	private static void regDoing(String funId, String keyId, 
			String eventCode, String pageType, String userId) {
		if (funId == null || funId.length() == 0) return;
		if (keyId == null || keyId.length() == 0) return;
		
		String sql = "insert into sys_doing(fun_id, key_id, event_code, page_type, add_date, add_userid, server_name) " +
				"values(?, ?, ?, ?, ?, ?, ?)";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		param.addStringValue(keyId);
		param.addStringValue(eventCode);
		param.addStringValue(pageType);
		param.addDateValue(DateUtil.getTodaySec());
		param.addStringValue(userId);
		param.addStringValue(ClusterUtil.getServerName());
		
		_dao.update(param);
	}
	
	/**
	 * 删除此次事件的注册操作
	 * @param funId
	 * @param keyId
	 */
	private static void delDoing(String funId, String keyId) {
		if (keyId == null || keyId.length() == 0) return;
		
		String sql = "delete from sys_doing where fun_id = ? and key_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		param.addStringValue(keyId);
		
		_dao.update(param);
	}
}
