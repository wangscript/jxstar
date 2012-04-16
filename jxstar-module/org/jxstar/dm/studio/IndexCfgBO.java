/*
 * IndexCfgBO.java 2010-12-21
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
import org.jxstar.util.MapUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 索引配置信息处理类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-21
 */
public class IndexCfgBO extends BusinessObject {
	private static final long serialVersionUID = 3313428831542559796L;

	/**
	 * 修改复制后新增记录的状态为“新增”
	 * @param indexIds -- 复制后新增的记录ID
	 * @return
	 */
	public String postCopy(String[] indexIds) {
		if (indexIds == null || indexIds.length == 0) {
			//"复制后新增的记录ID为空！"
			setMessage(JsMessage.getValue("fieldcfgbo.newidnull"));
			return _returnFaild;
		}
		_log.showDebug("--------------------postcopy=" + indexIds[0]);
		
		for (int i = 0, n = indexIds.length; i < n; i++) {
			//修改配置表状态为新建
			try {
				DmConfig.updateIndexState(indexIds[i], "1");
			} catch (DmException e) {
				_log.showError(e);//"修改状态出错："
				setMessage(JsMessage.getValue("fieldcfgbo.updateerror") + e.getMessage());
				return _returnFaild;
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 索引信息保存后事件：
	 * 如果索引状态为“完成”，则修改索引状态为“修改”；
	 * 如果表的状态为“完成”，则修改表状态为“修改”；
	 * 
	 * @param indexIds -- 索引配置ID组
	 * @return
	 */
	public String postSave(String[] indexIds) {
		if (indexIds == null || indexIds.length == 0) {
			//"保存的索引记录键值为空！"
			setMessage(JsMessage.getValue("indexcfgbo.keynull"));
			return _returnFaild;
		}
		
		for (int i = 0, n = indexIds.length; i < n; i++) {
			Map<String, String> mpIndex = DmConfig.getMapIndex(indexIds[i]);
		
			String tableId = mpIndex.get("table_id");
			
			String indexId = mpIndex.get("index_id");
			String indexState = mpIndex.get("state");
			String indexField = mpIndex.get("index_field");
			
			//检查索引字段必须在配置表中已定义
			if (!checkIndexField(tableId, indexField)) {
				//"索引字段【{0}】定义不正确，部分在字段表找不到！"
				setMessage(JsMessage.getValue("indexcfgbo.indexerror"), indexField);
				return _returnFaild;
			}
			
			if (indexState.equals("6")) {
				try {
					DmConfig.updateIndexState(indexId, "2");
				} catch (DmException e) {
					_log.showError(e);//"修改索引出错！"
					setMessage(JsMessage.getValue("indexcfgbo.updateerror"));
					return _returnFaild;
				}
			}
		
			String tableState = DmConfig.getTableCfgState(tableId);
			//如果表的状态为“完成”，添加新的索引后，表的状态该为修改
			if (tableState.equals("6")) {
				if (!DmConfig.updateTableState(tableId, "2")) {
					//"表配置信息状态改为“修改”时出错！"
					setMessage(JsMessage.getValue("indexcfgbo.statemod"));
					return _returnFaild;
				}
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 索引信息删除事件：
	 * 如果表状态为“新建”，则直接删除索引信息，结束事件；
	 * 如果表状态为“完成”，则修改表状态为“修改”；
	 * 如果索引状态为“新建”，则直接删除索引信息，否则修改状态为“删除”；
	 * 
	 * @param indexIds -- 索引配置ID组
	 * @param tableId -- 表配置ID
	 * @return
	 */
	public String deleteCfg(String[] indexIds) {
		if (indexIds == null || indexIds.length == 0) {
			//"保存的索引记录键值为空！"
			setMessage(JsMessage.getValue("indexcfgbo.keynull"));
			return _returnFaild;
		}
		
		for (int i = 0, n = indexIds.length; i < n; i++) {
			Map<String, String> mpIndex = DmConfig.getMapIndex(indexIds[i]);
			
			String indexId = mpIndex.get("index_id");
			String indexState = mpIndex.get("state");
			String tableId = mpIndex.get("table_id");
			
			//索引状态为新建时，直接删除索引记录
			if (indexState.equals("1")) {
				try {
					DmConfig.deleteCfg("dm_indexcfg", "index_id", indexId);
				} catch (DmException e) {
					_log.showError(e);//"删除索引信息时出错！"
					setMessage(JsMessage.getValue("indexcfgbo.delerror"));
					return _returnFaild;
				}
			} else {
				String tableState = DmConfig.getTableCfgState(tableId);
				
				//如果表的状态为“完成”，删除索引后，表的状态该为“修改”
				if (tableState.equals("6")) {
					if (!DmConfig.updateTableState(tableId, "2")) {
						//"表配置信息状态改为“修改”时出错！"
						setMessage(JsMessage.getValue("indexcfgbo.statemod"));
						return _returnFaild;
					}
				}
		
				//修改索引状态为“删除”
				try {
					DmConfig.updateIndexState(indexId, "3");
				} catch (DmException e) {
					_log.showError(e);//"修改索引配置信息状态改为“删除”时出错！"
					setMessage(JsMessage.getValue("indexcfgbo.statedel"));
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
	public boolean updateIndexCfg(String tableId) throws DmException {
		boolean bret = true;
		
		List<Map<String,String>> lsIndex = DmConfig.getIndexCfg(tableId, "state in ('1', '2', '3')");
		for (int i = 0, n = lsIndex.size(); i < n; i++) {
			Map<String,String> mpIndex = lsIndex.get(i);
			String state = mpIndex.get("state");
			String indexId = mpIndex.get("index_id");

			if (state.equals("1")) {			//复制记录到正式表中
				bret = copyIndexCfg(indexId, true);
				if (!bret) return false;
				bret = DmConfig.updateIndexState(indexId, "6");
			} else if (state.equals("2")) {		//先删除正式表中的记录，再复制记录
				bret = DmConfig.deleteCfg("dm_index", "index_id", indexId);
				if (!bret) return false;
				bret = copyIndexCfg(indexId, true);
				if (!bret) return false;
				bret = DmConfig.updateIndexState(indexId, "6");
			} else if (state.equals("3")) {		//先删除正式表中的记录，再删除配置表中的记录
				bret = DmConfig.deleteCfg("dm_index", "index_id", indexId);
				if (!bret) return false;
				bret = DmConfig.deleteCfg("dm_indexcfg", "index_id", indexId);
			} 
		}
		
		return bret;
	}
	
	/**
	 * 回滚索引配置状态
	 * @param tableId -- 表配置ID
	 * @return
	 */
	public boolean rollbackIndexCfg(String tableId) throws DmException {
		boolean bret = true;
		
		List<Map<String,String>> lsIndex = DmConfig.getIndexCfg(tableId, "state in ('1', '2', '3')");
		for (int i = 0, n = lsIndex.size(); i < n; i++) {
			Map<String,String> mpIndex = lsIndex.get(i);
			String state = mpIndex.get("state");
			String indexId = mpIndex.get("index_id");
			
			if (state.equals("1")) {			//删除新增的索引配置
				bret = DmConfig.deleteCfg("dm_indexcfg", "index_id", indexId);
			} else if (state.equals("2")) {		//先删除配置表中的记录，再复制原记录
				bret = DmConfig.deleteCfg("dm_indexcfg", "index_id", indexId);
				if (!bret) return false;
				bret = copyIndexCfg(indexId, false);
				if (!bret) return false;
				bret = DmConfig.updateIndexState(indexId, "6");
			} else if (state.equals("3")) {		//修改状态为“完成”
				bret = DmConfig.updateIndexState(indexId, "6");
			} 
		}
		
		return bret;
	}
	
	/**
	 * 复制配置表的记录到正式表中
	 * @param indexId -- 索引配置ID
	 * @param isCommit -- 是否提交
	 * @param isAll -- 是否复制全表记录，如果为是，则indexId的值是表配置ID
	 * @return
	 */
	public boolean copyIndexCfg(String indexId, boolean isCommit, boolean isAll) throws DmException {
		String toTable = "dm_index";
		String formTable = "dm_indexcfg";
		if (!isCommit) {
			toTable = "dm_indexcfg";
			formTable = "dm_index";
		}
		
		StringBuilder sbInsert = new StringBuilder();
		sbInsert.append("insert into "+ toTable +"( ");
		sbInsert.append("index_id, index_name, isunique, index_field, index_memo,");
		sbInsert.append("table_id, state, add_userid, add_date, modify_userid, modify_date)");
		sbInsert.append("select ");
		sbInsert.append("index_id, index_name, isunique, index_field, index_memo,");
		sbInsert.append("table_id, '6', add_userid, add_date, modify_userid, modify_date ");
		sbInsert.append("from "+ formTable);
		if (isAll) {
			sbInsert.append(" where table_id = ?");
		} else {
			sbInsert.append(" where index_id = ?");
		}
		
		DaoParam param = _dao.createParam(sbInsert.toString());
		param.addStringValue(indexId);
		
		boolean ret = _dao.update(param);
		if (!ret) {//"复制【{0}】索引配置信息到正式表出错！"
			throw new DmException(JsMessage.getValue("indexcfgbo.copyerror"), indexId);
		}
		return true;
	}
	public boolean copyIndexCfg(String indexId, boolean isCommit) throws DmException {
		return copyIndexCfg(indexId, isCommit, false);
	}
	
	/**
	 * 检查索引字段是否正确
	 * @param tableId -- 表配置ID
	 * @param indexField -- 索引字段，用,号分隔
	 * @return
	 */
	private boolean checkIndexField(String tableId, String indexField) {
		//取索引字段数量
		String[] fields = indexField.split(",");
		int fieldCnt = 0;
		for (int i = 0, n = fields.length; i < n; i++) {
			if (fields[i].trim().length() > 0) fieldCnt++;
		}
		
		String fieldWhere = "'" + indexField.replaceAll(",", "','") + "'";
		
		//在字段表中检查是否存在对应的字段
		String sql = "select count(*) as cnt from dm_fieldcfg where table_id = ? and field_name in (" + fieldWhere + ")";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableId);
		
		Map<String,String> mpCnt = _dao.queryMap(param);
		int cnt = MapUtil.hasRecodNum(mpCnt);
		
		return cnt == fieldCnt;
	}
}
