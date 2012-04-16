/*
 * PortletResultBO.java 2011-1-11
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.portlet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jxstar.dao.DaoParam;
import org.jxstar.dao.util.SqlParserUtil;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.resource.JsMessage;

/**
 * 取结果集的统计数据结果，与结果集设置信息。
 * 统计SQL中可以包含常用常量的标志字符串。
 *
 * @author TonyTan
 * @version 1.0, 2011-1-11
 */
public class PortletResultBO extends BusinessObject {
	private static final long serialVersionUID = 642337109953897525L;

	/**
	 * 取结果集的统计数据结果，与结果集设置信息。
	 * @param chartId -- 设置ID
	 * @param userInfo -- 当前用户信息
	 * @return
	 */
	public String getChartJson(String chartId, Map<String,String> userInfo) {
		Map<String,String> mpChart = queryChart(chartId);
		if (mpChart.isEmpty()) {
			//"图形结果集【{0}】设置信息为空！"
			setMessage(JsMessage.getValue("portlet.chatsetnull"), chartId);
			return _returnFaild;
		}
		
		//统计SQL语句
		String resultSql = mpChart.get("result_sql");
		//解析SQL中的常量对象
		resultSql = SqlParserUtil.parseSQLConstant(resultSql, userInfo);
		//构建参数对象，并解析跨数据库函数的SQL
		DaoParam param = _dao.createParam(resultSql);
		param.setUseParse(true);
		List<Map<String,String>> lsResult = _dao.query(param);
		
		//构建结果集JSON对象
		String titleField = mpChart.get("field_type");
		String numField = mpChart.get("field_stat");
		
		//检查字段定义名是否正确
		if (!lsResult.isEmpty()) {
			Set<String> keySet = lsResult.get(0).keySet();
			if (!keySet.contains(titleField) || !keySet.contains(numField)) {
				//"图形结果集的分类字段或统计字段名定义错误！"
				setMessage(JsMessage.getValue("portlet.chatfieldnull"));
				return _returnFaild;
			}
		}
		
		//统计数据JSON对象
		String dataJson = getResultJson(lsResult, titleField, numField);
		
		//图形类型
		String chartType = mpChart.get("chart_type");
		
		//JSON对象
		StringBuilder sbJson = new StringBuilder();
		sbJson.append("{charttype:'").append(chartType).append("',chartdata:");
		sbJson.append(dataJson).append("}");
		_log.showDebug("-------------result chart json=" + sbJson.toString());
		
		//返回前台
		setReturnData(sbJson.toString());
		
		return _returnSuccess;
	}
	
	/**
	 * 返回二维数组
	 * @param lsResult -- 结果集
	 * @param titleField -- 标题字段
	 * @param numField -- 数值字段
	 * @return
	 */
	private String getResultJson(List<Map<String,String>> lsResult, String titleField, String numField) {
		if (lsResult == null || lsResult.isEmpty()) return "[]";
		
		StringBuilder sbJson = new StringBuilder();
		for (int i= 0, n = lsResult.size(); i < n; i++) {
			Map<String,String> mpResult = lsResult.get(i);
			
			String title = mpResult.get(titleField);
			String value = mpResult.get(numField);
			
			sbJson.append("['").append(title).append("',");
			sbJson.append(value).append("]");
			if (i < n-1) sbJson.append(",");
		}
		
		String json = "[" + sbJson.toString() + "]";
		return json;
	}
	
	/**
	 * 查询图形结果集设置信息
	 * @param chartId -- 设置ID
	 * @return
	 */
	private Map<String,String> queryChart(String chartId) {
		String sql = "select chart_name, result_sql, field_type, field_stat, chart_type, chart_id " +
					 "from plet_chart where chart_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(chartId);
		
		return _dao.queryMap(param);
	}
}
