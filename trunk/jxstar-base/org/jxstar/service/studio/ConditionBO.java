/*
 * QueryDataBO.java 2010-10-30
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.studio;

import java.util.List;
import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.JsonDao;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.util.ServiceUtil;
import org.jxstar.util.ArrayUtil;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.key.KeyCreator;
import org.jxstar.util.resource.JsMessage;

/**
 * 查询条件管理对象。
 *
 * @author TonyTan
 * @version 1.0, 2010-10-30
 */
public class ConditionBO extends BusinessObject {
	private static final long serialVersionUID = 1L;
	
	private KeyCreator _keyCreator = KeyCreator.getInstance();
	
	/**
	 * 保存查询条件
	 * @param requestContext
	 * @param userInfo
	 * @return
	 */
	public String save(RequestContext requestContext, Map<String,String> userInfo) {
		//取查询条件名称
		String sel_funid = requestContext.getRequestValue("selfunid");
		String is_share = requestContext.getRequestValue("is_share");
		String queryid = requestContext.getRequestValue("query_id");
		String query_name = requestContext.getRequestValue("query_name");
		_log.showDebug("=======is_share=" + is_share + ";query_name=" + query_name);
		
		//新增的查询条件
		if (queryid.length() == 0) {
			queryid = saveQuery(sel_funid, is_share, query_name, userInfo);
			if (queryid.length() == 0) {
				//"保存查询条件名称失败！"
				setMessage(JsMessage.getValue("conditionbo.saveerror"));
				return _returnFaild;
			}
		} else {
			//先删除明细，下面会重新保存
			deleteQueryDet(queryid);
		}
		
		//取查询条件明细
		String[] gucols = new String[]{"left_brack", "colcode", "condtype", "cond_value", "right_brack", "andor", "coltype", "col_no"};
		List<Map<String,String>> lsquery = ServiceUtil.getRequestMaps(
				requestContext, gucols);
		
		if (!saveQueryDet(queryid, lsquery)) {
			//"保存查询条件明细失败！"
			setMessage(JsMessage.getValue("conditionbo.savedeterror"));
			return _returnFaild;
		}
		
		//返回新建的查询条件
		setReturnData("{query_id:'"+queryid+"', is_share:'"+is_share+"', query_name:'"+query_name+"'}");
		
		return _returnSuccess;
	}
	
