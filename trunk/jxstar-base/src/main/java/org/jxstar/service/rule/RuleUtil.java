/*
 * RuleUtil.java 2009-12-12
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.rule;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.control.ControlerUtil;
import org.jxstar.service.define.DefineDataManger;
import org.jxstar.util.key.CodeCreator;
import org.jxstar.util.key.KeyCreator;
import org.jxstar.util.log.Log;

/**
 * 规则解析工具类。
 *
 * @author TonyTan
 * @version 1.0, 2009-12-12
 */
public class RuleUtil {
	//外键值标志
	//private static final String FKEYID = "{FKEYID}";
	private static final String FKEYID_REGEX = "\\{FKEYID\\}";
	//新增主键标志
	private static final String NEW_KEYID = "{NEWKEYID}";
	private static final String NEW_KEYID_REGEX = "\\{NEWKEYID\\}";
	//新增编号标志
	private static final String NEW_CODE = "{NEWCODE}";
	private static final String NEW_CODE_REGEX = "\\{NEWCODE\\}";
	//系统日志对象
	protected Log _log = Log.getInstance();
	//数据库访问对象
	protected BaseDao _dao = BaseDao.getInstance();

	/**
	 * 执行导入SQL。
	 * @param srcFunId -- 数据来源的功能ID
	 * @param destFunId -- 新增数据的目标功能ID
	 * @param srcKeyId -- 来源数据的主键值
	 * @param forKeyId -- 新增数据的外键值
	 * @param routeId -- 规则路由ID
	 * @param userInfo -- 用户信息
	 * @return String 返回新增的记录主键值，用;分隔，主要用于子表新增记录时用。
	 * 				  如果执行返回true表示没有定义规则SQL，如果返回false表示执行失败。
	 */
	public String exeInsert(String srcFunId, String destFunId, 
			String srcKeyId, String forKeyId, String routeId, 
			Map<String,String> userInfo) {
		String faild = "false", success = "true", retKeyId = "";
		
		//取规则定义，如果没有定义则不处理
		Map<String, String> mpRule = queryRule(routeId, destFunId);
		if (mpRule.isEmpty()) {
			_log.showDebug("execute insert sql rule not define: " +
					"srcfunid is {0} destfunid is {1} routeid is {2}!",
					srcFunId, destFunId, routeId);
			return success;
		}
		
		DefineDataManger manger = DefineDataManger.getInstance();
		//取来源功能与目标功能的定义信息
		Map<String,String> srcDefine = manger.getFunData(srcFunId);
		Map<String,String> destDefine = manger.getFunData(destFunId);
		
		//取来源功能与目标功能的数据源
		String srcDsName = srcDefine.get("ds_name");
		String destDsName = destDefine.get("ds_name");
		String destTable = destDefine.get("table_name");
		
		//查找来源SQL、目标SQL、规则参数
		String srcSql = mpRule.get("src_sql");	//来源SQL
		String destSql = mpRule.get("dest_sql");//目标SQL
		String ruleId = mpRule.get("rule_id");
		List<Map<String, String>> lsParam = queryRuleParam(ruleId);//规则参数
		_log.showDebug("------------src sql=" + srcSql);
		_log.showDebug("------------dest sql=" + destSql);
		
		//创建主键生成对象
		KeyCreator keyCreator = KeyCreator.getInstance();
		//创建编码生成对象
		CodeCreator codeCreator = CodeCreator.getInstance();
		
		//解析来源SQL中的外键值，这样可以把主表数据写入目标表中
		srcSql = srcSql.replaceFirst(FKEYID_REGEX, addChar(forKeyId));
		
		//取来源数据
		DaoParam srcParam = _dao.createParam(srcSql);
		srcParam.setUseParse(true);
		srcParam.setDsName(srcDsName);
		srcParam.addStringValue(srcKeyId);
		List<Map<String, String>> srcListData = _dao.query(srcParam);
		if (srcListData.isEmpty()) {
			_log.showWarn("execute insert src data list is empty!");
			return faild;
		}
		
		//解析目标SQL中的常量
		destSql = parseConstant(destSql, userInfo);
		//解析目标SQL中的外键值
		destSql = destSql.replaceFirst(FKEYID_REGEX, addChar(forKeyId));
		
		//新增记录
		for (int i = 0, n = srcListData.size(); i < n; i++) {
			Map<String, String> mpValue = srcListData.get(i);
			String newKeyID = "";
			//是否新增主键
			boolean isNewKeyId = (destSql.indexOf(NEW_KEYID) >= 0);
			if (isNewKeyId) {
				newKeyID = keyCreator.createKey(destTable);
				if (newKeyID == null || newKeyID.length() == 0) {
					//新增记录时生成的主键值为空！
					_log.showWarn("execute insert sql: new keyid is null!");
					return faild;
				}
				destSql = destSql.replaceFirst(NEW_KEYID_REGEX, addChar(newKeyID));
			}
			
			//是否新增编号
			boolean isNewCode = (destSql.indexOf(NEW_CODE) >= 0);
			if (isNewCode) {
				String newCode = codeCreator.createCode(destFunId);
				destSql = destSql.replaceFirst(NEW_CODE_REGEX, addChar(newCode));
			}
			
			//执行新增语句
			_log.showDebug("------------parsed sql=" + destSql);
			DaoParam param = _dao.createParam(destSql);
			param.setUseParse(true);
			param.setDsName(destDsName);
			//根据定义的参数顺序赋值
			for (int j = 0, m = lsParam.size(); j < m; j++) {
				Map<String, String> mpParam = lsParam.get(j);
				param.addType(mpParam.get("param_type"));
				String paramName = mpParam.get("param_name");
				String paramValue = mpValue.get(paramName);
				param.addValue(paramValue);
			}
			
			if (!_dao.update(param)) {
				_log.showWarn("execute insert faild!");
				return faild;
			}
			
			retKeyId += newKeyID + ";";
		}
		retKeyId = retKeyId.substring(0, retKeyId.length()-1);
		_log.showDebug("------------retKeyId=" + retKeyId);
		
		return retKeyId;
	}
	
