/*
 * GridQuery.java 2009-6-10
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.query;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.JsonDao;
import org.jxstar.dao.util.DBTypeUtil;
import org.jxstar.service.BoException;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.DefineName;
import org.jxstar.service.define.FunctionDefine;
import org.jxstar.service.define.FunctionDefineManger;
import org.jxstar.service.util.PageSQL;
import org.jxstar.service.util.WhereUtil;
import org.jxstar.util.ArrayUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 取grid的数据对象。
 *
 * @author TonyTan
 * @version 1.0, 2009-6-10
 */
public class GridQuery extends BusinessObject {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 根据请求信息查询数据
	 * @param request
	 * @return
	 */
	public String query(RequestContext request) {
		//读取数据的开始位置，第一条数据为0
		int istart = Integer.parseInt(request.getRequestValue("start", "0"));
		//可以读取记录的条数
		int ilimit = Integer.parseInt(request.getRequestValue("limit", "0"));
		//排序字段，表名后面的.用__替代的，要替换回来
		String sort = request.getRequestValue("sort");
		sort = sort.replaceFirst("__", ".");
		//排序方式缺省 asc 降序是desc
		String dir = request.getRequestValue("dir").toLowerCase();		
		String funid = request.getRequestValue("query_funid");
		String userid = request.getRequestValue("user_id");
		String wheresql = request.getRequestValue("where_sql");
		String wheretype = request.getRequestValue("where_type");
		String wherevalue = request.getRequestValue("where_value");
		//查询方式：0 -- 普通查询 1 -- 高级查询
		String querytype = request.getRequestValue("query_type");
		//加分页处理：0 -- 是不加分页处理 1 -- 是加分页处理
		String has_page = request.getRequestValue("has_page", "1");
		//页面类型，如果是审批页面类型，则不处理归档，相当于设置为高级查询
		String pagetype = request.getRequestValue("pagetype");
		
		//取功能定义对象
		FunctionDefine funObj = FunctionDefineManger.getInstance().getDefine(funid);
		
		//如果是审批页面，则直接取前台传递的WHERESQL
		String where = "";
		if (pagetype.indexOf("chk") >= 0) {
			//功能定义中的wheresql需要添加，如多表关联定义的功能
			where = StringUtil.addkf(funObj.getElement("where_sql"));
			if (where != null && where.length() > 0) {
				if (wheresql != null && wheresql.trim().length() > 0) {
					where = where + " and " + wheresql;
				}
			} else {
				where = wheresql;
			}
		} else {
			//取功能定义查询where
			try {
				where = WhereUtil.queryWhere(funid, userid, wheresql, querytype);
			} catch (BoException e) {
				setMessage(e.getMessage());
				_log.showError(e);
			}
		}
		
		//取select语句
		String select = funObj.getSelectSQL();
		if (select == null || select.length() == 0) {
			_log.showDebug("get gridquery select sql is null!");
			setMessage(JsMessage.getValue("queryepro.selectsqlnull"));
			
			return _returnFaild;
		}
		
		//组合页面SQL
		StringBuilder sbpage = new StringBuilder(select);
		if (where != null && where.length() > 0) {
			sbpage.append(" where " + where);
		}
		
		//添加group by
		String groupsql = funObj.getElement("group_sql").trim();
		if (groupsql.length() > 0) {
			sbpage.append(" group by " + groupsql);
		}
		
		//排序子句，如果界面有传递排序子句，则不从功能定义中取值
		String ordersql = "";
		if (sort.length() == 0) {
			String osql = funObj.getElement("order_sql").trim();
			if (osql.length() > 0) {
				ordersql = " order by " + osql;
			}
		} else {
			ordersql = " order by " + sort + " " + dir;
			//防止排序后分页数据不对添加的排序字段
			String field = getSortField(funid);
			if (field.length() > 0) {
				//如果是SqlServer数据库，两个相同的字段排序会报错
				String f = StringUtil.getNoTableCol(field);
				if (sort.indexOf(f) < 0) {
					ordersql += ", " + field;
				}
			}
		}
		_log.showDebug("gridquery order sql:" + ordersql);
		
		//取数据库类型、数据源名称
		String dsname = funObj.getElement("ds_name");
		String dbtype = DBTypeUtil.getDbmsType(dsname);	

		//分页处理前，取记录的总条数
		String presql = sbpage.toString();
		presql = PageSQL.getCountSQL(presql);
		//构建查询参数对象
		DaoParam param = new DaoParam();
		param.setSql(presql);
		param.setValue(wherevalue).setType(wheretype).setDsName(dsname);
		Map<String,String> mpCnt = _dao.queryMap(param);
		int rownum = MapUtil.hasRecodNum(mpCnt);
		
		//添加排序语句
		sbpage.append(ordersql);
		//SQL语句加分页处理
		String pagesql = sbpage.toString();
		if (has_page.equals("1")) {
			pagesql = PageSQL.getPageSQL(pagesql, dbtype, istart, ilimit);
		}
		
		_log.showDebug("gridquery allsql:" + pagesql);
		_log.showDebug("gridquery wheretype:" + wheretype);
		_log.showDebug("gridquery wherevalue:" + wherevalue);
		
		//查询页面数据
		JsonDao jsonDao = JsonDao.getInstance();
		param.setSql(pagesql);
		String[] cols = ArrayUtil.getGridCol(sbpage.toString());
		String strJson = jsonDao.query(param, cols);
		
		//查询SQL异常
		if (strJson == null) {
			_log.showWarn("grid query error!");
			setMessage(JsMessage.getValue("web.query.error"));
			return _returnFaild;
		}
		
		//构建统计语句，取统计结果
		String sumJson = "";
		if (rownum > 0) {
			String sumSql = funObj.getSumSQL();
			if (sumSql != null && sumSql.length() > 0) {
				if (where != null && where.length() > 0) {
					sumSql += " where " + where;
				}
				_log.showDebug("total allsql:" + sumSql);
				
				param.setSql(sumSql);
				String[] sumCols = getSumCols(sumSql);
				sumJson = jsonDao.query(param, sumCols);
			}
		}
		
		StringBuilder sbJson = new StringBuilder("{total:"+rownum+",root:[");
		sbJson.append(strJson + "],sum:["+sumJson+"]}");
		//_log.showDebug("json=" + sbJson.toString());
		
		//返回查询数据
		setReturnData(sbJson.toString());
		
		return _returnSuccess;
	}
	
	/**
	 * SQL格式为：select sum(col1),sum(col2)... from ...
	 * 取sum()中的那个字段名，并替换.为__
	 * @param sql
	 * @return
	 */
	public String[] getSumCols(String sql) {
		List<String> lsRet = FactoryUtil.newList();
		
		Pattern p = Pattern.compile("sum\\([^(]+\\)");
		Matcher m = p.matcher(sql);
		while (m.find()) {
			String col = m.group();
			col = col.substring(4, col.length()-1);
			col = col.replace(".", "__");
			lsRet.add(col);
		}
		
		return lsRet.toArray(new String[lsRet.size()]);
	}
	
	//防止排序后分页数据不对添加的排序字段
	private String getSortField(String funId) {
		String sql = "select attr_value from fun_attr where attr_name = 'sortField' and fun_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		
		Map<String,String> mp = _dao.queryMap(param);
		return MapUtil.getValue(mp, "attr_value").trim();
	}
}
