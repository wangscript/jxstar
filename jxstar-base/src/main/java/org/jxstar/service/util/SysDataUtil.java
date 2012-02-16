/*
 * SysDataUtil.java 2008-5-18
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.util;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.BoException;
import org.jxstar.service.define.DefineDataManger;
import org.jxstar.service.define.FunDefineDao;
import org.jxstar.util.MapUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;
import org.jxstar.util.resource.JsMessage;


/**
 * 数据权限工具类：
 * 1、如果一个用户同一个功能有两个角色，则两个角色之间的数据权限关系是or
 * 2、一个角色中同一字段的数据权限采用or，不同字段之间缺省为and，可以在角色中设置为or
 * 3、如果要处理运算关系，则角色中的数据权限需要设置好各字段序号
 * 
 * @author TonyTan
 * @version 1.0, 2008-5-18
 */
public class SysDataUtil {
	private static Log _log = Log.getInstance();
	private static BaseDao _dao = BaseDao.getInstance();

	/**
	 * 取该用户指定功能的数据权限过滤语句。
	 * 
	 * @param userId -- 用户ID
	 * @param funId -- 功能ID
	 * @return String
	 */
	public static String getDataWhere(String userId, String funId) throws BoException {
		if (userId == null || userId.length() == 0) {
			throw new BoException(JsMessage.getValue("param.null.userid"));
		}
		if (funId == null || funId.length() == 0) {
			throw new BoException(JsMessage.getValue("param.null.funid"));
		}
		
		//判断用户是否为系统管理员
		boolean isAdmin = SysUserUtil.isAdmin(userId);
		if (isAdmin) return "";
		
		//如果有个人扩展数据权限，则不取通用权限
		if (hasPropFun(userId, funId)) {
			String extsql = queryExtSql(userId, funId);
			if (extsql.length() > 0) return extsql;
		}
		
		return buildWhere(userId, funId);
	}
	
	/**
	 * 取树形数据的数据权限过滤语句，与普通功能的差别是：
	 * 如果是树形功能主键作为数据权限控制字段，则过滤语句取当前级别的数据类型值，
	 * 如：数据权限设置部门ID为10010001，则
	 *    在第1级只能看到 = '1001'的记录，
	 *    在第2级只能看到 = '10010001'的记录，
	 *    在第3级能看到 like '10010001%'的记录。
	 * 
	 * @param userId -- 用户ID
	 * @param funId -- 功能ID
	 * @param level -- 当前控制的级别
	 * @return
	 * @throws BoException
	 */
	public static String getTreeDataWhere(String userId, String funId, int level) throws BoException {
		if (userId == null || userId.length() == 0) {
			throw new BoException(JsMessage.getValue("param.null.userid"));
		}
		if (funId == null || funId.length() == 0) {
			throw new BoException(JsMessage.getValue("param.null.funid"));
		}
		
		//判断用户是否为系统管理员
		boolean isAdmin = SysUserUtil.isAdmin(userId);
		if (isAdmin) return "";
		
		//树形功能的扩展数据权限暂时无效，无法处理树形下级数据，如果只控制第一级还有用处
		if (hasPropFun(userId, funId)) {
			String extsql = queryExtSql(userId, funId);
			if (extsql.length() > 0) return extsql;
		}
		
		return buildTreeWhere(userId, funId, level);
	}
	
	//构建树形结构的多个角色的数据权限
	private static String buildTreeWhere(String userId, String funId, int level) 
		throws BoException {
		List<String> lsRoles = queryRole(userId, funId);
		if (lsRoles.isEmpty()) return "";
		
		//构建各角色的where
		StringBuilder sbWhere = new StringBuilder();
		for (int i = 0, n = lsRoles.size(); i < n; i++) {
			String roleId = lsRoles.get(i);
			
			//获取用户数据类型值定义
			List<Map<String,String>> lsData = queryTreeDataType(roleId, userId, funId, level);
			
			//其中一个角色没设置数据权限，则该用户就不受权限控制
			if (lsData.isEmpty()) return "";
			
			String where = buildRoleWhere(funId, roleId, lsData);
			if (where.length() == 0) return "";
			
			//累加不同角色的数据权限
			sbWhere.append("(").append(where).append(") or ");
		}
		
		String funWhere = "";
		if (sbWhere.length() > 4) {
			funWhere = sbWhere.substring(0, sbWhere.length()-4);
		}
		
		return StringUtil.addkf(funWhere);
	}
	