	/**
	 * 执行事件触发的SQL规则定义。
	 * @param funId -- 触发功能ID
	 * @param selKeyId -- 选择的记录ID
	 * @param eventCode -- 事件代号
	 * @param userInfo -- 当前用户信息
	 * @return
	 */
	public boolean exeUpdate(String funId, String selKeyId, String eventCode,
			Map<String,String> userInfo) {
		List<Map<String, String>> lsRule = queryUpdateRule(funId, eventCode);
		if (lsRule.isEmpty()) {
			_log.showDebug("not define rule sql!");
			return true; 
		}
		
		for (int j = 0, m = lsRule.size(); j < m; j++) {
			Map<String, String> mpRule = lsRule.get(j);
			
			//取目标功能ID
			String destFunId = mpRule.get("dest_funid");
			//取目标功能的定义信息
			DefineDataManger manger = DefineDataManger.getInstance();
			Map<String,String> define = manger.getFunData(destFunId);
			//取目标功能的数据源与表名
			String dsName = define.get("ds_name");
			String tableName = define.get("table_name");
			
			//查找来源SQL、目标SQL、规则参数
			String srcSql = mpRule.get("src_sql");	//来源SQL
			String destSql = mpRule.get("dest_sql");//目标SQL
			String ruleId = mpRule.get("rule_id");
			List<Map<String, String>> lsParam = queryRuleParam(ruleId);//规则参数
			_log.showDebug("------------src sql=" + srcSql);
			_log.showDebug("------------dest sql=" + destSql);
			
			//创建主键生成对象
			KeyCreator keyCreator = KeyCreator.getInstance();
			//创建编码生成对象
			CodeCreator codeCreator = CodeCreator.getInstance();
			
			//取来源数据
			DaoParam srcParam = _dao.createParam(srcSql);
			srcParam.setUseParse(true);
			srcParam.setDsName(dsName);
			srcParam.addStringValue(selKeyId);
			List<Map<String, String>> srcListData = _dao.query(srcParam);
			
			//解析目标SQL中的常量
			destSql = parseConstant(destSql, userInfo);
			//新增记录
			for (int i = 0, n = srcListData.size(); i < n; i++) {
				Map<String, String> mpValue = srcListData.get(i);
				String newKeyID = "";
				//是否新增主键
				boolean isNewKeyId = (destSql.indexOf(NEW_KEYID) >= 0);
				if (isNewKeyId) {
					newKeyID = keyCreator.createKey(tableName);
					if (newKeyID == null || newKeyID.length() == 0) {
						//新增记录时生成的主键值为空！
						_log.showWarn("execute update sql: new keyid is null!");
						return false;
					}
					destSql = destSql.replaceFirst(NEW_KEYID_REGEX, addChar(newKeyID));
				}
				
				//是否新增编号
				boolean isNewCode = (destSql.indexOf(NEW_CODE) >= 0);
				if (isNewCode) {
					String newCode = codeCreator.createCode(destFunId);
					destSql = destSql.replaceFirst(NEW_CODE_REGEX, addChar(newCode));
				}
				
				//执行SQL语句
				_log.showDebug("------------parsed sql=" + destSql);
				DaoParam param = _dao.createParam(destSql);
				param.setUseParse(true);
				param.setDsName(dsName);
				//根据定义的参数顺序赋值
				for (int k = 0, p = lsParam.size(); k < p; k++) {
					Map<String, String> mpParam = lsParam.get(k);
					param.addType(mpParam.get("param_type"));
					String paramName = mpParam.get("param_name");
					String paramValue = mpValue.get(paramName);
					param.addValue(paramValue);
				}
				
				if (!_dao.update(param)) {
					_log.showWarn("execute update faild!");
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 根据来源功能ID与目标功能ID查找路由条件ID
	 * @param srcFunId -- 来源功能ID
	 * @param destFunId -- 目标功能ID
	 * @return
	 */
	public String queryRoute(String srcFunId, String destFunId) {
		String sql = "select route_id from fun_rule_route where src_funid = ? and fun_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(srcFunId);
		param.addStringValue(destFunId);
		Map<String, String> mp = _dao.queryMap(param);
		if (mp.isEmpty()) return "";
		
		return mp.get("route_id");
	}
	
	/**
	 * 根据路由条件与目标功能ID查找导入SQL定义
	 * @param routeId -- 路由条件ID
	 * @param destFunId -- 目标功能ID
	 * @return
	 */
	public Map<String, String> queryRule(String routeId, String destFunId) {
		String sql = "select rule_id, src_sql, dest_sql from fun_rule_sql " +
				"where route_id = ? and dest_funid = ? and event_code like '%,import,%'";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(routeId);
		param.addStringValue(destFunId);
		return _dao.queryMap(param);
	}
	
	/**
	 * 根据来源功能ID查找SQL定义
	 * @param funId -- 触发事件的功能ID
	 * @param eventCode -- 事件代码
	 * @return
	 */
	public List<Map<String, String>> queryUpdateRule(String funId, String eventCode) {
		String sql = "select rule_id, src_sql, dest_sql, dest_funid from fun_rule_sql " +
				"where src_funid = ? and event_code like '%,"+eventCode+",%'";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		return _dao.query(param);
	}
	
	/**
	 * 查找SQL规则的参数
	 * @param ruleId -- 规则SQL ID
	 * @return
	 */
	private List<Map<String, String>> queryRuleParam(String ruleId) {
		String sql = "select param_name, param_type from fun_rule_param " +
				"where rule_id = ? order by param_no";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(ruleId);
		return _dao.query(param);
	}
	
	/**
	 * 解析SQL中的常量值。
	 * @param sql
	 * @param userInfo
	 * @return
	 */
	private String parseConstant(String sql, 
			Map<String,String> userInfo) {
		String regex = "\\{[^}]+\\}";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sql);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String tag = m.group();
			//取常量的值
			String value = (String) ControlerUtil.getConstantParam(tag, userInfo);
			//如果还含{，说明没有解析
			if (value.indexOf("{") >= 0) {
				m.appendReplacement(sb, value);
			} else {
				m.appendReplacement(sb, addChar(value));
			}
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
	/**
	 * 字符串两头加上'
	 * @param str
	 * @return
	 */
	private String addChar(String str) {
		StringBuilder sb = new StringBuilder();
		sb.append("'").append(str).append("'");
		
		return sb.toString();
	}
}
