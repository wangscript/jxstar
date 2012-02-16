/*
 * PortletMsgBO.java 2011-1-9
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.portlet;


import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.DmDao;
import org.jxstar.service.BoException;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.event.CreateEvent;
import org.jxstar.util.DateUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;
import org.jxstar.util.resource.JsParam;

/**
 * 首页公告处理类。
 *
 * @author TonyTan
 * @version 1.0, 2011-1-9
 */
public class PortletBoardBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 批量发布公告。
	 * @param keyIds -- 消息数组ID
	 * @return
	 */
	public String gridSendMsg(String[] keyIds) {
		if (keyIds == null || keyIds.length == 0) {
			//"发送消息ID为空！"
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
	 * 发布公告，如果有消息ID说明是先保存了再发送的，如果没有消息ID则需要先保存再发送。
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
	 * 生成公告阅读记录
	 * @param msgId -- 消息ID
	 * @param userId -- 用户ID
	 * @param userName -- 用户名
	 * @return
	 */
	public String readBoard(String msgId, String userId, String userName) {
		Map<String,String> mpRead = FactoryUtil.newMap();
		mpRead.put("msg_id", msgId);
		mpRead.put("user_id", userId);
		mpRead.put("user_name", userName);
		mpRead.put("read_date", DateUtil.getTodaySec());
		
		String readId = DmDao.insert("plet_read", mpRead);
		if (readId == null || readId.length() == 0) {
			//"生成公告阅读记录失败！"
			setMessage(JsMessage.getValue("portlet.createboarderror"));
			return _returnFaild;
		}
		return _returnSuccess;
	}
	
	/**
	 * 删除公告消息时，同时删除所有的阅读记录
	 * @param msgId -- 消息ID
	 * @return
	 */
	public String deleteBoard(String msgId) {
		String sql = "delete from plet_read where msg_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(msgId);
		_dao.update(param);
		
		return _returnSuccess;
	}
	
	/**
	 * 修改公告状态为“新”
	 * @param msgId
	 * @return
	 */
	private void sendMsg(String msgId) throws BoException {
		String usql = "update plet_msg set msg_state = '1', send_date = ? where msg_id = ?";
		DaoParam param = _dao.createParam(usql);
		param.addDateValue(DateUtil.getTodaySec());
		param.addStringValue(msgId);
		boolean bu = _dao.update(param);
		if (!bu) {
			//"发布公告失败：修改公告状态为“新”失败！"
			throw new BoException(JsMessage.getValue("portlet.boarderror"));
		}
	}
}
