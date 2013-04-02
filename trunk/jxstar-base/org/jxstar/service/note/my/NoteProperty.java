/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.note.my;

import org.jxstar.util.config.SystemVar;

/**
 * 短信接口配置属性。
 *
 * @author TonyTan
 * @version 1.0, 2012-5-21
 */
public class NoteProperty {
	/*
	#短信服务器设置
	sms.note.charset=gbk
	sms.note.username=
	sms.note.userpwd=
	sms.note.isopen=1
	#客户端代理设置
	sms.proxy.host=
	sms.proxy.port=
	sms.proxy.username=
	sms.proxy.userpwd=
	#短信头设置
	sms.note.header=
	*/
	
	public static boolean getIsOpen() {
		String isOpen = SystemVar.getValue("sms.note.isopen", "0");
		return isOpen.equals("1");
	}
	public static String getMsgHeader() {
		return SystemVar.getValue("sms.note.header");
	}
	public static String getProxyHostIP() {
		return SystemVar.getValue("sms.proxy.host");
	}
	public static int getProxyPortCode() {
		String code = SystemVar.getValue("sms.proxy.port");
		return Integer.parseInt(code);
	}
	public static String getProxyUserName() {
		return SystemVar.getValue("sms.proxy.username");
	}
	public static String getProxyUserPwd() {
		return SystemVar.getValue("sms.proxy.userpwd");
	}
	public static String getCharSet() {
		return SystemVar.getValue("sms.note.charset", "gbk");
	}
	public static String getUserName() {
		return SystemVar.getValue("sms.note.username");
	}
	public static String getUserPwd() {
		return SystemVar.getValue("sms.note.userpwd");
	}
}
