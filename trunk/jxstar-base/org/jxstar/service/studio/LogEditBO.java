/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.studio;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jxstar.control.action.RequestContext;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.DefineDataManger;
import org.jxstar.service.util.ServiceUtil;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.key.KeyCreator;
import org.jxstar.util.resource.JsParam;

/**
 * 记录数据修改日志，增加系统变量：sys.log.edit 
 * 设置修改内容字段值长度为1000，超过长度的不处理。
 *
 * @author TonyTan
 * @version 1.0, 2012-11-19
 */
public class LogEditBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	protected DefineDataManger _define = DefineDataManger.getInstance();
	
	/**
	 * 处理表单修改内容，保留痕迹
	 * @param requestContext
	 * @return
	 */
	public String formSave(RequestContext request) {
		//是否启用数据修改日志
		String logEdit = SystemVar.getValue("sys.log.edit", "0");
		//是否只启用审批中数据修改日志
		String editwf = SystemVar.getValue("sys.log.editwf", "0");
		if (!logEdit.equals("1") && !editwf.equals("1")) return _returnSuccess;
		
		String funid = request.getRequestValue("funid");
		//启用数据修改日志的功能ID，用,,分隔
		String logFunIds = SystemVar.getValue("sys.log.edit.funid");
		if (logFunIds.length() > 0) {
			//如果当前功能ID不在设置范围内，则不处理修改日志
			if (logFunIds.indexOf(","+funid+",") < 0) {
				return _returnSuccess;
			}
		}
		
		_log.showDebug("................formSave method starting!");
		Map<String,String> mpFun = _define.getFunData(funid);
		String tableName = mpFun.get("table_name");
		String funName = mpFun.get("fun_name");
		String pkCol = mpFun.get("pk_col");
		String pkValue = request.getRequestValue(JsParam.KEYID);
		Map<String,String> mpUser = request.getUserInfo();
		String userId = MapUtil.getValue(mpUser, "user_id");
		String userName = MapUtil.getValue(mpUser, "user_name");
		
		//如果没有当前用户的任务分配消息，则不处理修改数据留痕
		if (editwf.equals("1") && !hasAssign(funid, pkValue, userId)) {
			_log.showDebug("................formSave method is not in checking!");
			return _returnSuccess;
		}
		
		//取修改了数据的字段名与字段标题
		String dirtyFields = request.getRequestValue("dirtyfields");
		if (dirtyFields.length() == 0) {
			_log.showDebug("................formSave method param: dirtyFields is empty!");
			return _returnSuccess;
		}
		Map<String,String> fieldTitles = queryFieldTitle(funid, dirtyFields);
		if (fieldTitles.isEmpty()) {
			_log.showDebug("................formSave method param: fieldTitles is empty!");
			return _returnSuccess;
		}
		
		//取修改前的数据
		Map<String,String> mpOld = queryOldData(tableName, dirtyFields, pkCol, pkValue);
		
		//取修改后的数据
		Map<String,String> mpValue = ServiceUtil.getDirtyData(request.getRequestMap(), tableName);
		String modifyCont = getModifyContent(fieldTitles, mpOld, mpValue);
		_log.showDebug("..............formSave content: " + modifyCont);
		
		//把修改内容写入审批意见表中
		//新增数据编辑日志
		insertEditLog(funid, funName, "", "", "保存", pkValue, userId, userName, modifyCont);
		
		return _returnSuccess;
	}
	
	/**
	 * 处理子表格修改内容，保留修改痕迹
	 * @param request
	 * @return
	 */
	public String gridSave(RequestContext request) {
		String logEdit = SystemVar.getValue("sys.log.edit", "0");
		String editwf = SystemVar.getValue("sys.log.editwf", "0");
		if (!logEdit.equals("1") && !editwf.equals("1")) return _returnSuccess;
		
		_log.showDebug("................gridSave method starting!");
		String funid = request.getRequestValue("funid");
		Map<String,String> mpFun = _define.getFunData(funid);
		String tableName = mpFun.get("table_name");
		String funName = mpFun.get("fun_name");
		String pkCol = mpFun.get("pk_col");
		Map<String,String> mpUser = request.getUserInfo();
		String userId = MapUtil.getValue(mpUser, "user_id");
		String userName = MapUtil.getValue(mpUser, "user_name");
		String parentFunId = request.getRequestValue("pfunid");//主功能ID
		String parentDataId = request.getRequestValue("fkValue");//主记录ID
		String pagetype = request.getRequestValue("pagetype");//页面类型：subeditgrid、editgrid
		
		//如果没有当前用户的任务分配消息，则不处理修改数据留痕
		if (editwf.equals("1") && pagetype.equals("subeditgrid") && 
				!hasAssign(parentFunId, parentDataId, userId)) {
			_log.showDebug("................gridSave method is not in checking!");
			return _returnSuccess;
		}
		
		//取修改了数据的字段名与字段标题
		Map<String,String> titles = queryAllTitle(funid, tableName);
		String[] cols = new String[titles.size()];
		cols = titles.keySet().toArray(cols);
		
		//取值数组对象
		List<Map<String,String>> lsVals = ServiceUtil.getRequestMaps(
				request, cols);
		
		//grid可以编辑多条数据，分条处理，新增记录不处理
		for (Map<String,String> mpValue : lsVals) {
			String pkValue = MapUtil.getValue(mpValue, pkCol);
			
			if (pkValue.length() == 0) continue;
			//如果是主表数据保存，则需要单条判断
			if (editwf.equals("1") && pagetype.equals("editgrid")  && 
					!hasAssign(funid, pkValue, userId)) {
				_log.showDebug("................gridSave method is not in checking!");
				return _returnSuccess;
			}
			
			//取修改前的数据
			Map<String,String> mpOld = queryOldData(tableName, "*", pkCol, pkValue);
			
			String modifyCont = getModifyContent(titles, mpOld, mpValue);
			_log.showDebug("..............gridSave content: " + modifyCont);
			
			//新增数据编辑日志
			insertEditLog(funid, funName, parentFunId, parentDataId, 
					"保存", pkValue, userId, userName, modifyCont);
		}
		
		return _returnSuccess;
	}
	
	//取修改了的字段标题
	private Map<String,String> queryFieldTitle(String funid, String dirtyFields) {
		Map<String,String> mpTitle = FactoryUtil.newMap();
		
		String[] fields = dirtyFields.split(";");
		
		String sql = "select col_code, col_name from fun_col where fun_id = ? order by col_index";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funid);
		List<Map<String,String>> lsCol = _dao.query(param);
		
		for (String field : fields) {
			for (Map<String,String> mpCol : lsCol) {
				String colcode = mpCol.get("col_code");
			
				if (colcode.equals(field)) {
					colcode = StringUtil.getNoTableCol(colcode);
					mpTitle.put(colcode, mpCol.get("col_name"));
					break;
				}
			}
		}
		
		return mpTitle;
	}
	
	//取当前表的字段标题
	private Map<String,String> queryAllTitle(String funid, String tableName) {
		Map<String,String> mpTitle = FactoryUtil.newMap();

		String sql = "select col_code, col_name from fun_col where fun_id = ? and col_code like ? order by col_index";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funid);
		param.addStringValue(tableName+".%");
		List<Map<String,String>> lsCol = _dao.query(param);
		
		for (Map<String,String> mpCol : lsCol) {
			String colcode = mpCol.get("col_code");
		
			mpTitle.put(colcode, mpCol.get("col_name"));
		}
		
		return mpTitle;
	}
	
	//取修改前的数据记录
	private Map<String,String> queryOldData(String tableName, String fields, 
			String pkCol, String pkValue) {
		//把字段分隔符改为,
		if (!fields.equals("*")) fields = fields.replaceAll(";", ",");
		
		StringBuilder sbsql = new StringBuilder();
		sbsql.append("select ").append(fields).append(" from ")
			.append(tableName).append(" where ").append(pkCol).append(" = ? ");
		
		DaoParam param = _dao.createParam(sbsql.toString());
		param.addStringValue(pkValue);
		return _dao.queryMap(param);
	}
	
	//构建修改内容
	private String getModifyContent(Map<String,String> fieldTitles, 
			Map<String,String> mpOld, Map<String,String> mpValue) {
		StringBuilder sbcont = new StringBuilder();
		
		Iterator<String> fields = fieldTitles.keySet().iterator();
		while (fields.hasNext()) {
			String field = fields.next();
			
			String oldValue = MapUtil.getValue(mpOld, StringUtil.getNoTableCol(field));
			String newValue = MapUtil.getValue(mpValue, field);
			
			if (!oldValue.equals(newValue)) {
				String title = fieldTitles.get(field);
				sbcont.append(title).append("【" + oldValue + "】改为【"+ newValue +"】\n");
			}
		}
		
		return sbcont.toString();
	}
	
	//当前操作是否在审批中
	private boolean hasAssign(String funId, String dataId, String userId) {
		String sql = "select count(*) as cnt from wf_assign where fun_id = ? and data_id = ? and assign_userid = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		param.addStringValue(dataId);
		param.addStringValue(userId);
		
		Map<String,String> mp = _dao.queryMap(param);
		return MapUtil.hasRecord(mp);
	}
	
	//新增数据编辑日志
	private boolean insertEditLog(String funId, String funName, 
			String parentFunId, String parentDataId, 
			String eventName, String dataId, String userId,
			String userName, String editDesc) {
		if (editDesc.getBytes().length > 1000) {
			_log.showDebug("................modify data too long!!");
			return true;
		}
		
		String editId = KeyCreator.getInstance().createKey("sys_log_edit");
		
		String sql = "insert into sys_log_edit(edit_id, fun_id, fun_name, pfun_id, pdata_id, event_name, " +
				"data_id, user_id, user_name, edit_desc, edit_date, add_userid, add_date) " +
				"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(editId);
		param.addStringValue(funId);
		param.addStringValue(funName);
		param.addStringValue(parentFunId);
		param.addStringValue(parentDataId);
		param.addStringValue(eventName);
		param.addStringValue(dataId);
		param.addStringValue(userId);
		param.addStringValue(userName);
		param.addStringValue(editDesc);
		param.addDateValue(DateUtil.getTodaySec());
		param.addStringValue(userId);
		param.addDateValue(DateUtil.getTodaySec());
		
		return _dao.update(param);
	}
}
