/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.studio;

import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.dao.DmDao;
import org.jxstar.dao.JsonDao;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.ArrayUtil;
import org.jxstar.util.DateUtil;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 处理查询方案数据。
 *
 * @author TonyTan
 * @version 1.0, 2012-4-17
 */
public class QueryCaseBO extends BusinessObject {
	private static final long serialVersionUID = 1L;
	
	private JsonDao _jsonDao = JsonDao.getInstance();
	
	/**
	 * 自动新增查询方案
	 * @param funId
	 * @return
	 */
	public String createCase(String funId, String userId, String userName) {
		//取新的方案序号
		int caseNo = queryCaseNo(funId)+1;
		//取方案名称
		String caseName = "查询方案" + caseNo;
		
		Map<String,String> mp = FactoryUtil.newMap();
		mp.put("query_name", caseName);
		mp.put("fun_id", funId);
		mp.put("user_name", userName);
		mp.put("user_id", userId);
		mp.put("is_default", "0");
		mp.put("is_share", "1");
		mp.put("query_no", Integer.toString(caseNo));
		mp.put("add_userid", userId);
		mp.put("add_date", DateUtil.getTodaySec());
		
		String qryid = DmDao.insert("sys_query", mp);
		
		setReturnData("{qryid:'"+ qryid +"'}");
		
		return _returnSuccess;
	}
	
	/**
	 * 关闭查询方案界面或打开功能页面时调用
	 * @param funId
	 * @param userId
	 * @return
	 */
	public String reloadQryCase(String funId, String userId) {
		StringBuilder jsonsb = new StringBuilder("{");
		
		//取第一个查询方案的名称
		String queryId = queryFirstId(funId, userId);
		if (queryId.length() == 0) {
			jsonsb.append("qryid:'',");
		} else {
			jsonsb.append("qryid:'"+ queryId +"',");
		}
		
		//取所有查询方案
		String qrycase = queryNameJson(funId, userId);
		if (qrycase.length() == 0) {
			jsonsb.append("qrycase:[],");
		} else {
			jsonsb.append("qrycase:[" + qrycase + "],");
		}
		
		//取第一个查询方案的查询条件
		String qrycond = queryCondJson(queryId);
		if (qrycond.length() == 0) {
			jsonsb.append("qrycond:[]");
		} else {
			jsonsb.append("qrycond:[" + qrycond + "]");
		}
		
		jsonsb.append("}");
		setReturnData(jsonsb.toString());
		
		return _returnSuccess;
	}
	
	/**
	 * 选择指定的查询方案时调用
	 * @param queryId
	 * @return
	 */
	public String selectQryCase(String queryId) {
		StringBuilder jsonsb = new StringBuilder("{");
		
		String qrycond = queryCondJson(queryId);
		if (qrycond.length() == 0) {
			jsonsb.append("qrycond:[],");
		} else {
			jsonsb.append("qrycond:[" + qrycond + "],");
		}
		
		jsonsb.append("qryid:'"+ queryId +"'}");
		setReturnData(jsonsb.toString());
		
		return _returnSuccess;
	}
	
	/**
	 * 取查询方案的JSON
	 * @param funId
	 * @param userId
	 * @return
	 */
	private String queryNameJson(String funId, String userId) {
		String sqlm = "select query_id, query_name from sys_query where fun_id = ? and " +
				"(is_share = '1' or user_id = ?) order by query_no";
		DaoParam param = _dao.createParam(sqlm);
		param.addStringValue(funId);
		param.addStringValue(userId);
		return _jsonDao.query(param, new String[]{"value", "text"});
	}
	
	/**
	 * 取查询条件的JSON
	 * @param queryId
	 * @return
	 */
	private String queryCondJson(String queryId) {
		String[] cols = new String[]{"left_brack", "colcode", "colname", "condtype", 
				"cond_value", "right_brack", "andor", "coltype", "row_no"};
		String sqlm = "select "+ ArrayUtil.arrayToString(cols) +" from sys_query_det " +
				"where query_id = ? order by col_no";
		
		DaoParam param = _dao.createParam(sqlm);
		param.addStringValue(queryId);
		
		return _jsonDao.query(param, cols);
	}
	
	/**
	 * 取第一个查询方案
	 * @param funId
	 * @param userId
	 * @return
	 */
	private String queryFirstId(String funId, String userId) {
		String sqlm = "select query_id from sys_query where is_default = '1' and fun_id = ? and " +
				"(is_share = '1' or user_id = ?) order by query_no";
		DaoParam param = _dao.createParam(sqlm);
		param.addStringValue(funId);
		param.addStringValue(userId);
		
		Map<String,String> mp = _dao.queryMap(param);
		if (mp.isEmpty()) return "";
		
		return mp.get("query_id");
	}
	
	/**
	 * 取最大的方案序号
	 * @param funId
	 * @return
	 */
	private int queryCaseNo(String funId) {
		String sql = "select query_no from sys_query where fun_id = ? order by query_no desc";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		
		Map<String,String> mp = _dao.queryMap(param);
		if (mp.isEmpty()) return 0;
		
		String no = mp.get("query_no");
		return Integer.parseInt(no);
	}
}