	//构建普通功能的多个角色的数据权限
	private static String buildWhere(String userId, String funId) 
		throws BoException {
		List<String> lsRoles = queryRole(userId, funId);
		if (lsRoles.isEmpty()) return "";
		
		//构建各角色的where
		StringBuilder sbWhere = new StringBuilder();
		for (int i = 0, n = lsRoles.size(); i < n; i++) {
			String roleId = lsRoles.get(i);
			
			//获取用户数据类型值定义
			List<Map<String,String>> lsData = queryDataType(roleId, userId, funId);
			
			//其中一个角色没设置数据权限，则该用户就不受权限控制
			if (lsData.isEmpty()) return "";
			
			String where = buildRoleWhere(funId, roleId, lsData);
			if (where.length() == 0) return "";
			
			//累加不同角色的数据权限
			sbWhere.append("(").append(where).append(") or ");
		}
		
		String funWhere = "";
		if (sbWhere.length() > 4) {
			funWhere = sbWhere.substring(0, sbWhere.length()-4);
		}
		
		return StringUtil.addkf(funWhere);
	}
	
	//构建一个角色的数据权限
	private static String buildRoleWhere(String funId, String roleId, 
			List<Map<String,String>> lsData) throws BoException {
		//取当前功能的字段信息
		List<String> lsField = getFunColumn(funId);
		//取功能定义from语句中的相关表名
		String[] tables = getFunTables(funId);
		if (tables == null) {
			throw new BoException(JsMessage.getValue("system.fromsql.error"));
		}
		
		//取数据权限的运算关系
		List<Map<String,String>> lsRela = queryFunData(funId, roleId);
		if (lsRela.isEmpty()) return "";
		
		//保存单个数据权限控制字段拼凑的where
		StringBuilder sbType = new StringBuilder();
		//保存所有数据权限控制字段拼凑的where
		StringBuilder sbWhere = new StringBuilder();
		
		for (int k = 0, m = lsRela.size(); k < m; k++) {
			Map<String,String> mpRela = lsRela.get(k);
			String oneField = mpRela.get("dtype_field");
			
			for(int i = 0, n = lsData.size(); i < n; i++) {
				Map<String,String> mpData = lsData.get(i);
				
				String hasSub = mpData.get("has_sub");
				String typeValue = mpData.get("dtype_data");
				String typeField = mpData.get("dtype_field");
				if (!typeField.equals(oneField)) continue;
				
				//如果数据值为空，不能查询数据
				if (typeValue == null || typeValue.length() == 0) {
					typeValue = "&notdata&";
				}
				
				//取功能字段列定义中的字段名
				String funField = getTableField(tables, typeField, lsField);
				//如果字段列表中没有该字段，则不处理
				if (funField.length() == 0) continue;
				
				//添加单个值比较的where语句
				sbType.append(funField);
				if (hasSub.equals("1")) {
					sbType.append(" like '").append(typeValue).append("%'");
				} else {
					sbType.append(" = '").append(typeValue).append("'");
				}
				sbType.append(" or ");
				//_log.showDebug("--------data type sql=" + sbType.toString());
			}
			if (sbType.length() > 0) {
				String oneWhere = sbType.substring(0, sbType.length() - 4);
				sbType = sbType.delete(0, sbType.length());
				
				sbWhere = whereAndOr(sbWhere, oneWhere, oneField, mpRela);
			}
			_log.showDebug("--------field name=" + oneField + "; field where sql=" + sbWhere.toString());
		}
		
		//取数据权限的where
		String dataWhere = "";
		if (sbWhere.length() > 0) {
			dataWhere = sbWhere.substring(0, sbWhere.length()-5);
		}
		_log.showDebug("--------data where sql=" + dataWhere);
		
		return dataWhere;
	}
	
