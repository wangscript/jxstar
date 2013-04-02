/*
 * Copyright(c) 2013 Donghong Inc.
 */
package org.jxstar.control.login;

import java.util.Map;

/**
 * 代理用户信息处理扩展类。
 *
 * @author TonyTan
 * @version 1.0, 2013-1-4
 */
public abstract class ProxyLoginProcess {

	/**
	 * 给原有的用户信息添加更多的用户信息
	 * @param mpUser -- 包含有user_id, user_code, user_name, dept_id, dept_code, dept_name, role_id信息。
	 * @return
	 */
	protected Map<String,String> getUserMap(Map<String,String> mpUser) {
		return mpUser;
	}
}
