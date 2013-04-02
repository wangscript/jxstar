/*
 * PortletMsgBO.java 2011-1-9
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.portlet;

import java.util.List;
import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.JsonDao;
import org.jxstar.service.BoException;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.event.CreateEvent;
import org.jxstar.util.ArrayUtil;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;
import org.jxstar.util.resource.JsParam;

/**
 * 首页消息处理类。
 *
 * @author TonyTan
 * @version 1.0, 2011-1-9
 */
public class PortletMsgBO extends BusinessObject {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 查找新消息返回到前台
	 * @param userId -- 用户ID
	 * @return
	 */
	public String queryNewMsg(String userId) {
		StringBuilder sbsql = new StringBuilder();
		String selectsql = "select msg_title, content, msg_type, dept_id, msg_id from plet_msg ";
		
		sbsql.append(selectsql);
		sbsql.append(" where ");
		sbsql.append("(plet_msg.isto = '1' and plet_msg.to_userid = ? and plet_msg.msg_state = '1') or ");
		sbsql.append("(plet_msg.msg_type = 'gg' and not exists ");
		sbsql.append("(select * from plet_read where plet_read.msg_id = plet_msg.msg_id and plet_read.user_id = ?)) ");
		sbsql.append("order by plet_msg.send_date desc");

		DaoParam param = _dao.createParam(sbsql.toString());
		param.addStringValue(userId);
		param.addStringValue(userId);
		
		//取查询字段名
		String[] cols = ArrayUtil.getGridCol(selectsql);
		
		//查询页面数据
		JsonDao jsonDao = JsonDao.getInstance();
		String strJson = jsonDao.query(param, cols);
		
		//查询SQL异常
		if (strJson == null) {
			_log.showWarn("simplequery error!");
			setMessage(JsMessage.getValue("web.query.error"));
			return _returnFaild;
		}
		
		StringBuilder sbJson = new StringBuilder("{root:[").append(strJson + "]}");
		//_log.showDebug("json=" + sbJson.toString());
		
		//返回查询数据
		setReturnData(sbJson.toString());
		
		return _returnSuccess;
	}
	
