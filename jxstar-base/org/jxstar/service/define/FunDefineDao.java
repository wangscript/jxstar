/*
 * FunDefineDao.java 2011-3-1
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.define;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.DmDaoUtil;

/**
 * 功能定义信息DAO对象。
 *
 * @author TonyTan
 * @version 1.0, 2011-3-1
 */
public class FunDefineDao {
	private static BaseDao _dao = BaseDao.getInstance();
	
	//取功能定义相关表的所有字段
	private static String _field_fun = DmDaoUtil.getFieldSql("fun_base");
	private static String _field_col = DmDaoUtil.getFieldSql("fun_col");
	private static String _field_colext = DmDaoUtil.getFieldSql("fun_colext");
	private static String _field_event = DmDaoUtil.getFieldSql("fun_event");
	
	//查询时间的SQL
	private static String _event_sql = "";
	
	/**
	 * 查询所有功能的信息
	 * @return
	 */
	public static List<Map<String,String>> queryFun() {
		String sql = "select "+ _field_fun +" from fun_base where reg_type <> 'nouse' and fun_state < '9'";
		DaoParam param = _dao.createParam(sql);
		
		return _dao.query(param);
	}

	/**
	 * 查询一个功能的信息
	 * @param funId -- 功能ID
	 * @return
	 */
	public static Map<String,String> queryFun(String funId) {
		String sql = "select "+ _field_fun +" from fun_base where fun_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		
		return _dao.queryMap(param);
	}
	
	/**
	 * 查询一个功能的字段信息列表
	 * @param funId -- 功能ID
	 * @return
	 */
	public static List<Map<String,String>> queryCol(String funId) {
		String sql = "select "+ _field_col +" from fun_col where fun_id = ? order by col_index";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		
		return _dao.query(param);
	}
	
	/**
	 * 查询字段列定义对象。
	 * @param funId -- 功能ID
	 * @return
	 */
	public static ColumnDefine queryColDefine(String funId) {
		return new ColumnDefine(funId);
	}
	
	/**
	 * 查询一个功能的一个字段的信息
	 * @param funId -- 功能ID
	 * @param colCode -- 字段名
	 * @return
	 */
	public static Map<String,String> queryCol(String funId, String colCode) {
		String sql = "select "+ _field_col +" from fun_col where fun_id = ? and col_code = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		param.addStringValue(colCode);
		
		return _dao.queryMap(param);
	}
	
	/**
	 * 查询字段扩展定义信息
	 * @param colId -- 字段ID
	 * @return
	 */
	public static Map<String,String> queryColExt(String colId) {
		String sql = "select "+ _field_colext +" from fun_colext where col_id = ?";
	
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(colId);
		return _dao.queryMap(param);
	}
	
	/**
	 * 查询要控制重复值字段
	 * @param funId - 功能ID
	 * @return List
	 */
	public static List<Map<String,String>> queryRepeatCol(String funId) {
		StringBuilder sbsql = new StringBuilder("select fun_col.col_code, fun_col.col_name ");
			sbsql.append("from fun_col, fun_colext where ");
			sbsql.append("fun_col.col_id = fun_colext.col_id and ");
			sbsql.append("fun_colext.is_repeatval = '1' and fun_col.fun_id = ?");
		
		DaoParam param = _dao.createParam(sbsql.toString());
		param.addStringValue(funId);
		return _dao.query(param);
	}
	
	/**
	 * 查询统计字段定义信息
	 * @param funId - 功能ID
	 * @return List
	 */
	public static List<Map<String,String>> queryStatCol(String funId) {
		StringBuilder sbsql = new StringBuilder("select col_code, stat_tables, ");
			sbsql.append("stat_col, stat_fkcol, stat_where ");
			sbsql.append("from fun_col, fun_colext where ");
			sbsql.append("fun_col.col_id = fun_colext.col_id and ");
			sbsql.append("stat_col > ' ' and stat_tables > ' ' and stat_fkcol > ' ' and fun_id = ?");
		
		DaoParam param = _dao.createParam(sbsql.toString());
		param.addStringValue(funId);
		return _dao.query(param);
	}
	
	/**
	 * 查询指定功能与页面类型的事件信息
	 * @param funId - 功能ID
	 * @param pageType - 页面类型
	 * @return List
	 */
	public static List<Map<String,String>> queryEvent(String funId, String pageType) {
		//取查询事件SQL语句
		if (_event_sql.length() == 0) _event_sql = getEventSql();
		String sql = _event_sql + " and is_hide = '0' and page_type like ? order by event_index";
		
		//查询参数对象
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		param.addStringValue(funId);
		param.addStringValue("%," + pageType + ",%");
		
		return _dao.query(param);
	}
	
	/**
	 * 查询指定功能与事件代码的事件信息
	 * @param funId - 功能ID
	 * @param eventCode - 页面类型
	 * @return Map
	 */
	public static Map<String,String> queryEventMap(String funId, String eventCode) {
		//取查询事件SQL语句
		if (_event_sql.length() == 0) _event_sql = getEventSql();
		String sql = _event_sql + " and event_code = ?";
		
		//查询参数对象
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		param.addStringValue(funId);
		param.addStringValue(eventCode);

		return _dao.queryMap(param);
	}
	
	/**
	 * 取查询事件的SQL
	 * @return
	 */
	private static String getEventSql() {
		StringBuilder sbsql = new StringBuilder();
		sbsql.append("select "+ _field_event +" from fun_event where ");

		StringBuilder sbevent = new StringBuilder();
		sbevent.append("exists (select * from fun_event where ");
		sbevent.append("funall_domain.domain_code = fun_event.event_code and ");
		sbevent.append("is_domain = '1' and fun_id = ?)");
		
		StringBuilder sbdomain = new StringBuilder();
		sbdomain.append("exists (select * from funall_domain, funall_domain_event where ");
		sbdomain.append("funall_domain.domain_id = funall_domain_event.domain_id and ");
		sbdomain.append("funall_domain_event.event_id = fun_event.event_id and ");
		sbdomain.append(sbevent + ")");
		
		//如果定义了事件，则直接取事件，如果包含事件域则连域中的事件一起取到
		sbsql.append(" ((is_domain = '0' and fun_id = ? ) or (" + sbdomain + "))");
		
		return sbsql.toString();
	}
}
