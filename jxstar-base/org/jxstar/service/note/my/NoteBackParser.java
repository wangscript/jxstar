/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.note.my;

/**
 * 短信发送后反馈结果信息解析类。
 * 返回结果信息示例：num=2&success=1393710***4,1393710***5&faile=&err=发送成功&errid=0
 *
 * @author TonyTan
 * @version 1.0, 2012-5-21
 */
public class NoteBackParser {
	public static String BACK_NUM = "num=";			//发送短信的条数
	public static String BACK_SUCC = "success=";	//发送成功的手机号
	public static String BACK_FAILE = "faile=";		//发送失败的手机号
	private static String BACK_ERR = "err=";		//发送失败的消息
	private static String BACK_ERRID = "errid=";	//发送失败的错误号
	
	/**
	 * 返回成功发送短信的条数
	 * @param retInfo
	 * @return
	 */
	public static int getSendNum(String retInfo) {
		if (retInfo == null || retInfo.length() == 0) {
			return 0;
		}
		
		int iStart = retInfo.indexOf(BACK_NUM)+BACK_NUM.length();
		int iEnd = retInfo.indexOf("&");
		String num = retInfo.substring(iStart, iEnd);
		return Integer.parseInt(num);
	}

	/**
	 * 返回发送失败的手机号
	 * @return
	 */
	public static String getFaileCode(String retInfo) {
		if (retInfo == null || retInfo.length() == 0) {
			return "";
		}
		
		int iStart = retInfo.indexOf(BACK_FAILE)+BACK_FAILE.length();
		int iEnd = retInfo.indexOf(BACK_ERR)-1;
		return retInfo.substring(iStart, iEnd);
	}
	
	/**
	 * 返回err值
	 * @return
	 */
	public static String getError(String retInfo) {
		if (retInfo == null || retInfo.length() == 0) {
			return "";
		}
		
		int iStart = retInfo.indexOf(BACK_ERR)+BACK_ERR.length();
		int iEnd = retInfo.indexOf(BACK_ERRID)-1;
		
		return retInfo.substring(iStart, iEnd);
	}
}