	/**
	 * 批量已读消息
	 * @param keyIds
	 * @return
	 */
	public String updateMsgState(String[] keyIds) {
		if (keyIds == null || keyIds.length == 0) {
			setMessage(JsMessage.getValue("portlet.msgidisnull"));
			return _returnFaild;
		}
		
		for (int i = 0, n = keyIds.length; i < n; i++) {
			String bret = updateMsgState(keyIds[i]);
			if (bret.equals(_returnFaild)) return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 修改消息状态为已读
	 * @param msgId -- 消息ID，个人发送的消息ID最后一位是“-”字符
	 * @return
	 */
	public String updateMsgState(String msgId) {
		if (!updateState(msgId)) {
			//"修改消息状态失败！"
			setMessage(JsMessage.getValue("portlet.msgstateerror"));
			return _returnFaild;
		}
		
		//如果是个人发送的消息
		int index = msgId.indexOf("-");
		if (index >= 0) {
			String formId = msgId.substring(0, index);
			if (!updateState(formId)) {
				setMessage(JsMessage.getValue("portlet.msgstateerror"));
				return _returnFaild;
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 批量发送消息。
	 * @param keyIds -- 消息数组ID
	 * @return
	 */
	public String gridSendMsg(String[] keyIds) {
		if (keyIds == null || keyIds.length == 0) {
			setMessage(JsMessage.getValue("portlet.msgidisnull"));
			return _returnFaild;
		}
		
		//发送消息
		try {
			for (int i = 0, n = keyIds.length; i < n; i++) {
				sendMsg(keyIds[i]);
			}
		} catch (BoException e) {
			_log.showError(e);
			setMessage(e.getMessage());
			return _returnFaild;
		}
		
		return _returnSuccess;
	}

	/**
	 * 发送消息，如果有消息ID说明是先保存了再发送的，如果没有消息ID则需要先保存再发送。
	 * @param requestContext
	 * @return
	 */
	public String formSendMsg(RequestContext requestContext) {
		String sKeyID = requestContext.getRequestValue(JsParam.KEYID);
		
		//消息ID为空，先保存再发送
		if (sKeyID == null || sKeyID.length() == 0) {
			CreateEvent ce = new CreateEvent();
			String bret = ce.create(requestContext);
			if (bret.equals(_returnFaild)) return _returnFaild;
			
			sKeyID = requestContext.getRequestValue(JsParam.KEYID);
		}
		
		//发送消息
		try {
			sendMsg(sKeyID);
		} catch (BoException e) {
			_log.showError(e);
			setMessage(e.getMessage());
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 发送消息：修改消息的状态为“新”；复制一条新记录为发送的消息记录，用于收件处理，消息ID加后缀-；
	 * @param msgId
	 * @return
	 */
	public void sendMsg(String msgId) throws BoException {
		//取消息记录值
		Map<String,String> mpMsg = queryMsg(msgId);
		if (mpMsg.isEmpty()) {
			//"发送消息失败：来源消息为空！"
			throw new BoException(JsMessage.getValue("portlet.scrmsgnull"));
		}
		String msgState = mpMsg.get("msg_state");
		if (!msgState.equals("0")) {
			//"发送消息失败：消息的状态不为“草稿”！"
			throw new BoException(JsMessage.getValue("portlet.msginiterror"));
		}
		
		List<Map<String,String>> lsUser = queryUser(msgId);
		//添加表单中的接收人
		putUser(lsUser, mpMsg);
		
		if (lsUser.isEmpty()) {
			throw new BoException("没有选择消息接收人！");
		}
		
		//更新消息的状态
		String usql = "update plet_msg set msg_state = '1' where msg_id = ?";
		DaoParam param = _dao.createParam(usql);
		param.addStringValue(msgId);
		boolean bu = _dao.update(param);
		if (!bu) {
			//"发送消息失败：修改消息状态为“新”失败！"
			throw new BoException(JsMessage.getValue("portlet.msgnewerror"));
		}
		
		//复制消息记录
		String isql = "insert into plet_msg(msg_id, content, from_userid, from_user, " +
					  "to_userid, to_user, send_date, msg_state, msg_type, isto, add_userid, add_date) " +
					  "values (?, ?, ?, ?,  ?, ?, ?, ?, ?, '1', ?, ?)";
		
		for (int i = 0; i < lsUser.size(); i++) {
			 Map<String,String> mpUser = lsUser.get(i);
			 
			DaoParam iparam = _dao.createParam(isql);
			iparam.addStringValue(msgId+'-'+i);
			iparam.addStringValue(mpMsg.get("content"));
			iparam.addStringValue(mpMsg.get("from_userid"));
			iparam.addStringValue(mpMsg.get("from_user"));
			iparam.addStringValue(mpUser.get("user_id"));
			iparam.addStringValue(mpUser.get("user_name"));
			iparam.addDateValue(mpMsg.get("send_date"));
			iparam.addStringValue("1");//msg_state 消息状态为“新”
			iparam.addStringValue(mpMsg.get("msg_type"));
			iparam.addStringValue(mpMsg.get("add_userid"));
			iparam.addDateValue(mpMsg.get("add_date"));
			
			bu = _dao.update(iparam);
			if (!bu) {
				//"发送消息失败：复制消息记录失败！"
				throw new BoException(JsMessage.getValue("portlet.msgcopyerror"));
			}
		}
	}
	
	/**
	 * 修改消息状态为已读
	 * @param msgId
	 * @return
	 */
	private boolean updateState(String msgId) {
		String sql = "update plet_msg set msg_state = '3', read_date = ? where msg_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addDateValue(DateUtil.getTodaySec());
		param.addStringValue(msgId);
		
		return _dao.update(param);
	}
	
	/**
	 * 查询消息记录值
	 * @param msgId -- 消息ID
	 * @return
	 */
	private Map<String,String> queryMsg(String msgId) {
		String sql = "select msg_id, content, from_userid, from_user, " + 
					 "to_userid, to_user, send_date, msg_state, msg_type, add_userid, add_date " + 
					 "from plet_msg where msg_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(msgId);
		
		return _dao.queryMap(param);
	}
	
	/**
	 * 取多个分配人
	 * @param msgId
	 * @return
	 */
	private List<Map<String,String>> queryUser(String msgId) {
		String sql = "select user_id, user_name from plet_msg_user where msg_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(msgId);
		
		return _dao.query(param);
	}
	
	/**
	 * 如果表单中的接收人没有重复，则也发送消息
	 * @param lsUser
	 * @param mpUser
	 */
	private void putUser(List<Map<String,String>> lsUser, Map<String,String> mpUser) {
		String userId = MapUtil.getValue(mpUser, "to_userid");
		if (userId.length() == 0) return;
		
		for (Map<String,String> myUser:lsUser) {
			String myId = myUser.get("user_id");
			if (myId.equals(userId)) return;
		}
		
		String userName = MapUtil.getValue(mpUser, "to_user");
		Map<String,String> mp = FactoryUtil.newMap();
		mp.put("user_id", userId);
		mp.put("user_name", userName);
		lsUser.add(mp);
	}
}
