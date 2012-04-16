/*
 * ServiceUtil.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.DaoUtil;
import org.jxstar.service.BoException;
import org.jxstar.service.define.DefineDataManger;
import org.jxstar.service.define.FunDefineDao;
import org.jxstar.service.define.FunctionDefine;
import org.jxstar.service.define.FunctionDefineManger;
import org.jxstar.util.ArrayUtil;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.key.KeyCreator;
import org.jxstar.util.log.Log;
import org.jxstar.util.resource.JsMessage;

/**
 * 服务层工具类.
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class ServiceUtil {
	//日志对象
	private static Log _log = Log.getInstance();	
	//DAO对象
	private static BaseDao _dao = BaseDao.getInstance();
	
	/**
	 * 把请求对象中的值取到map中，字段名为键值.
	 * 
	 * @param requestContext - 请求上下文对象 
	 * @param asCol - 字段名样式:tbl.col
	 * @return Map<String,String>
	 */	
	public static Map<String,String> getRequestMap(
			RequestContext requestContext, 
			String[] asCol) {
		if (asCol == null || asCol.length == 0) {
			_log.showWarn("get asCol param is null! ");
			return null;
		}
		if (requestContext == null) {
			_log.showWarn("get requestContext param is null! ");
			return null;
		}
		
		Map<String,String> param = FactoryUtil.newMap();
		
		for (int i = 0; i < asCol.length; i++) {
			param.put(asCol[i], requestContext.getRequestValue(asCol[i]));
		}
		
		return param;
	}
	
	/**
	 * 从请求数据中根据表名取数据值，返回数据的字段名中不含表名。
	 * @param mpRequest -- 请求数据
	 * @param tableName -- 表名
	 * @return
	 */
	public static Map<String,String> getDirtyData(
			Map<String,Object> mpRequest, 
			String tableName) {
		Map<String,String> mpData = FactoryUtil.newMap();
		Iterator<String> keys = mpRequest.keySet().iterator();
		
		//取修改了值的字段名
		String dirtyFields = MapUtil.getValue(mpRequest, "dirtyfields");
		_log.showDebug("--------save dirtyfields=" + dirtyFields);
		List<String> lsField = ArrayUtil.arrayToList(dirtyFields.split(";"));
		
		while(keys.hasNext()) {
			String name = keys.next();
			
			if (name.length() > 0 && name.indexOf(tableName+".") == 0 && lsField.contains(name)) {
				String field = StringUtil.getNoTableCol(name);
				String value = MapUtil.getValue(mpRequest, name);
				
				mpData.put(field, value);
			}
		}
		_log.showDebug("--------dirty mpData=" + mpData.toString());
		
		return mpData;
	}
	
	/**
	 * 取上下文对象中的多组数据
	 * 把请求对象中的值取到map中，字段名为键值.
	 * 
	 * @param requestContext - 请求上下文对象 
	 * @param asCol - 字段名样式:tbl.col
	 * @return List<Map<String,String>>
	 */
	public static List<Map<String,String>> getRequestMaps(
			RequestContext requestContext, 
			String[] asCol) {
		List<Map<String,String>> lsVal = FactoryUtil.newList();
		if (asCol == null || asCol.length == 0) {
			_log.showWarn("get asCol param is null! ");
			return lsVal;
		}
		if (requestContext == null) {
			_log.showWarn("get requestContext param is null! ");
			return lsVal;
		}
		
		for (int i = 0; i < asCol.length; i++) {
			String[] oneVal = requestContext.getRequestValues(asCol[i]);
			Map<String,String> mpVal;
			for (int j = 0; j < oneVal.length; j++) {
				if (j >= lsVal.size()) {
					mpVal = FactoryUtil.newMap();
					lsVal.add(mpVal);
				} else {
					mpVal = lsVal.get(j);
				}
				mpVal.put(asCol[i], oneVal[j]);
			}
		}
		_log.showDebug("getValuesByCol:" + lsVal);
		
		return lsVal;
	}
	
	/**
	 * 把map中的参数值, 根据字段列表取值, 存到数组中.
	 * 
	 * @param mpValue - 保存字段值的map对象 
	 * @param asCol - 字段名样式:tbl.col
	 * @return String[]
	 */	
	public static String[] requestMapToArray(
			Map<String,String> mpValue, String[] asCol) {
		if (asCol == null || asCol.length == 0) {
			_log.showWarn("get asCol param is null! ");
			return null;
		}
		if (mpValue == null) {
			_log.showWarn("get mpValue param is null! ");
			return null;
		}
		
		String[] asVal = new String[asCol.length];
		for (int i = 0; i < asCol.length; i++) {
			asVal[i] = MapUtil.getValue(mpValue, asCol[i]);
		}
		
		return asVal;
	}

	/**
	 * 把Context中的参数值, 根据字段列表取值, 存到数组中.
	 * 
	 * @param requestContext - 传递给组件的环境信息 
	 * @param asCol - 字段名样式:tbl.col
	 * @return String[]
	 */
	public static String[] contextToValues(RequestContext requestContext, 
			String[] asCol) {
		if (asCol == null || asCol.length == 0) {
			_log.showWarn("get asCol param is null! ");
			return null;
		}
		if (requestContext == null) {
			_log.showWarn("get requestContext param is null! ");
			return null;
		}
		
		String[] asVal = new String[asCol.length];
		for (int i = 0; i < asCol.length; i++) {
			asVal[i] = requestContext.getRequestValue(asCol[i]);
		}
		
		return asVal;
	}
	
	/**
	 * 检查是否有重复值，如果有重复则会抛出异常信息。
	 * 
	 * @param keyId - 当前记录主键值，如果是新增时判断则为空
	 * @param mpValue - 当前操作的数据对象，因为表格保存时会存在多条记录的值，所有只取当前保存的数据
	 * @param request - 请求上下文
	 * @return 
	 */
	public static void isRepeatVal(String keyId, 
			Map<String,String> mpValue, RequestContext request) throws BoException {
		String funId = request.getFunID();
		DefineDataManger manger = DefineDataManger.getInstance();
		Map<String,String> mpFun = manger.getFunData(funId);
		String tableName = mpFun.get("table_name");
		String pkColName = mpFun.get("pk_col");
		
		//如果是子功能，需要添加外键检查
		String regType = mpFun.get("reg_type");//sub
		String fkColName = mpFun.get("fk_col");
		fkColName = StringUtil.getNoTableCol(fkColName);
		String fkValue = request.getRequestValue("fkValue");
		
		//判断是否有重复值
		List<Map<String, String>> lsRepeat = FunDefineDao.queryRepeatCol(funId);
		if (lsRepeat != null && !lsRepeat.isEmpty()) {
			for (int i = 0; i < lsRepeat.size(); i++) {
				Map<String, String> mpRepCol = lsRepeat.get(i);
				String sRepCol = (String) mpRepCol.get("col_code");
				String sRepName = (String) mpRepCol.get("col_name");
				
				//如果是保存前判断，且字段值未被修改，则不需要判断重复
				String sRepVal = MapUtil.getValue(mpValue, sRepCol);
				if (sRepVal == null || sRepVal.length() == 0) {
					continue;
				}
				
				StringBuilder sbRepSQL = new StringBuilder("select count(*) as cnt ");
					sbRepSQL.append(" from ").append(tableName);
					sbRepSQL.append(" where ").append(sRepCol).append(" = '");
					sbRepSQL.append(sRepVal + "' ");
				//如果是保存时检查,则当前记录除外
				if (keyId.length() > 0) {
					sbRepSQL.append(" and " + pkColName + " <> '" + keyId + "'");
				}
				//如果是子功能，需要添加外键检查
				if (regType.equals("sub") && fkColName.length() > 0 && fkValue.length() > 0) {
					sbRepSQL.append(" and " + tableName+"."+fkColName + " = '" + fkValue + "'");
				}
				_log.showDebug("select repeatval sql=" + sbRepSQL.toString());
				
				DaoParam param = _dao.createParam(sbRepSQL.toString());
				Map<String, String> mpCnt = _dao.queryMap(param);
				if (MapUtil.hasRecord(mpCnt)) {
					//返回信息：字段{0}有重复的值！
					throw new BoException(JsMessage.getValue("functionbm.repeatval", sRepName));
				}
			}
		}
	}
	
	/**
	 * 新增或复制记录。
	 * @param mpParam -- 记录参数值
	 * @param mpUser -- 当前用户信息
	 * @param define -- 功能定义信息
	 * @return
	 */
	public static boolean insertRow(Map<String,String> mpParam, 
			Map<String,String> mpUser, FunctionDefine define) throws BoException {
		String dsName = define.getElement("ds_name");
		//取新增SQL语句,如果更新用户信息则SQL会增加两个信息字段
		String insertSql  = define.getInsertSQL();
		String[] icols = define.getInsertCol();
		//取新增的记录值
		String[] value = ServiceUtil.requestMapToArray(mpParam, icols);
		if (value == null || value.length == 0) {
			throw new BoException(JsMessage.getValue("functionbm.newvaluenull"));
		}
		//给新增时间与新增人赋值
		String hasUser = define.getElement("is_userinfo");
		if (hasUser.equals("1")) {
			//如果保存用户信息, 约定最后两个字段为新增时间与新增人ID
			int len = value.length;
			value[len - 2] = DateUtil.getTodaySec();
			value[len - 1] = MapUtil.getValue(mpUser, "user_id");
		}
		
		//取新增的值数据类型
		String[] type  = define.getInsertParamType();
		//输出调试信息
		_log.showDebug("insert or copy sql=" + insertSql);
		_log.showDebug("insert or copy type=" + ArrayUtil.arrayToString(type));
		_log.showDebug("insert or copy value=" + ArrayUtil.arrayToString(value));
		
		DaoParam param = _dao.createParam(insertSql);
		param.setValues(value).setTypes(type).setDsName(dsName);
		return _dao.update(param);
	}
	
	/**
	 * 复制子表数据。
	 * @param oldKeyID -- 原记录主键值
	 * @param newKeyID -- 新记录主键值
	 * @param mpUser -- 用户信息
	 * @param define -- 功能定义对象
	 * @return
	 */
	public static boolean copySubData(String oldKeyID, String newKeyID, 
			Map<String,String> mpUser, FunctionDefine define) throws BoException {
		//取子功能ID
		String subFunId = define.getElement("subfun_id");
		if (subFunId.length() == 0) return true;
		String subFunIds[] = subFunId.split(",");
		
		//取主键、无表名主键
		String pkcol = define.getElement("pk_col");
		String pk = StringUtil.getNoTableCol(pkcol);
		
		//子功能定义对象
		FunctionDefine subDefine;
		FunctionDefineManger funManger = FunctionDefineManger.getInstance();
		
		//主键创建对象
		KeyCreator keyCreator = KeyCreator.getInstance();
		
		//复制每个子功能的数据
		for (int i = 0, n = subFunIds.length; i < n; i++) {
			subDefine = funManger.getDefine(subFunIds[i]);
			String tableName = subDefine.getElement("table_name");
			String dsName = subDefine.getElement("ds_name");
			String fkcol = subDefine.getElement("fk_col");
			String subPkcol = subDefine.getElement("pk_col");
			//如果定义了外键则取定义值，否则取主功能主键，外键无表名
			if (fkcol.length() == 0) {
				fkcol = pk;
			} else {
				fkcol = StringUtil.getNoTableCol(fkcol);
			}
			fkcol = tableName + "." + fkcol;
			
			//构建查询语句
			String whereSql = " where " + fkcol + " = ?";
			String funWhere = subDefine.getWhereSQL();
			if (funWhere.length() > 0) {
				whereSql = " where " + funWhere + " and " + fkcol + " = ?";
			}
			
			String selectSql = subDefine.getSelectSQL() + whereSql;
			_log.showDebug("select sub data sql=" + selectSql);
			
			//查询需要复制的子表数据
			DaoParam param = _dao.createParam(selectSql);
			param.setDsName(dsName);
			param.addStringValue(oldKeyID);
			List<Map<String, String>> lsData = _dao.query(param);
			_log.showDebug("sub data num=" + lsData.size() + " oldKeyID=" + oldKeyID);
			
			//复制子表数据
			for (int j = 0, m = lsData.size(); j < m; j++) {
				Map<String, String> mpData = lsData.get(j);
				//给键添加表名
				mpData = DaoUtil.mapAddTable(mpData, tableName);
				//取原子功能主键值
				String oldSubKeyID = mpData.get(subPkcol);
				//设置外键值为当前新的主记录的主键值
				mpData.put(fkcol, newKeyID);
				//创建明细记录主键值
				String subKeyID = keyCreator.createKey(tableName);
				if (subKeyID == null || subKeyID.length() == 0) {
					//新增记录时生成的主键值为空！
					_log.showWarn("new sub keyid value is null!");
					return false;
				}
				mpData.put(subPkcol, subKeyID);
				
				if (!insertRow(mpData, mpUser, subDefine)) {
					return false;
				}
				
				//复制子功能的子功能数据，递归处理
				copySubData(oldSubKeyID, subKeyID, mpUser, subDefine);
			}
		}

		return true;
	}
	
	/**
	 * 创建记录主键值，如果是普通功能，则直接生成主键，如果是树型功能，
	 * 则按规则四位一级生成，且处理级别字段值
	 * @param requestContext
	 * @return
	 */
	public static String createPkValue(Map<String,String> mpVal, RequestContext requestContext) {
		String funID = requestContext.getFunID();
		DefineDataManger manger = DefineDataManger.getInstance();
		Map<String,String> mpFun = manger.getFunData(funID);
		String tableName = mpFun.get("table_name");
		String pkField = mpFun.get("pk_col");
		String regType = mpFun.get("reg_type");
		String dsName = mpFun.get("ds_name");
		
		//主键创建对象
		KeyCreator keyCreator = KeyCreator.getInstance();
		String keyid = "";
		//树型主功能
		if(regType.equalsIgnoreCase("treemain")){
			String parentId = requestContext.getRequestValue("parentId");
			_log.showDebug("treeParentId:" + parentId);
			
			//如果父级ID长度小于4，则改ID为空，根节点的ID为10，要忽略
			if (parentId.length() < 4) parentId = "";

			String levelCol = requestContext.getRequestValue("levelCol");
			if(levelCol.indexOf(".") < 0) {
				levelCol = tableName + "." + levelCol;
			}

			int curLevel = parentId.length()/4 + 1;
			
			keyid = keyCreator.createTreeKey(parentId, curLevel, tableName, pkField, levelCol, dsName);
			_log.showDebug("newTreeId:" + keyid + ";treeLevel:" + curLevel);
			
			mpVal.put(levelCol, Integer.toString(curLevel));
		}else{
			keyid = keyCreator.createKey(tableName);
		}
		mpVal.put(pkField, keyid);
		
		return keyid;
	}
}