	/**
	 * 处理单个字段的运算关系符号
	 * @param sbWhere -- 合并后的where
	 * @param oneWhere -- 单个字段的where
	 * @param field -- 字段名称
	 * @param mpAndor -- 运算关系符号
	 * @return
	 */
	private static StringBuilder whereAndOr(StringBuilder sbWhere, String oneWhere, 
			String field, Map<String,String> mpAndor) throws BoException {
		//处理不同字段之间的运算关系
		String leftBrack = MapUtil.getValue(mpAndor, "left_brack").trim();
	    String rightBrack = MapUtil.getValue(mpAndor, "right_brack").trim();
	    String andor = MapUtil.getValue(mpAndor, "andor").trim();
	    //不同字段之间，缺省采用"and"关系
	    if (andor.length() == 0) {
	    	andor = "and";
	    }
	    if (leftBrack.length() > 0 && !isValidBrack(leftBrack, '(')) {
	    	throw new BoException(JsMessage.getValue("sysdata.error1"));
	    }
	    if (rightBrack.length() > 0 && !isValidBrack(rightBrack, ')')) {
	    	throw new BoException(JsMessage.getValue("sysdata.error2"));
	    }
		
		//添加不同字段之间的括弧与and、or
		if (leftBrack.length() > 0) {
			sbWhere.append(leftBrack);
		}
		
		sbWhere.append("(").append(oneWhere).append(")");
		
		if (rightBrack.length() > 0) {
			sbWhere.append(rightBrack);
		}
		
		if (andor.length() == 2) {
			sbWhere.append(" or  ");//最后统一去掉5个字符，所以多一个空格
		} else {
			sbWhere.append(" and ");
		}
		
		return sbWhere;
	}
	
	/**
	 * 判断是否为指定功能设置了数据权限。
	 * @param userId -- 用户ID
	 * @param funId -- 功能ID
	 * @return
	 */
	private static boolean hasPropFun(String userId, String funId) {
		String sql = "select count(*) as cnt from sys_user_funx where fun_id = ? and user_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		param.addStringValue(userId);
		
		return MapUtil.hasRecord(_dao.queryMap(param));
	}
	
	/**
	 * 取用户自定义数据权限SQL
	 * @param userId -- 用户ID
	 * @param funId -- 功能ID
	 * @return
	 */
	private static String queryExtSql(String userId, String funId) {
		String sql = "select ext_sql from sys_user_funx where user_id = ? and fun_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(userId);
		param.addStringValue(funId);
		
		Map<String,String> mpData = _dao.queryMap(param);
		
		String extsql = MapUtil.getValue(mpData, "ext_sql");
		return StringUtil.addkf(extsql);
	}
	
	/**
	 * 取用户与指定功能的数据权限控制值
	 * 
	 * @param roleId -- 角色ID
	 * @param userId -- 用户ID
	 * @param funId -- 功能ID
	 * @return
	 */
	private static List<Map<String,String>> queryDataType(String roleId, String userId, String funId) {
		String sql = "select distinct dtype_data, has_sub, dtype_field " +
					 "from v_user_data where role_id = ? and user_id = ? and fun_id = ? " +
					 "order by dtype_field";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(roleId);
		param.addStringValue(userId);
		param.addStringValue(funId);
		
		return _dao.query(param);
	}
	
	/**
	 * 如果是树形功能主键作为数据权限控制字段，则过滤语句取当前级别的数据类型值。
	 * 
	 * @param roleId -- 角色ID
	 * @param userId -- 用户ID
	 * @param funId -- 功能ID
	 * @param level -- 当前级别
	 * @return
	 */
	private static List<Map<String,String>> queryTreeDataType(
			String roleId, String userId, String funId, int level) {
		List<Map<String,String>> lsData = queryDataType(roleId, userId, funId);
		if (lsData.isEmpty()) return lsData;
		
		//取功能定义信息
		Map<String,String> mpFun = FunDefineDao.queryFun(funId);
		
		//取功能定义主键值
		String pk_col = mpFun.get("pk_col").toLowerCase();
		pk_col = StringUtil.getNoTableCol(pk_col);
		
		for (int i = 0, n = lsData.size(); i < n; i++) {
			Map<String,String> mpData = lsData.get(i);
			
			String dtype_data = mpData.get("dtype_data");
			String dtype_field = mpData.get("dtype_field");
			
			//如果主键字段，则处理数据类型值
			if (pk_col.equals(dtype_field)) {
				if (dtype_data.length() <= level*4) continue;
				
				dtype_data = dtype_data.substring(0, level*4);
				mpData.put("dtype_data", dtype_data);
				mpData.put("has_sub", "0");
			}
		}
		
		return lsData;
	}
	
