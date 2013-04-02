/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.note.my;

import org.jxstar.service.note.SenderI;

/**
 * JXstar自带的短信发送接口。
 *
 * @author TonyTan
 * @version 1.0, 2012-5-24
 */
public class MySender implements SenderI {
	private NoteSender _sender = null;

	public void init() {
		String name = NoteProperty.getUserName();
		String pwd = NoteProperty.getUserPwd();
		_sender = new NoteSender(name, pwd);
	}

	public String massSend(String dst, String msg, String time, String subNo,
			String txt) {
		if (_sender == null) init();
		
		String ret = _sender.massSend(dst, msg, time, subNo, txt);
		int num = NoteBackParser.getSendNum(ret);
		String bret = "0";
		if (num > 0) {
			bret = "1";
		}
		return "ret=" + bret + "&info=" + ret;
	}

	public String massSend(String dst, String msg) {
		return massSend(dst, msg, "", "", "");
	}

	public String readSms() {
		if (_sender == null) init();
		return _sender.readSms();
	}

}