	/**
	 * 保存查询方案
	 * @param requestContext
	 * @param curUserId
	 * @return
	 */
	public String saveCase(RequestContext requestContext, String curUserId) {
		String[] gucols = new String[]{"query_name", "is_share", "query_id"};
		List<Map<String,String>> lsquery = ServiceUtil.getRequestMaps(
				requestContext, gucols);
		
		String sql = "update sys_query set query_name = ?, is_share = ?, modify_userid = ?, modify_date = ? where query_id = ?";
		
		for (Map<String,String> mpQuery : lsquery) {
			DaoParam param = _dao.createParam(sql);
			param.addStringValue(MapUtil.getValue(mpQuery, "query_name"));
			param.addStringValue(MapUtil.getValue(mpQuery, "is_share"));
			param.addStringValue(curUserId);
			param.addDateValue(DateUtil.getTodaySec());
			param.addStringValue(MapUtil.getValue(mpQuery, "query_id"));
			_dao.update(param);
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 删除查询条件
	 * @param keyid
	 * @return
	 */
	public String delete(String keyid) {
		String dsql1 = "delete from sys_query_det where query_id = ?";
		String dsql2 = "delete from sys_query where query_id = ?";
		
		//删除明细表
		DaoParam param1 = _dao.createParam(dsql1);
		param1.addStringValue(keyid);
		boolean bret = _dao.update(param1);
		
		//删除主表
		DaoParam param2 = _dao.createParam(dsql2);
		param2.addStringValue(keyid);
		bret = bret && _dao.update(param2);
		
		if (!bret) {
			//"删除查询条件失败！"
			setMessage(JsMessage.getValue("conditionbo.deleteerror"));
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 查询当前功能的查询条件
	 * @param userid -- 用户ID
	 * @param funid -- 功能ID
	 * @return
	 */
	public String queryData(String userid, String funid) {
		_log.showDebug("=======userid=" + userid + ";funid=" + funid);
		String sql = "select query_id, query_name, user_id, is_share " +
					 "from sys_query where fun_id = ? and (is_share = '1' or (user_id = ? and is_share = '0'))";
		
		DaoParam param = new DaoParam();
		param.setSql(sql);
		param.addStringValue(funid);
		param.addStringValue(userid);
		
		JsonDao jsonDao = JsonDao.getInstance();
		String[] cols = ArrayUtil.getGridCol(sql);
		String json = jsonDao.query(param, cols);
		
		json = "{root:["+json+"]}";
		_log.showDebug("==========json=" + json);
		
		setReturnData(json);
		
		return _returnSuccess;
	}
	
	/**
	 * 查询当前功能的查询条件明细
	 * @param queryid -- 条件ID
	 * @return
	 */
	public String queryCond(String queryid) {
		String sql = "select left_brack, colcode, condtype, cond_value, right_brack, andor, coltype, col_no " +
				 	 "from sys_query_det where query_id = ? order by col_no";
		
		DaoParam param = new DaoParam();
		param.setSql(sql);
		param.addStringValue(queryid);
		
		JsonDao jsonDao = JsonDao.getInstance();
		String[] cols = ArrayUtil.getGridCol(sql);
		String json = jsonDao.query(param, cols);
		
		json = "{root:["+json+"]}";
		_log.showDebug("========json=" + json);
		
		setReturnData(json);
		
		return _returnSuccess;
	}

	/**
	 * 新增查询条件
	 * @param sel_funid
	 * @param is_share
	 * @param query_name
	 * @param userInfo
	 * @return
	 */
	private String saveQuery(String sel_funid, String is_share, String query_name, 
			Map<String,String> userInfo) {
		String keyid = _keyCreator.createKey("sys_query");
		
		String isql = "insert into sys_query(" + 
					  "query_id, query_name, fun_id, user_name, user_id, is_share, add_userid, add_date) " +
					  "values(?, ?, ?, ?, ?, ?, ?, ?)";
		
		DaoParam param = _dao.createParam(isql);
		param.addStringValue(keyid);
		param.addStringValue(query_name);
		param.addStringValue(sel_funid);
		param.addStringValue(userInfo.get("user_name"));
		param.addStringValue(userInfo.get("user_id"));
		param.addStringValue(is_share);
		param.addStringValue(userInfo.get("user_id"));
		param.addDateValue(DateUtil.getTodaySec());
		
		if (_dao.update(param)) return keyid;
		
		return "";
	}
	
	/**
	 * 保存查询条件明细
	 * @param queryid
	 * @param lsquery
	 * @return
	 */
	private boolean saveQueryDet(String queryid, List<Map<String,String>> lsquery) {
		String isql = "insert into sys_query_det(" +
					  "query_detid, query_id, left_brack, colcode, condtype, cond_value, right_brack, andor, coltype, col_no) " +
					  "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		for (int i = 0; i < lsquery.size(); i++) {
			Map<String,String> mp = lsquery.get(i);
			
			//如果没有序号，自动构建序号
			String colno = mp.get("col_no");
			if (colno.length() == 0 || colno.equals("0")) {
				colno = Integer.toString((i+1)*2);
			}
			
			String detid = _keyCreator.createKey("sys_query_det");
			DaoParam param = _dao.createParam(isql);
			param.addStringValue(detid);
			param.addStringValue(queryid);
			param.addStringValue(mp.get("left_brack"));
			param.addStringValue(mp.get("colcode"));
			param.addStringValue(mp.get("condtype"));
			param.addStringValue(mp.get("cond_value"));
			param.addStringValue(mp.get("right_brack"));
			param.addStringValue(mp.get("andor"));
			param.addStringValue(mp.get("coltype"));
			param.addStringValue(colno);
			
			if (!_dao.update(param)) return false;
		}
		
		return true;
	}
	
	/**
	 * 删除查询条件明细
	 * @param queryid
	 * @return
	 */
	private boolean deleteQueryDet(String queryid) {
		String sql = "delete from sys_query_det where query_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(queryid);
		
		return _dao.update(param); 
	}
}
