/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.studio;

import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.DmDao;
import org.jxstar.dao.JsonDao;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 处理统计方案数据。
 *
 * @author TonyTan
 * @version 1.0, 2012-4-18
 */
public class StatCaseBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 保存统计方案数据，如果ID为空则新增加记录，否则保存；
	 * 明细记录先删除再保存；
	 * @param funId -- 功能ID
	 * @param statId -- 方案ID
	 * @param statName -- 方案名称
	 * @param chars -- 分组字段
	 * @param charTitles -- 分组字段标题
	 * @param nums -- 统计字段
	 * @param numTitles -- 统计字段标题
	 * @return
	 */
	public String saveCase(RequestContext req) {
		String funId = req.getRequestValue("statfunid");
		String statId = req.getRequestValue("keyid");
		String statName = req.getRequestValue("statname");
		Map<String,String> user = req.getUserInfo();
		String chars = req.getRequestValue("chars");
		String charTitles = req.getRequestValue("chartitles"); 
		String nums = req.getRequestValue("nums");
		String numTitles = req.getRequestValue("numtitles");
		
		String[] achars = chars.split(",");
		String[] acharTitles = charTitles.split(",");
		String[] anums = nums.split(",");
		String[] anumTitles = numTitles.split(",");
		
		if (statId.length() == 0) {
			statId = createCase(funId, statName, user, achars, acharTitles, anums, anumTitles);
		} else {
			saveCase(statId, achars, acharTitles, anums, anumTitles);
		}
		
		setReturnData("{keyid:'"+ statId +"'}");
		
		return _returnSuccess;
	}
	
	/**
	 * 取统计方案字段
	 * @param statId
	 * @return
	 */
	public String clickCase(String statId) {
		StringBuilder sbJson = new StringBuilder("{");
		
		String chars = getColJson(statId, "0");
		sbJson.append("chars:" + chars + ",");
		
		String nums = getColJson(statId, "1");
		sbJson.append("nums:" + nums + "}");
		
		this.setReturnData(sbJson.toString());
		
		return _returnSuccess;
	}
	
	//取统计字段
	private String getColJson(String statId, String type) {
		JsonDao jd = JsonDao.getInstance();
		String sql = "select col_code, col_title from sys_stat_det where stat_id = ? and prop_type = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(statId);
		param.addStringValue(type);
		String json = jd.query(param, new String[]{"colcode", "colname"});
		
		return "["+ json +"]";
	}
	
	//新增方案与明细
	private String createCase(String funId, String statName, 
			Map<String,String> user,
			String[] chars, String[] charTitles, 
			String[] nums, String[] numTitles) {
		Map<String,String> mp = FactoryUtil.newMap();
		mp.put("stat_name", statName);
		mp.put("fun_id", funId);
		mp.put("user_id", user.get("user_id"));
		mp.put("user_name", user.get("user_name"));
		mp.put("is_share", "1");
		
		String statId = DmDao.insert("sys_stat", mp);
		
		for (int i = 0, n = chars.length; i < n; i++) {
			saveDet(statId, chars[i], charTitles[i], "0", i);
		}
		
		for (int i = 0, n = nums.length; i < n; i++) {
			saveDet(statId, nums[i], numTitles[i], "1", i);
		}
		
		return statId;
	}
	
	//保存方案与明细，先删除再新增明细
	private boolean saveCase(String statId,
			String[] chars, String[] charTitles, 
			String[] nums, String[] numTitles) {
		deleteDet(statId);
		
		for (int i = 0, n = chars.length; i < n; i++) {
			saveDet(statId, chars[i], charTitles[i], "0", i);
		}
		
		for (int i = 0, n = nums.length; i < n; i++) {
			saveDet(statId, nums[i], numTitles[i], "1", i);
		}
		
		return true;
	}
	
	//保存方案明细
	private void saveDet(String statId, String colcode, String coltitle, String type, int colno) {
		Map<String,String> mp = FactoryUtil.newMap();
		mp.put("stat_id", statId);
		mp.put("col_code", colcode);
		mp.put("col_title", coltitle);
		mp.put("prop_type", type);
		mp.put("col_no", Integer.toString(colno));
		
		DmDao.insert("sys_stat_det", mp);
	}
	
	//删除方案明细
	private boolean deleteDet(String statId) {
		String sql = "delete from sys_stat_det where stat_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(statId);
		return _dao.update(param);
	}
}
