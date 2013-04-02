/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.note;

/**
 * 短信发送接口
 *
 * @author TonyTan
 * @version 1.0, 2012-5-24
 */
public interface SenderI {
	
	/**
	 * 参数初始化
	 */
	public void init();

	/**
	 * 发送短信
	 * @param dst
	 * @param msg
	 * @param time
	 * @param subNo
	 * @param txt
	 * @return 返回值：ret=1|0&info=xxxxx
	 */
	public String massSend(String dst, String msg, String time, String subNo,
			String txt);
	
	/**
	 * 发送短信
	 * @param dst
	 * @param msg
	 * @return 返回值：ret=1|0&info=xxxxx
	 */
	public String massSend(String dst, String msg);
	
	/**
	 * 读取短信
	 * @return 返回值：ret=1|0&code=xxxxxx&info=xxxxxx
	 */
	public String readSms();
}
