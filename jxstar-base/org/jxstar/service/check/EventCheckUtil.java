/*
 * Copyright(c) 2013 Donghong Inc.
 */
package org.jxstar.service.check;

import java.util.List;
import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.control.ControlerUtil;
import org.jxstar.service.util.ConditionUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.log.Log;

/**
 * 检查项执行的辅助工具类。
 *
 * @author TonyTan
 * @version 1.0, 2013-3-28
 */
public class EventCheckUtil {
	private static BaseDao _dao = BaseDao.getInstance();
	private static Log _log = Log.getInstance();
	
	/**
	 * 执行检查项设置类
	 * @param keyId -- 主键值
	 * @param mpCheck -- 检查项设置
	 * @param request -- 请求上下文
	 * @return
	 */
	public static boolean exeCheckClass(String keyId, Map<String,String> mpCheck, RequestContext request) {
		//检查是否设置了类名与方法名
		String className = mpCheck.get("class_name");
		String methodName = mpCheck.get("method_name");
		String useRequest = MapUtil.getValue(mpCheck, "use_request", "0");
		if (className.length() == 0 || methodName.length() == 0) {
			String checkNo = mpCheck.get("check_no");
			request.setMessage("检查项【"+ checkNo +"】的类设置信息为空！");
			return false;
		}
		_log.showDebug("EventCheck execute ClassName={0}.{1}", className, methodName);
		
		Object[] params;
		if (useRequest.equals("0")) {
			params = new Object[]{keyId};
		} else {
			params = new Object[]{keyId, request};
		}
		
		if (! ControlerUtil.invoke(className, methodName, params, request)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 查询功能的检查项设置
	 * @param funId
	 * @param eventCode
	 * @return
	 */
	public static List<Map<String,String>> queryCheck(String funId, String eventCode) {
		String sql = "select check_id, check_no, check_name, check_type, set_type, faild_desc, check_ext, " +
		"class_name, method_name, use_request, src_sql, comp_type, dest_sql " +
		"from fun_check where (status = '0' or status is null) and fun_id = ? and event_code = ? " +
		"order by check_no";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		param.addStringValue(eventCode);
		
		return _dao.query(param);
	}
	
	/**
	 * 执行检查项设置SQL：
	 * 1、如果只有来源SQL，则来源SQL查询值为0则表示失败，非0表示成功；
	 * 2、如果值类型是SQL，则取SQL查询值，否则为常量值，如果常量值为纯数字，则按数字规则比较；
	 * 3、常量值中可以设置{CURUSERID}类型的会话常量值，需要判断解析；
	 * @param keyId -- 主键值
	 * @param mpCheck -- 检查项设置
	 * @param request -- 请求上下文
	 * @return
	 */
	public static boolean exeCheckSql(String keyId, Map<String,String> mpCheck, RequestContext request) {
		//检查是否设置了来源值、比较方法、目标值
		String srcSql = MapUtil.getValue(mpCheck, "src_sql").trim();
		String compType = MapUtil.getValue(mpCheck, "comp_type");
		String destSql = MapUtil.getValue(mpCheck, "dest_sql").trim();
		if (srcSql.length() == 0) {
			String checkNo = mpCheck.get("check_no");
			request.setMessage("检查项【"+ checkNo +"】的SQL设置信息为空！");
			return false;
		}
		
		Map<String,String> userInfo = request.getUserInfo();
		
		boolean ret = false;
		String destValue = "";
		String srcValue = parseCheckValue(srcSql, keyId, userInfo);
		
		if (destSql.length() > 0) {
			destValue = parseCheckValue(destSql, keyId, userInfo);
			
			//解析比较值的结果
			ret = parseCondition(srcValue, compType, destValue);
		} else {
			ret = !srcValue.equals("0");
		}
		
		if (!ret) {
			String json = "{srcvalue:'"+ srcValue +"', destvalue:'"+ destValue +"'}";
			request.setReturnData(json);
			return ret; 
		}
		
		return true;
	}
	
	/**
	 * 解析比较值
	 * @param srcSql -- 比较值设置
	 * @param keyId -- 主键值，用于查询数据
	 * @param userInfo -- 会话信息，用于解析常量
	 * @return
	 */
	private static String parseCheckValue(String srcSql, String keyId, Map<String,String> userInfo) {
		if (srcSql.length() == 0) {
			return "";
		}
		//有空格说明是SQL语句
		if (srcSql.indexOf(" ") > -1) {
			DaoParam param = _dao.createParam(srcSql);
			if (srcSql.indexOf('?') > -1) {
				param.addStringValue(keyId);
			}
			Map<String, String> mp = _dao.queryMap(param);
			if (!mp.isEmpty()) {
				return mp.values().iterator().next();
			}
		} else {
		//没有空格说明是常量，检查是否是会话常量
			if (srcSql.equals("{CURUSER}")) return "";
			return (String) ControlerUtil.getConstantParam(srcSql, userInfo);
		}
		
		return "";
	}
	
	/**
	 * 解析比较值的结果
	 * @param srcValue -- 来源值
	 * @param compType -- 比较类型
	 * @param destValue -- 目标值
	 * @return
	 */
	private static boolean parseCondition(String srcValue, String compType, String destValue) {
		//构建比较条件
		StringBuffer sbcond = new StringBuffer();
		if (StringUtil.isNum(srcValue)) {
			sbcond.append(srcValue);
		} else {
			sbcond.append("'" + srcValue + "'");
		}
		
		if (compType.indexOf("like") > -1) {
			sbcond.append(" like ");
		} else {
			sbcond.append(" " + compType + " ");
		}
		
		if (StringUtil.isNum(destValue)) {
			sbcond.append(destValue);
		} else {
			sbcond.append("'" + destValue + "'");
		}
		return ConditionUtil.validCondition(sbcond.toString());
	}
}
