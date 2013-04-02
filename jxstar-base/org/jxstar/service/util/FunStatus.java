/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.util;

import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.define.DefineName;
import org.jxstar.util.MapUtil;

/**
 * 取功能业务状态值设置信息的工具类。
 *
 * @author TonyTan
 * @version 1.0, 2012-9-12
 */
public class FunStatus {
	private static BaseDao _dao = BaseDao.getInstance();

	/**
	 * 取功能状态信息
	 * @param funId
	 * @return
	 */
	public static Map<String,String> getStatus(String funId) {
		String sql = "select audit0, audit1, audit2, audit3, audit4, audit_b, audit_e from fun_status where fun_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.setDsName(DefineName.DESIGN_NAME);
		param.addStringValue(funId);
		
		return _dao.queryMap(param);
	}
	
	/**
	 * 取功能状态值
	 * @param funId -- 功能ID
	 * @param name -- 状态值名
	 * @param def -- 缺省值
	 * @return
	 */
	public static String getValue(String funId, String name, String def) {
		Map<String,String> mpData = getStatus(funId);
		if (mpData.isEmpty()) return def;
		
		return MapUtil.getValue(mpData, name, def);
	}
	
	/**
	 * 取记录有效值，根据标志1|3取真实状态值
	 * @param funId
	 * @param flag
	 * @return
	 */
	public static String getValidStatus(String funId, String flag) {
		Map<String,String> mpData = getStatus(funId);
		if (mpData.isEmpty()) return flag;
		
		if (flag.equals("3")) {
			return MapUtil.getValue(mpData, "audit3", "3");
		}
		
		return MapUtil.getValue(mpData, "audit1", "1");
	}
}
