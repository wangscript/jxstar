/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.dao.util;

import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.util.MapUtil;

/**
 * 检查数据库连接是否有效。
 * 数据库没有启动而应用已经启动，或者数据库停了：
 * 防止线程没有关闭、静态缓存没有重新加载等问题，连接池中的连接没法清除，所以建议重启动应用。
 *
 * @author TonyTan
 * @version 1.0, 2012-9-17
 */
public class ConnValid {

	/**
	 * 判断当前连接是否有效
	 * @return
	 */
	public static boolean hasValid() {
		BaseDao dao = BaseDao.getInstance();
		String sql = "select count(*) as cnt from fun_base where fun_id = 'sys_var'";
		DaoParam param = dao.createParam(sql);
		
		Map<String,String> mp = dao.queryMap(param);
		if (mp == null || mp.isEmpty()) return false;
		
		String cnt = MapUtil.getValue(mp, "cnt");
		return cnt.equals("1");
	}
}
