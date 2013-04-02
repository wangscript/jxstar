/*
 * Copyright(c) 2013 Donghong Inc.
 */
package org.jxstar.service.query;

import java.util.List;
import java.util.Map;

import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.EventDefine;
import org.jxstar.service.rule.RuleUtil;
import org.jxstar.util.ArrayUtil;

/**
 * 事件执行内容查询类：查看此事件将执行哪些类、哪些反馈SQL；
 *
 * @author TonyTan
 * @version 1.0, 2013-3-2
 */
public class EventInfoQuery extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 执行指定功能的指定事件的执行内容：
	 * 1、按顺序先检查执行类；
	 * 2、如果遇到了SqlRuleBO，则检查是否定义了反馈SQL，如果有则给标记
	 * @param funId -- 功能ID
	 * @param eventCode -- 事件代号
	 * @return
	 */
	public String queryInfo(String funId, String eventCode) {
		List<Map<String,String>> lsInvoke = null;
		
		//Map中有字段：invoke_id, module_name, method_name, position, issys, hassql, invoke_index, fun_id, event_code
		if (eventCode.equals("audit") || eventCode.equals("process_3")) {
			String checkFunId = funId;
			lsInvoke = EventDefine.getAuditModule(funId, eventCode, checkFunId);
		} else {
			lsInvoke = EventDefine.getEventModule(funId, eventCode);
		}
		
		int cnt = lsInvoke.size();
		for (int i = 0; i < cnt; i++) {
			Map<String, String> mp = lsInvoke.get(i);
			//是否有反馈SQL定义
			mp.put("hassql", "0");
			if (mp.get("module_name").indexOf(".SqlRuleBO") >= 0) {
				RuleUtil ru = new RuleUtil();
				List<Map<String, String>> lsRule = ru.queryUpdateRule(funId, eventCode);
				if (!lsRule.isEmpty()) {
					mp.put("hassql", "1");
				}
			}
			
			//添加执行类的序号
			String index = Integer.toString(i+1);
			mp.put("invoke_index", index);
			mp.put("fun_id", funId);
			mp.put("event_code", eventCode);
		}
		
		String strJson = ArrayUtil.listToJson(lsInvoke);
		StringBuilder sbJson = new StringBuilder("{total:"+cnt+",root:"+ strJson +",sum:[]}");
		_log.showDebug("json=" + sbJson.toString());
		
		//返回查询数据
		setReturnData(sbJson.toString());
		
		return _returnSuccess;
	}
}