	/**
	 * 取功能中是否包括数据权限控制字段，如果有则返回控制字段，否则为空
	 * @param tables -- 当前功能相关的表名
	 * @param field -- 控制字段
	 * @param lsCol -- 功能字段列表
	 * @return
	 */
	private static String getTableField(String[] tables, String field, List<String> lsCol) {
		String tfield = "";
		for (int i =0, n = tables.length; i < n; i++){
			tfield = tables[i] + "." + field;
			if (lsCol.contains(tfield)){
				return tfield;
			} else {
				if (field.equalsIgnoreCase("add_userid")) {
					return tfield;
				}	
			}
		}
		return "";
	}
	
	/**
	 * 取一个功能的字段列表
	 * @param funId -- 功能ID
	 * @return
	 */
	private static List<String> getFunColumn(String funId) {
		List<String> lsRet = FactoryUtil.newList();
		
		//取字段设计信息
		DefineDataManger config = DefineDataManger.getInstance();
		List<Map<String,String>> lsCol = config.getColData(funId);
		
		for (int i = 0, n = lsCol.size(); i < n; i++) {
			Map<String,String> mp = lsCol.get(i);
			
			lsRet.add(mp.get("col_code"));
		}
		
		return lsRet;
	}
	
	/**
	 * 取功能定义from语句中的相关表名
	 * @param funId -- 功能ID
	 * @return
	 */
	private static String[] getFunTables(String funId) {
		//取功能定义信息
		DefineDataManger config = DefineDataManger.getInstance();
		Map<String,String> mpFun = config.getFunData(funId);
		
		//取功能定义from语句
		String formTable = mpFun.get("from_sql").toLowerCase();
		if (formTable == null || formTable.length() == 0) return null;
		//取功能主表名
		String mainTable = mpFun.get("table_name").toLowerCase();
		
		String[] froms = formTable.split("from ");
		if (froms.length < 2) return null;
		String[] tables = froms[1].split(",");
		
		//清除表名间空格，并把主表名排在第1个
		for (int i =0, n = tables.length; i < n ; i ++){
			tables[i] = tables[i].trim();
			
			if (tables[i].equals(mainTable) && i > 0){
				String strTmp = tables[0];
				tables[0] = mainTable;
				tables[i] = strTmp;
			}
		}
		return tables;
	}
	
	/**
	 * 检查字符串是否为括弧
	 * @param str -- 字符串
	 * @param c -- 括弧
	 * @return
	 */
	private static boolean isValidBrack(String str, char c) {
		if (str == null || str.length() == 0) return false;
		
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) != c) return false;
		}
		
		return true;
	}
	
	/**
	 * 查询用户指定功能的角色
	 * @param userId -- 用户ID
	 * @param funId -- 功能ID
	 * @return
	 */
	private static List<String> queryRole(String userId, String funId) {
		List<String> lsRet = FactoryUtil.newList();
		String sql = "select distinct sys_user_role.role_id " +
					 "from sys_user_role, sys_role_fun " +
					 "where sys_user_role.role_id = sys_role_fun.role_id and " +
					 "sys_user_role.user_id = ? and sys_role_fun.fun_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(userId);
		param.addStringValue(funId);
		
		List<Map<String,String>> lsData = _dao.query(param);
		if (lsData.isEmpty()) return lsRet;
		
		for (int i = 0, n = lsData.size(); i < n; i++) {
			Map<String,String> mpData = lsData.get(i);
			lsRet.add(mpData.get("role_id"));
		}
		
		return lsRet;
	}
	
	/**
	 * 查询角色中的指定功能的数据权限运算关系
	 * @param funId -- 功能ID
	 * @param roleId -- 角色ID
	 * @return
	 */
	private static List<Map<String,String>> queryFunData(String funId, String roleId) {
		String sql = "select sys_datatype.dtype_field, sys_role_data.left_brack, sys_role_data.right_brack, sys_role_data.andor " + 
					 "from sys_role_data, sys_role_fun, sys_datatype " +
					 "where sys_role_data.role_fun_id = sys_role_fun.role_fun_id and " +
					 "sys_role_data.dtype_id = sys_datatype.dtype_id and " +
					 "sys_role_fun.fun_id = ? and sys_role_fun.role_id = ? " +
					 "order by sys_role_data.data_no";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		param.addStringValue(roleId);
		
		return _dao.query(param);
	}
}
