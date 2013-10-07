/*
 * Copyright(c) 2013 Donghong Inc.
 */
package org.jxstar.service.studio;

import java.util.List;
import java.util.Map;

import org.jxstar.service.BusinessObject;
import org.jxstar.util.ArrayUtil;

/**
 * 批量显示图片，查询图片附件信息的类。
 *
 * @author TonyTan
 * @version 1.0, 2013-6-19
 */
public class AttachPicBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 查找当前选择记录的图片附件信息
	 * @param dataIds -- 记录ID
	 * @param tableName -- 当前表名
	 * @param userId -- 当前用户ID
	 * @return
	 */
	public String queryPic(String[] dataIds, String tableName, String userId) {
		if ((dataIds == null || dataIds.length == 0)) {
			_log.showDebug("not select data!!");
			return _returnFaild;
		}
		
		List<Map<String,String>> lsData = queryAttach(dataIds, tableName);
		
		String json = queryJson(lsData, userId);
		_log.showDebug("..........json=" + json);
		setReturnData(json);
		
		return _returnSuccess;
	}
	
	/**
	 * 构建图片路径的JSON对象
	 * @param lsData
	 * @return
	 */
	private String queryJson(List<Map<String,String>> lsData, String userId) {
		StringBuffer sbJson = new StringBuffer();
		
		if (lsData == null || lsData.isEmpty()) return "[]";
		
		for (int i = 0, n = lsData.size(); i < n; i++) {
			Map<String,String> mpData = lsData.get(i);
			
			String attach_id = mpData.get("attach_id");
			String attach_name = mpData.get("attach_name");
			
			sbJson.append("{url:'" + getUrl(attach_id, userId) + "',title:'" + attach_name + "',name:'"+ attach_id +"'},");
		}
		
		String json = "[" + sbJson.substring(0, sbJson.length() - 1) + "]";
		
		return json;
	}
	
	/**
	 * 取构建图片的URL
	 * @param attach_id
	 * @param userId
	 * @return
	 */
	private String getUrl(String attach_id, String userId) {
		StringBuilder sburl = new StringBuilder();
		
		sburl.append("./fileAction.do?funid=sys_attach&pagetype=editgrid&eventcode=down&dataType=byte&keyid=");
		sburl.append(attach_id);
		sburl.append("&user_id=");
		sburl.append(userId);
		
		return sburl.toString();
	}
	
	/**
	 * 根据记录ID查找图片附件
	 * @param dataIds
	 * @return
	 */
	private List<Map<String,String>> queryAttach(String[] dataIds, String tableName) {
		String ins = ArrayUtil.arrayToString(dataIds, "','");
		
		String sql = "select attach_id, attach_name from sys_attach where data_id in " +
				"('"+ ins.substring(0, ins.length()-1) +") " +
				"and table_name = '"+ tableName +"' and content_type like 'image%'";
		_log.showDebug("...............query attach sql=" + sql);
		
		return _dao.query(_dao.createParam(sql));
	}
}
