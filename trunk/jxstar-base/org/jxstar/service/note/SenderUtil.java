/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.note;

import java.util.Map;

import org.jxstar.dao.DmDao;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.log.Log;

/**
 * 短信发送的工具方法。
 *
 * @author TonyTan
 * @version 1.0, 2012-5-21
 */
public class SenderUtil extends BusinessObject {
	private static final long serialVersionUID = 1L;
	private static SenderI _sender = null;
	
	public static final String SEND_FAILD = "7";	//发送失败
	public static final String SEND_SUCCESS = "3";	//发送成功
	
	/**
	 * 获取短信发送对象
	 * @return
	 */
	public static SenderI getSender() {
		if (_sender == null) {
			String clsName = SystemVar.getValue("sms.note.class", "org.jxstar.service.note.my.MySender");
			_sender = (SenderI) SystemFactory.createObject(clsName);
			_sender.init();
		}
		return _sender;
	}
	
	/**
	 * 发送短信
	 * @param modCode -- 手机号码，可以是多个号码，用,分隔
	 * @param msg -- 短信内容
	 * @return
	 */
	public static boolean massSend(String modCode, String msg) {
		if (modCode == null || modCode.length() == 0) return false;
		if (msg == null || msg.length() == 0) return false;
		
		msg = SystemVar.getValue("sms.note.header") + msg;
		
		String ret = getSender().massSend(modCode, msg);
		boolean bret = ret.charAt(4) == '1';
		if (!bret) {
			Log.getInstance().showWarn(ret);
		}
		return bret;
	}
	
	/**
	 * 保存短信发送记录，数据格式为：
		user_id	接收用户ID	
		user_name	接收用户	
		mob_code	手机号码	
		send_msg	短信内容	
		fun_id	功能ID	
		data_id	数据ID	
		send_src	发送来源	wf 工作流；user 编辑；warn 提醒
		send_srcid	来源记录ID	
		send_user	发送人	
		
		send_date	发送时间	
		send_status	消息发送状态	0 编辑；1 发送中；3 发送成功；7 发送失败
		//note_id	短信ID	

	 * @param mpData
	 * @return
	 */
	public static boolean saveSend(Map<String,String> mpData) {
		mpData.put("send_date", DateUtil.getTodaySec());
		//如果没有设置发送状态，则给状态0
		String status = MapUtil.getValue(mpData, "send_status");
		if (status.length() == 0) {
			mpData.put("send_status", "1");
		}
		
		DmDao.insert("sys_note", mpData);
		
		return true;
	}
	
	/**
	 * 取代理设置对象
	 * @return
	 */
	/*public static Proxy getProxy() {
		String proxyHost = NoteProperty.getProxyHostIP();
		if (proxyHost.length() == 0) return null;
		
		InetSocketAddress addr = new InetSocketAddress(proxyHost, NoteProperty.getProxyPortCode());
		Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
		
		return proxy;
	}*/
	
}
