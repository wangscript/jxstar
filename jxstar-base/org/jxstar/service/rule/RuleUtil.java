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
import org.jxstar.util.MapUtil;
import org.jxstar.util.factory.FactoryUtil;
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
	 * @param mpRule -- 导入SQL定义信息
	 * @param srcKeyId -- 来源数据的主键值
	 * @param forKeyId -- 新增数据的外键值，如果是导入子表记录时需要
	 * @param userInfo -- 用户信息
	 * @return String 返回新增的记录主键值，用;分隔，如果返回false表示执行失败。
	 */
	public String exeInsert(Map<String, String> mpRule, 
			String srcKeyId, String forKeyId, Map<String,String> userInfo) {
		String faild = "false", retKeyId = "";
		
		String srcFunId = MapUtil.getValue(mpRule, "src_funid");
		String destFunId = MapUtil.getValue(mpRule, "dest_funid");
		
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
		_log.showDebug("------------src param=" + srcKeyId);
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
		String baseSql = new String(destSql);
		
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
			destSql = baseSql;//用于替换新的主键与编码
		}
		retKeyId = retKeyId.substring(0, retKeyId.length()-1);
		_log.showDebug("------------retKeyId=" + retKeyId);
		
		return retKeyId;
	}
	
	/**
	 * 执行定义的SQL语句，不返回主键值，用于反馈SQL定义与导入SQL中的非首条定义
	 * @param lsRule -- 需要执行反馈SQL
	 * @param selKeyId -- 选择的记录ID
	 * @param forKeyId -- 外键值，新增子表记录时需要
	 * @param userInfo -- 当前用户信息
	 * @return
	 */
	public boolean exeUpdate(List<Map<String, String>> lsRule, String selKeyId, 
			String forKeyId, Map<String,String> userInfo) {
		if (lsRule == null || selKeyId == null) {
			_log.showWarn("------------ext update param is null!");
			return true;
		}
		
		for (int j = 0, m = lsRule.size(); j < m; j++) {
			Map<String, String> mpRule = lsRule.get(j);
			
			String srcFunId = MapUtil.getValue(mpRule, "src_funid");
			String destFunId = MapUtil.getValue(mpRule, "dest_funid");
			
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
			_log.showDebug("------------src param=" + selKeyId);
			_log.showDebug("------------dest sql=" + destSql);
			
			//创建主键生成对象
			KeyCreator keyCreator = KeyCreator.getInstance();
			//创建编码生成对象
			CodeCreator codeCreator = CodeCreator.getInstance();
			
			//取来源数据
			DaoParam srcParam = _dao.createParam(srcSql);
			srcParam.setUseParse(true);
			srcParam.setDsName(srcDsName);
			srcParam.addStringValue(selKeyId);
			List<Map<String, String>> srcListData = _dao.query(srcParam);
			if (srcListData.isEmpty()) {
				_log.showWarn("execute update src data list is empty!");
				//不处理来源数据为空的情况
				continue;
			}
			
			//解析目标SQL中的常量
			destSql = parseConstant(destSql, userInfo);
			//解析目标SQL中的外键值
			destSql = destSql.replaceFirst(FKEYID_REGEX, addChar(forKeyId));
			String baseSql = new String(destSql);
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
				param.setDsName(destDsName);
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
				destSql = baseSql;//用于替换新的主键与编码
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
		String sql = "select route_id from fun_rule_route where (status = '0' or status is null) and src_funid = ? and fun_id = ?";
		
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
		String sql = "select rule_id, src_sql, dest_sql, dest_funid, src_funid from fun_rule_sql " +
			"where (status = '0' or status is null) and route_id = ? and dest_funid = ? and event_code like '%,import,%' order by sql_no";
		_log.showDebug("------------query first import sql=" + sql);
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(routeId);
		param.addStringValue(destFunId);
		return _dao.queryMap(param);
	}
	
	/**
	 * 取该路由条件下的其它SQL，第一条不要
	 * @return
	 */
	public List<Map<String, String>> queryOtherRule(String routeId) {
		List<Map<String, String>> lsRet = FactoryUtil.newList(); 
		String sql = "select rule_id, src_sql, dest_sql, dest_funid, src_funid from fun_rule_sql " +
			"where (status = '0' or status is null) and route_id = ? and event_code like '%,import,%' order by sql_no";
		_log.showDebug("------------query other import sql=" + sql);
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(routeId);
		List<Map<String, String>> lsQry = _dao.query(param);
		if (lsQry.isEmpty()) {
			return lsRet;
		} else {
			lsQry.remove(0);
			return lsQry;
		}
	}
	
	/**
	 * 根据来源功能ID查找SQL定义
	 * @param funId -- 触发事件的功能ID
	 * @param eventCode -- 事件代码
	 * @return
	 */
	public List<Map<String, String>> queryUpdateRule(String funId, String eventCode) {
		String sql = "select rule_id, src_sql, dest_sql, dest_funid, src_funid from fun_rule_sql " +
			"where (status = '0' or status is null) and src_funid = ? and event_code like '%,"+eventCode+",%' order by sql_no";
		_log.showDebug("------------query update sql=" + sql);
		
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
		if (str == null) str = "";
		
		StringBuilder sb = new StringBuilder();
		sb.append("'").append(str).append("'");
		
		return sb.toString();
	}
}
