/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.studio;

import org.jxstar.security.LicenseInfo;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.StringUtil;

/**
 * 与许可相关的业务处理类。
 *
 * @author TonyTan
 * @version 1.0, 2012-5-15
 */
public class LicenseBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 读取机器序列号
	 * @return
	 */
	public String readKey() {
		String key = LicenseInfo.readKey();
		if (key == null || key.length() == 0) {
			setMessage("获取的服务器序列号为空！");
			return _returnFaild;
		}
		
		setReturnData("{key:'"+ key +"'}");
		
		return _returnSuccess;
	}
	
	/**
	 * 读取许可信息
	 * @return
	 */
	public String readInfo() {
		String str = LicenseInfo.readInfo();
		setReturnData("{info:'"+ StringUtil.strForJson(str) +"'}");
		
		return _returnSuccess;
	}
}
