/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.note;

import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.DateUtil;

/**
 * 用户编辑短信发送。
 *
 * @author TonyTan
 * @version 1.0, 2012-5-21
 */
public class UserSendBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	public String sendMsg(String[] noteIds) {
		if (noteIds == null || noteIds.length == 0) {
			setMessage("没有选择需要发送的短信！");
			return _returnFaild;
		}
			
		for (int i = 0, n = noteIds.length; i < n; i++) {
			Map<String,String> mpNote = queryNote(noteIds[i]);
			String mob_code = mpNote.get("mob_code");
			String send_msg = mpNote.get("send_msg");
			
			boolean ret = SenderUtil.massSend(mob_code, send_msg);
			String status = ret? SenderUtil.SEND_SUCCESS : SenderUtil.SEND_FAILD;
			
			updateNote(noteIds[i], status);
		}
		
		return _returnSuccess;
	}
	
	private Map<String,String> queryNote(String noteId) {
		String sql = "select send_msg, mob_code from sys_note where note_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(noteId);
		return _dao.queryMap(param);
	}
	
	private boolean updateNote(String noteId, String status) {
		String sql = "update sys_note set send_date = ?, send_status = ? where note_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addDateValue(DateUtil.getTodaySec());
		param.addStringValue(status);
		param.addStringValue(noteId);
		
		return _dao.update(param);
	}
}
