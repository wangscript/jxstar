/*
 * TableCfgBO.java 2010-12-17
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.studio;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.dm.DdlTable;
import org.jxstar.dm.DmException;
import org.jxstar.dm.DmFactory;
import org.jxstar.dm.util.DmConfig;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.ArrayUtil;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.StringUtil;
import org.jxstar.util.key.KeyCreator;
import org.jxstar.util.resource.JsMessage;

/**
 * 表配置处理类：
 * 每个表都需要自动增加一些系统字段，这些字段定义在sys_field表中了；
 *
 * @author TonyTan
 * @version 1.0, 2010-12-17
 */
public class TableCfgBO extends BusinessObject {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 修改复制后新增记录的状态为“新增”
	 * @param tableIds -- 复制后新增的记录ID
	 * @return
	 */
	public String postCopy(String[] tableIds) {
		if (tableIds == null || tableIds.length == 0) {
			//"复制后新增的记录ID为空！"
			setMessage(JsMessage.getValue("fieldcfgbo.newidnull"));
			return _returnFaild;
		}
		
		for (int i = 0, n = tableIds.length; i < n; i++) {
			String tableId = tableIds[i];
			//修改配置表状态为新建
			try {
				DmConfig.updateState("dm_tablecfg", tableId, "1");
			
				DmConfig.updateState("dm_fieldcfg", tableId, "1");
			
				DmConfig.updateState("dm_indexcfg", tableId, "1");
			} catch (DmException e) {
				_log.showError(e);//"修改状态出错："
				setMessage(JsMessage.getValue("fieldcfgbo.updateerror") + e.getMessage());
				return _returnFaild;
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 表格保存后事件：
	 * 根据配置状态判断是新增还是修改，如果状态为“新建”表示是新增，否则表示是修改；
	 * 只有“完成”状态才可以修改为“修改”，删除后的记录不能再修改；
	 * @param tableIds -- 表键值组
	 * @param userId -- 当前用户
	 * @return
	 */
	public String postSave(String[] tableIds, String userId) {		
		if (tableIds == null || tableIds.length == 0) {
			//"提交的表记录键值为空！"
			setMessage(JsMessage.getValue("tablecfgbo.tablenull"));
			return _returnFaild;
		}
		
		for (int i = 0, n = tableIds.length; i < n; i++) {
			Map<String, String> mpTable = DmConfig.getTableCfg(tableIds[i]);
			if (mpTable.isEmpty()) {//"表配置【{0}】记录为空！"
				setMessage(JsMessage.getValue("tablecfgbo.tablerecnull"), tableIds[i]);
				return _returnFaild;
			}
			
			String tableId = mpTable.get("table_id");
			String tableName = mpTable.get("table_name");
			
			String state = mpTable.get("state");
			String keyField = mpTable.get("key_field");
			if (keyField.length() == 0) {//"表配置【{0}】的主键字段为空！"
				setMessage(JsMessage.getValue("tablecfgbo.tablekeynull"), tableName);
				return _returnFaild;
			}
		
			if (state.equals("1")) {
				//新增主键字段与系统字段
				String ret = postCreate(tableId, keyField, userId);
				if (ret.equals(_returnFaild)) return _returnFaild;
			} else if (state.equals("6")) {
				//表状态由“完成”改为“修改”
				if (!DmConfig.updateTableState(tableId, "2")) {
					//"修改表【{0}】为修改状态出错！"
					setMessage(JsMessage.getValue("tablecfgbo.updateerror"), tableName);
					return _returnFaild;
				}
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 删除表配置信息，替换通用删除事件：
	 * 判断表是否在功能定义中使用，如果有则不能删除；
	 * 如果表状态为“新建”，则删除表记录与明细记录，否则删除后修改状态为“删除”；
	 * @param tableIds -- 表键值组
	 * @return
	 */
	public String deleteCfg(String[] tableIds) {
		if (tableIds == null || tableIds.length == 0) {
			//"提交的表记录键值为空！"
			setMessage(JsMessage.getValue("tablecfgbo.tablenull"));
			return _returnFaild;
		}
		
		for (int i = 0, n = tableIds.length; i < n; i++) {
			Map<String, String> mpTable = DmConfig.getTableCfg(tableIds[i]);
			String tableId = mpTable.get("table_id");
			String tableName = mpTable.get("table_name");
			String state = mpTable.get("state");
			
			String tableType = mpTable.get("table_type");
			if (tableType.equals("5") || tableType.equals("6")) {
				//"表类型为系统表与特殊表的不能删除！"
				setMessage(JsMessage.getValue("tablecfgbo.sysdelno"));
				return _returnFaild;
			}
			
			//检查功能定义信息
			if (hasTableUse(tableName)) {
				//"表【{0}】在功能定义中已使用，不能删除！"
				setMessage(JsMessage.getValue("tablecfgbo.usedelno"), tableName);
				return _returnFaild;
			}
			
			if (state.equals("1")) {
				//新建时可以直接删除配置
				try {
					DmConfig.deleteCfg("dm_fieldcfg", tableId);
					
					DmConfig.deleteCfg("dm_indexcfg", tableId);
					
					DmConfig.deleteCfg("dm_tablecfg", tableId);
				} catch (DmException e) {
					_log.showError(e);//"删除配置信息时出错："
					setMessage(JsMessage.getValue("tablecfgbo.tabledel") + e.getMessage());
					return _returnFaild;
				}
			} else {
				//修改配置表状态
				try {
					DmConfig.updateState("dm_tablecfg", tableId, "3");
				
					DmConfig.updateState("dm_fieldcfg", tableId, "3");
				
					DmConfig.updateState("dm_indexcfg", tableId, "3");
				} catch (DmException e) {
					_log.showError(e);//"修改状态出错："
					setMessage(JsMessage.getValue("fieldcfgbo.updateerror") + e.getMessage());
					return _returnFaild;
				}
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 批量提交配置信息
	 * @param tableIds -- 表键值组
	 * @return
	 */
	public String commitDDL(String[] tableIds) {
		if (tableIds == null || tableIds.length == 0) {
			//"提交的表记录键值为空！"
			setMessage(JsMessage.getValue("tablecfgbo.tablenull"));
			return _returnFaild;
		}
		
		for (int i = 0, n = tableIds.length; i < n; i++) {
			Map<String, String> mpTable = DmConfig.getTableCfg(tableIds[i]);
			if (mpTable.isEmpty()) {//"没有找到【{0}】表的配置信息！"
				setMessage(JsMessage.getValue("tablecfgbo.tablenocfg"), tableIds[i]);
				return _returnFaild;
			}
			
			String tableId = mpTable.get("table_id");
			String tableName = mpTable.get("table_name");
			String state = mpTable.get("state"); 
			String dsName = mpTable.get("ds_name"); 
			
			String sret = commitDDL(tableId, tableName,  state, dsName);
			if (sret.equals(_returnFaild)) {
				return _returnFaild;
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 批量取消配置信息修改
	 * @param tableIds -- 表键值组
	 * @return
	 */
	public String rollback(String[] tableIds) {
		if (tableIds == null || tableIds.length == 0) {
			//"取消的表记录键值为空！"
			setMessage(JsMessage.getValue("tablecfgbo.cancelnull"));
			return _returnFaild;
		}
		
		for (int i = 0, n = tableIds.length; i < n; i++) {
			Map<String, String> mpTable = DmConfig.getTableCfg(tableIds[i]);
			
			String tableId = mpTable.get("table_id");
			String state = mpTable.get("state");
			_log.showDebug("--------rollback tableid=" + tableId + " state=" + state);
			
			try {
				rollbackTableCfg(tableId, state);
			} catch (DmException e) {
				_log.showError(e);//"取消提交配置信息出错："
				setMessage(JsMessage.getValue("tablecfgbo.cancelerror") + e.getMessage());
				return _returnFaild;
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 提交数据库配置，先修改数据库对象，再提交配置信息，
	 * 存在问题：如果更新数据库成功，但提交配置信息失败，则不能回滚。
	 * @param tableId -- 表配置ID
	 * @param tableName -- 表名
	 * @param state -- 状态
	 * @param dsName -- 数据源
	 * @return
	 */
	public String commitDDL(String tableId, String tableName, String state, String dsName) {
		if (state == null || state.length() == 0) {
			//"表配置信息的状态为空，不能执行！"
			setMessage(JsMessage.getValue("tablecfgbo.tabledonot"));
			return _returnFaild;
		}
		
		if (tableName == null || tableName.length() == 0 ||
				dsName == null || dsName.length() == 0) {
			//"表配置信息的表名与数据源为空，不能执行！"
			setMessage(JsMessage.getValue("tablecfgbo.dbsrcnull"));
			return _returnFaild;
		}
		
		//字段为空则不能提交
		List<Map<String,String>> lsField = DmConfig.getFieldCfg(tableId);
		if (lsField.isEmpty()) {//"表【{0}】没有定义字段信息，不能提交！"
			setMessage(JsMessage.getValue("tablecfgbo.nofield"), tableName);
			return _returnFaild;
		}
		
		//取数据库解析对象
		DdlTable ddlTable = DmFactory.getDdlTable(dsName);
		if (ddlTable == null) {//"数据源【{0}】的数据库解析对象创建失败，请检查数据源名是否正确！"
			setMessage(JsMessage.getValue("tablecfgbo.newerror"), dsName);
			return _returnFaild;
		}
		
		//执行数据库操作
		boolean bret = false;
		try {
			if (state.equals("1")) {
				bret = ddlTable.create(tableId);	//新建
			} else if (state.equals("2")) {
				bret = ddlTable.modify(tableId);	//修改
			} else if (state.equals("3")) {
				bret = ddlTable.delete(tableId);	//删除
			} else {//"表配置信息的状态为：{0}，不需要执行！"
				setMessage(JsMessage.getValue("tablecfgbo.statedonot"), state);
				return _returnFaild;
			}
		} catch(DmException e) {//"执行数据库操作出错："
			setMessage(JsMessage.getValue("tablecfgbo.dbdoerror") + e.getMessage());
			return _returnFaild;
		}
		if (!bret) {//"执行数据库操作失败！"
			setMessage(JsMessage.getValue("tablecfgbo.dbdofaild"));
			return _returnFaild;
		}
		
		//修改配置表的状态为“完成”
		if (!DmConfig.updateTableState(tableId, "6")) {
			//"修改表配置信息状态为“完成”时出错！"
			setMessage(JsMessage.getValue("tablecfgbo.stateend"));
			return _returnFaild;
		}
		
		//同步正式表中的信息
		try {
			updateTableCfg(tableId, state);
		} catch (DmException e) {
			_log.showError(e);//"同步正式表中的配置信息时出错："
			setMessage(JsMessage.getValue("tablecfgbo.sncyerror") + e.getMessage());
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 检查配置信息是否正确，并返回生成的SQL
	 * @param tableId -- 表配置ID
	 * @param state -- 状态
	 * @param dsName -- 数据源
	 * @return
	 */
	public String checkConfig(String tableId, String state, String dsName) {
		//取数据库解析对象
		DdlTable ddlTable = DmFactory.getDdlTable(dsName);
		if (ddlTable == null) {//"数据源【{0}】的数据库解析对象创建失败，请检查数据源名是否正确！"
			setMessage(JsMessage.getValue("tablecfgbo.srcerror"), dsName);
			return _returnFaild;
		}
		
		//取执行SQL
		List<String> lssql = null;
		try {
			if (state.equals("1")) {
				lssql = ddlTable.getCreateSql(tableId);	//新建
			} else if (state.equals("2")) {
				lssql = ddlTable.getModifySql(tableId);	//修改
			} else if (state.equals("3")) {
				lssql = ddlTable.getDeleteSql(tableId);	//删除
			} else {//"表配置信息的状态为：{0}，不需要验证！"
				setMessage(JsMessage.getValue("tablecfgbo.statecheck"), state);
				return _returnFaild;
			}
		} catch(DmException e) {//"验证配置出错："
			setMessage(JsMessage.getValue("tablecfgbo.checkerror") + e.getMessage());
			return _returnFaild;
		}
		if (lssql == null || lssql.isEmpty()) {//"验证的SQL为空！"
			setMessage(JsMessage.getValue("tablecfgbo.checksql"));
			return _returnFaild;
		}
		
		//返回验证SQL到前台
		String sql = ArrayUtil.listToString(lssql, "");
		sql = StringUtil.strForJson(sql);
		setReturnData("{'sql':'" + sql + "'}");
		
		return _returnSuccess;
	}
	
	/**
	 * 新增表配置后需要处理的内容：
	 * 1、如果没有主键字段，需要自动创建主键字段；
	 * 2、如果配置了系统字段，需要自动添加系统字段；
	 * @param tableId -- 表配置ID
	 * @param keyField -- 主键字段
	 * @param userId -- 新增用户ID
	 * @return
	 */
	private String postCreate(String tableId, String keyField, String userId) {
		boolean bret = false;

		bret = addKeyField(tableId, keyField, userId);
		if (!bret) {//"添加主键字段出错！"
			setMessage(JsMessage.getValue("tablecfgbo.addkey"));
			return _returnFaild;
		}
		
		bret = insertSysField(tableId, userId);
		if (!bret) {//"添加常用系统字段出错！"
			setMessage(JsMessage.getValue("tablecfgbo.addfield"));
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 同步配置表与正式表中的表配置信息：
	 * 如果状态是新增，则直接添加记录；
	 * 如果状态是修改，则删除原记录，添加新记录；
	 * 如果状态是删除，则删除原记录与新记录.
	 * @param tableId -- 表配置ID
	 * @param state -- 表配置状态
	 * @return
	 */
	private boolean updateTableCfg(String tableId, String state) throws DmException {
		boolean bret = false;
		
		if (state.equals("1")) {			//复制记录到正式表中
			Map<String, String> mpTable = DmConfig.getTableOldCfg(tableId);
			if (!mpTable.isEmpty()) {//"正式表中已存在【{0}】表的配置信息，不能复制记录！"
				throw new DmException(JsMessage.getValue("tablecfgbo.nocopy"), tableId);
			}
			
			bret = copyTableCfg(tableId, true);
			if (!bret) return false;
			
			FieldCfgBO fieldcfg = new FieldCfgBO();
			bret = fieldcfg.updateFieldCfg(tableId);
			if (!bret) return false;
			
			IndexCfgBO indexcfg = new IndexCfgBO();
			bret = indexcfg.updateIndexCfg(tableId);
			if (!bret) return false;
		} else if (state.equals("2")) {		//先删除正式表中的记录，再复制记录
			FieldCfgBO fieldcfg = new FieldCfgBO();
			bret = fieldcfg.updateFieldCfg(tableId);
			if (!bret) return false;
			
			IndexCfgBO indexcfg = new IndexCfgBO();
			bret = indexcfg.updateIndexCfg(tableId);
			if (!bret) return false;
			
			bret = DmConfig.deleteCfg("dm_table", tableId);			
			if (!bret) return false;
			
			bret = copyTableCfg(tableId, true);
			if (!bret) return false;
		} else if (state.equals("3")) {		//先删除正式表中的记录，再删除配置表中的记录
			bret = DmConfig.deleteCfg("dm_field", tableId);
			if (!bret) return false;
			
			bret = DmConfig.deleteCfg("dm_fieldcfg", tableId);
			if (!bret) return false;
			
			bret = DmConfig.deleteCfg("dm_index", tableId);
			if (!bret) return false;
			
			bret = DmConfig.deleteCfg("dm_indexcfg", tableId);
			if (!bret) return false;
			
			bret = DmConfig.deleteCfg("dm_table", tableId);
			if (!bret) return false;
			
			bret = DmConfig.deleteCfg("dm_tablecfg", tableId);
			if (!bret) return false;
		} 
		
		return bret;
	}
	
	/**
	 * 取消修改的配置信息：
	 * 如果状态是新增，则直接删除这些记录；
	 * 如果状态是修改，则删除新记录，添加原记录；
	 * 如果状态是删除，则修改状态为完成.
	 * @param tableId -- 表配置ID
	 * @param state -- 表配置状态
	 * @return
	 */
	private boolean rollbackTableCfg(String tableId, String state) throws DmException {
		boolean bret = true;
		
		if (state.equals("1")) {			//删除新增的表配置与相关明细配置
			bret = DmConfig.deleteCfg("dm_fieldcfg", tableId);
			if (!bret) return false;
			
			bret = DmConfig.deleteCfg("dm_indexcfg", tableId);
			if (!bret) return false;
			
			bret = DmConfig.deleteCfg("dm_tablecfg", tableId);
		} else if (state.equals("2")) {		//删除新记录，添加原记录
			FieldCfgBO fieldcfg = new FieldCfgBO();
			bret = fieldcfg.rollbackFieldCfg(tableId);
			if (!bret) return false;
			
			IndexCfgBO indexcfg = new IndexCfgBO();
			bret = indexcfg.rollbackIndexCfg(tableId);
			if (!bret) return false;
			
			bret = DmConfig.deleteCfg("dm_tablecfg", tableId);			
			if (!bret) return false;
			
			bret = copyTableCfg(tableId, false);
			if (!bret) return false;
		} else if (state.equals("3")) {		//修改状态为完成
			DmConfig.updateState("dm_tablecfg", tableId, "6");
		
			DmConfig.updateState("dm_fieldcfg", tableId, "6");
		
			DmConfig.updateState("dm_indexcfg", tableId, "6");
		}
		
		return bret;
	}
	
	/**
	 * 复制配置表的记录到正式表中
	 * @param tableId -- 表配置ID
	 * @param isCommit -- 是否提交
	 * @return
	 */
	public boolean copyTableCfg(String tableId, boolean isCommit) throws DmException {
		String toTable = "dm_table";
		String formTable = "dm_tablecfg";
		if (!isCommit) {
			toTable = "dm_tablecfg";
			formTable = "dm_table";
		}
		
		StringBuilder sbInsert = new StringBuilder();
		sbInsert.append("insert into "+ toTable +"( ");
		sbInsert.append("table_id, table_name, table_title, table_memo, table_space, key_field,");
		sbInsert.append("state,  add_userid, add_date, modify_userid, modify_date, ds_name, table_type)");
		sbInsert.append("select ");
		sbInsert.append("table_id, table_name, table_title, table_memo, table_space, key_field,");
		sbInsert.append("'6',  add_userid, add_date, modify_userid, modify_date, ds_name, table_type ");
		sbInsert.append("from "+ formTable +" where table_id = ?");
		
		DaoParam param = _dao.createParam(sbInsert.toString());
		param.addStringValue(tableId);
		
		boolean ret = _dao.update(param);
		if (!ret) {//"复制【{0}】表配置信息到正式表出错！"
			throw new DmException(JsMessage.getValue("tablecfgbo.copyerror"), tableId);
		}
		return true;
	}
	
	/**
	 * 添加主键字段
	 * @param tableId -- 表配置ID
	 * @param fieldName -- 主键字段名
	 * @param userId -- 新增用户ID
	 * @return
	 */
	private boolean addKeyField(String tableId, String fieldName, String userId) {
		//先检查主键字段是否存在
		if (hasKeyField(tableId, fieldName)) {
			_log.showDebug("key field [{0}] is exist, not add...", fieldName);
			return true;
		}
		
		//新建主键字段
		return insertKeyField(tableId, fieldName, userId);
	}
	
	/**
	 * 是否有主键字段
	 * @param tableId -- 表配置ID
	 * @param fieldName -- 主键字段名
	 * @return
	 */
	private boolean hasKeyField(String tableId, String fieldName) {
		String sql = "select count(*) as cnt from dm_fieldcfg where table_id = ? and field_name = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableId);
		param.addStringValue(fieldName);
		
		Map<String,String> mpCnt = _dao.queryMap(param);
		return MapUtil.hasRecord(mpCnt);
	}
	
	/**
	 * 表是否有使用
	 * @param tableName -- 表名
	 * @return
	 */
	private boolean hasTableUse(String tableName) {
		String sql = "select count(*) from fun_base where table_name = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableName);
		
		Map<String,String> mpCnt = _dao.queryMap(param);
		return MapUtil.hasRecord(mpCnt);
	}
	
	/**
	 * 新增主键记录
	 * @param tableId -- 表配置ID
	 * @param fieldName -- 主键字段名
	 * @param userId -- 新增用户ID
	 * @return
	 */
	private boolean insertKeyField(String tableId, String fieldName, String userId) {
		StringBuilder sbInsert = new StringBuilder();
		sbInsert.append("insert into dm_fieldcfg(");
		sbInsert.append("field_id, field_name, field_title, data_type, data_size, ");
		sbInsert.append("nullable, field_type, field_index, table_id, state, add_userid, add_date) ");
		sbInsert.append("values(?, ?, '主键', 'varchar', 25, ");
		sbInsert.append("1, 0, 500, ?, 1, ?, ?)");
		
		DaoParam param = _dao.createParam(sbInsert.toString());
		String fieldId = KeyCreator.getInstance().createKey("dm_fieldcfg");
		param.addStringValue(fieldId);
		param.addStringValue(fieldName);
		param.addStringValue(tableId);
		param.addStringValue(userId);
		param.addDateValue(DateUtil.getTodaySec());
		
		return _dao.update(param);
	}
	
	/**
	 * 添加常用系统字段，如：add_userid, add_date, modify_userid, modify_date
	 * @param tableId -- 表配置ID
	 * @param userId -- 新增用户ID
	 * @return
	 */
	private boolean insertSysField(String tableId, String userId) {
		//设置的系统字段
		StringBuilder sbSelect = new StringBuilder();
		sbSelect.append("select field_id, field_name, field_title, data_type, data_size, data_scale, default_value, ");
		sbSelect.append("nullable, field_type, field_index, table_id, state from dm_fieldcfg where table_id in (");
		sbSelect.append("select table_id from dm_tablecfg where table_name = 'sys_field') and ");
		sbSelect.append("field_name not in (select field_name from dm_fieldcfg where table_id = ?) ");
		DaoParam param = _dao.createParam(sbSelect.toString());
		param.addStringValue(tableId);
		
		List<Map<String,String>> lsSysField = _dao.query(param);
		if (lsSysField.isEmpty()) {
			_log.showDebug("system field is empty, not add...");
			return true;
		}
		
		//新增系统字段
		StringBuilder sbInsert = new StringBuilder();
		sbInsert.append("insert into dm_fieldcfg(");
		sbInsert.append("field_id, field_name, field_title, data_type, data_size, data_scale, default_value, ");
		sbInsert.append("nullable, field_type, field_index, table_id, state, add_userid, add_date) ");
		sbInsert.append("values(?, ?, ?, ?, ?, ?, ?, ");
		sbInsert.append("?, ?, ?, ?, ?, ?, ?)");
		
		DaoParam iparam = _dao.createParam(sbInsert.toString());
		for (int i = 0, n = lsSysField.size(); i < n; i++) {
			Map<String,String> mpField = lsSysField.get(i);
			
			String fieldId = KeyCreator.getInstance().createKey("dm_fieldcfg");
			iparam.addStringValue(fieldId);
			iparam.addStringValue(mpField.get("field_name"));
			iparam.addStringValue(mpField.get("field_title"));
			iparam.addStringValue(mpField.get("data_type"));
			iparam.addIntValue(mpField.get("data_size"));
			iparam.addIntValue(mpField.get("data_scale"));
			iparam.addStringValue(mpField.get("default_value"));
			iparam.addStringValue(mpField.get("nullable"));
			iparam.addStringValue(mpField.get("field_type"));
			iparam.addIntValue(mpField.get("field_index"));
			iparam.addStringValue(tableId);
			iparam.addStringValue("1");
			iparam.addStringValue(userId);
			iparam.addDateValue(DateUtil.getTodaySec());
			
			if (!_dao.update(iparam)) {
				return false;
			}
			
			iparam.clearParam();
		}
		
		return true;
	}
}
