/*
 * FieldCfgBO.java 2010-12-21
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.studio;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.dm.DmException;
import org.jxstar.dm.util.DmConfig;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.StringUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 字段配置信息处理类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-21
 */
public class FieldCfgBO extends BusinessObject {
	private static final long serialVersionUID = -8331379837338361547L;
	
	/**
	 * 如果有新导入的记录，则修改表记录状态
	 * @param tableId
	 * @return
	 */
	public String modifyTable(String tableId) {
		String tableState = DmConfig.getTableCfgState(tableId);
		//如果表的状态为“完成”，添加新的字段后，表的状态该为修改
		if (tableState.equals("6")) {
			if (!DmConfig.updateTableState(tableId, "2")) {
				//"表配置信息状态改为“修改”时出错！"
				setMessage(JsMessage.getValue("fieldcfgbo.statemod"));
				return _returnFaild;
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 根据表名与字段名查询字段信息
	 * @param tableName -- 表名
	 * @param fieldName -- 字段名
	 * @return
	 */
	public String queryFieldInfo(String tableName, String fieldName) {
		String sql = "select field_memo, field_name, field_title, data_type, data_size, table_name " +
					 "from dm_field, dm_table " +
					 "where dm_field.table_id = dm_table.table_id and " +
					 "dm_table.table_name = ? and dm_field.field_name = ?";

		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableName);
		param.addStringValue(fieldName);
		Map<String,String> mpField = _dao.queryMap(param);
		
		StringBuilder fieldInfo = new StringBuilder();
		if (!mpField.isEmpty()) {
			String field_memo = mpField.get("field_memo");
			String field_name = mpField.get("field_name");
			String field_title = mpField.get("field_title");
			String data_type = mpField.get("data_type");
			String data_size = mpField.get("data_size");
			String table_name = mpField.get("table_name");
			//"字段："
			fieldInfo.append(JsMessage.getValue("fieldcfgbo.field") + 
					field_title + " " + table_name + "." + field_name);
			fieldInfo.append(" " + data_type + "[" + data_size + "]<br>");
			//"说明："
			fieldInfo.append(JsMessage.getValue("fieldcfgbo.text") + field_memo);
		}
		String info = fieldInfo.toString();
		info = StringUtil.strForJson(info);
		_log.showDebug("--------" + info);
		setReturnData("{info:'"+ info +"'}");
		
		return _returnSuccess;
	}
	
	/**
	 * 修改复制后新增记录的状态为“新增”
	 * @param fieldIds -- 复制后新增的记录ID
	 * @return
	 */
	public String postCopy(String[] fieldIds) {
		if (fieldIds == null || fieldIds.length == 0) {
			//"复制后新增的记录ID为空！"
			setMessage(JsMessage.getValue("fieldcfgbo.newidnull"));
			return _returnFaild;
		}
		_log.showDebug("--------------------postcopy=" + fieldIds[0]);
		
		for (int i = 0, n = fieldIds.length; i < n; i++) {
			//修改配置表状态为新建
			try {
				DmConfig.updateFieldState(fieldIds[i], "1");
			} catch (DmException e) {
				_log.showError(e);//"修改状态出错："
				setMessage(JsMessage.getValue("fieldcfgbo.updateerror") + e.getMessage());
				return _returnFaild;
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 字段信息保存后事件：
	 * 如果字段状态为“完成”，则修改字段状态为“修改”；
	 * 如果表的状态为“完成”，则修改表状态为“修改”；
	 * 
	 * @param fieldIds -- 字段配置ID组
	 * @return
	 */
	public String postSave(String[] fieldIds) {
		if (fieldIds == null || fieldIds.length == 0) {
			//"保存的字段记录键值为空！"
			setMessage(JsMessage.getValue("fieldcfgbo.keynull"));
			return _returnFaild;
		}
		
		for (int i = 0, n = fieldIds.length; i < n; i++) {
			Map<String, String> mpField = DmConfig.getMapField(fieldIds[i]);
		
			String fieldId = mpField.get("field_id");
			String fieldState = mpField.get("state");
			String tableId = mpField.get("table_id");
			
			if (fieldState.equals("6")) {
				try {
					DmConfig.updateFieldState(fieldId, "2");
				} catch (DmException e) {
					_log.showError(e);//"修改字段出错！"
					setMessage(JsMessage.getValue("fieldcfgbo.fielderror"));
					return _returnFaild;
				}
				//form页面修改时需要
				setReturnData("{'dm_fieldcfg__state':'2'}");
			}
		
			String tableState = DmConfig.getTableCfgState(tableId);
			//如果表的状态为“完成”，添加新的字段后，表的状态该为修改
			if (tableState.equals("6")) {
				if (!DmConfig.updateTableState(tableId, "2")) {
					//"表配置信息状态改为“修改”时出错！"
					setMessage(JsMessage.getValue("fieldcfgbo.statemod"));
					return _returnFaild;
				}
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 字段信息删除事件：
	 * 如果表状态为“新建”，则直接删除字段信息，结束事件；
	 * 如果表状态为“完成”，则修改表状态为“修改”；
	 * 如果字段状态为“新建”，则直接删除字段信息，否则修改状态为“删除”；
	 * 
	 * @param fieldIds -- 字段配置ID组
	 * @param tableId -- 表配置ID
	 * @return
	 */
	public String deleteCfg(String[] fieldIds) {
		if (fieldIds == null || fieldIds.length == 0) {
			//"保存的字段记录键值为空！"
			setMessage(JsMessage.getValue("fieldcfgbo.keynull"));
			return _returnFaild;
		}
		
		for (int i = 0, n = fieldIds.length; i < n; i++) {
			Map<String, String> mpField = DmConfig.getMapField(fieldIds[i]);
			
			String tableId = mpField.get("table_id");
			
			String fieldId = mpField.get("field_id");
			String fieldName = mpField.get("field_name");
			String fieldState = mpField.get("state");
			String fieldType = mpField.get("field_type");
			
			//索引字段、系统字段不能删除
			if (DmConfig.isIndexField(tableId, fieldName)) {
				//"字段【{0}】已建立了索引，不能删除！"
				setMessage(JsMessage.getValue("fieldcfgbo.hasindex"), fieldName);
				return _returnFaild;
			}
			if (fieldType.equals("1")) {
				//"字段【{0}】是系统字段，不能删除！"
				setMessage(JsMessage.getValue("fieldcfgbo.sysfield"), fieldName);
				return _returnFaild;
			}
			
			//字段状态为新建时，直接删除字段记录
			if (fieldState.equals("1")) {
				try {
					DmConfig.deleteCfg("dm_fieldcfg", "field_id", fieldId);
				} catch (DmException e) {
					_log.showError(e);//"删除字段信息时出错！"
					setMessage(JsMessage.getValue("fieldcfgbo.delerror"));
					return _returnFaild;
				}
			} else {
				String tableState = DmConfig.getTableCfgState(tableId);
				
				//如果表的状态为“完成”，删除字段后，表的状态该为“修改”
				if (tableState.equals("6")) {
					if (!DmConfig.updateTableState(tableId, "2")) {
						//"表配置信息状态改为“修改”时出错！"
						setMessage(JsMessage.getValue("fieldcfgbo.statemod"));
						return _returnFaild;
					}
				}
		
				//修改字段状态为“删除”
				try {
					DmConfig.updateFieldState(fieldId, "3");
				} catch (DmException e) {
					_log.showError(e);//"修改字段配置信息状态改为“删除”时出错！"
					setMessage(JsMessage.getValue("fieldcfgbo.statedel"));
					return _returnFaild;
				}
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 同步配置表与正式表中的表配置信息：
	 * 如果状态是新增，则直接添加记录；
	 * 如果状态是修改，则删除原记录，添加新记录；
	 * 如果状态是删除，则删除原记录与新记录.
	 * @param tableId -- 表配置ID
	 * @return
	 */
	public boolean updateFieldCfg(String tableId) throws DmException {
		boolean bret = true;
		
		List<Map<String,String>> lsField = DmConfig.getFieldCfg(tableId, "state in ('1', '2', '3')");
		for (int i = 0, n = lsField.size(); i < n; i++) {
			Map<String,String> mpField = lsField.get(i);
			String state = mpField.get("state");
			String fieldId = mpField.get("field_id");

			if (state.equals("1")) {			//复制记录到正式表中
				bret = copyFieldCfg(fieldId, true);
				if (!bret) return false;
				bret = DmConfig.updateFieldState(fieldId, "6");
			} else if (state.equals("2")) {		//先删除正式表中的记录，再复制记录
				bret = DmConfig.deleteCfg("dm_field", "field_id", fieldId);			
				if (!bret) return false;
				bret = copyFieldCfg(fieldId, true);
				if (!bret) return false;
				bret = DmConfig.updateFieldState(fieldId, "6");
			} else if (state.equals("3")) {		//先删除正式表中的记录，再删除配置表中的记录
				bret = DmConfig.deleteCfg("dm_field", "field_id", fieldId);			
				if (!bret) return false;
				bret = DmConfig.deleteCfg("dm_fieldcfg", "field_id", fieldId);
			} 
		}
		
		return bret;
	}
	
	/**
	 * 回滚字段状态
	 * @param tableId -- 表配置ID
	 * @return
	 */
	public boolean rollbackFieldCfg(String tableId) throws DmException {
		boolean bret = true;
		
		List<Map<String,String>> lsField = DmConfig.getFieldCfg(tableId, "state in ('1', '2', '3')");
		for (int i = 0, n = lsField.size(); i < n; i++) {
			Map<String,String> mpField = lsField.get(i);
			String state = mpField.get("state");
			String fieldId = mpField.get("field_id");
			_log.showDebug("--------rollback fieldId=" + fieldId + " state=" + state);
			
			if (state.equals("1")) {			//删除新增的字段配置
				bret = DmConfig.deleteCfg("dm_fieldcfg", "field_id", fieldId);
			} else if (state.equals("2")) {		//先删除配置表中的记录，再复制原记录
				bret = DmConfig.deleteCfg("dm_fieldcfg", "field_id", fieldId);			
				if (!bret) return false;
				bret = copyFieldCfg(fieldId, false);
				if (!bret) return false;
				bret = DmConfig.updateFieldState(fieldId, "6");
			} else if (state.equals("3")) {		//修改状态为“完成”
				bret = DmConfig.updateFieldState(fieldId, "6");
			} 
		}
		
		return bret;
	}
	
	/**
	 * 复制配置表的记录到正式表中
	 * @param fieldId -- 字段配置ID
	 * @param isCommit -- 是否提交
	 * @param isAll -- 是否复制全表记录，如果为是，则fieldId的值是表配置ID
	 * @return
	 */
	public boolean copyFieldCfg(String fieldId, boolean isCommit, boolean isAll) throws DmException {
		String toTable = "dm_field";
		String formTable = "dm_fieldcfg";
		if (!isCommit) {
			toTable = "dm_fieldcfg";
			formTable = "dm_field";
		}
		
		StringBuilder sbInsert = new StringBuilder();
		sbInsert.append("insert into "+ toTable +"( ");
		sbInsert.append("field_id, field_name, field_title, data_type, data_size, data_scale, default_value, nullable, field_type,");
		sbInsert.append("field_index, table_id, state, field_memo, add_userid, add_date, modify_userid, modify_date)");
		sbInsert.append("select ");
		sbInsert.append("field_id, field_name, field_title, data_type, data_size, data_scale, default_value, nullable, field_type,");
		sbInsert.append("field_index, table_id, '6', field_memo, add_userid, add_date, modify_userid, modify_date ");
		sbInsert.append("from "+ formTable);
		if (isAll) {
			sbInsert.append(" where table_id = ?");
		} else {
			sbInsert.append(" where field_id = ?");
		}
		
		DaoParam param = _dao.createParam(sbInsert.toString());
		param.addStringValue(fieldId);
		
		boolean ret = _dao.update(param);
		if (!ret) {//"复制【{0}】字段配置信息到正式表出错！"
			throw new DmException(JsMessage.getValue("copyerror"), fieldId);
		}
		return true;
	}
	public boolean copyFieldCfg(String fieldId, boolean isCommit) throws DmException {
		return copyFieldCfg(fieldId, isCommit, false);
	}
}
