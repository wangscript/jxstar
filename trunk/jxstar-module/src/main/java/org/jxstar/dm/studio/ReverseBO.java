/*
 * ReverseBO.java 2010-12-23
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.studio;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.dm.DmException;
import org.jxstar.dm.DmFactory;
import org.jxstar.dm.MetaData;
import org.jxstar.dm.util.DmConfig;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.key.KeyCreator;
import org.jxstar.util.resource.JsMessage;

/**
 * 反向生成配置信息处理类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-23
 */
public class ReverseBO extends BusinessObject {
	private static final long serialVersionUID = 8674130598412220967L;
	//当前操作人员
	private String _userid = "";
	//缺省数据源
	private String _dsname = "default";
	//主键构建器
	private KeyCreator _keyCreator = KeyCreator.getInstance();
	//元数据对象
	private MetaData _metaData = null;
	//系统字段列表
	private List<Map<String,String>> _sysField = null;
	
	/**
	 * 提交反向生成的配置信息，修改状态为完成，复制一份到正式表中。
	 * @param tableIds -- 表数组
	 * @return
	 */
	public String commitCfg(String[] tableIds) {		
		if (tableIds == null || tableIds.length == 0) {
			//"选择提交反向生成的表名组为空！"
			setMessage(JsMessage.getValue("reversebo.tablenull"));
			return _returnFaild;
		}
		
		for (int i = 0, n = tableIds.length; i < n; i++) {
			try {
				updateTableCfg(tableIds[i]);
			} catch (DmException e) {
				_log.showError(e);
				setMessage(e.getMessage());
				return _returnFaild;
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 复制配置信息到正式表中
	 * @param tableId -- 表配置ID
	 * @return
	 * @throws DmException
	 */
	private boolean updateTableCfg(String tableId) throws DmException {
		Map<String, String> mpTable = DmConfig.getTableOldCfg(tableId);
		if (!mpTable.isEmpty()) {
			//"正式表中已存在【{0}】表的配置信息，不能复制记录！"
			throw new DmException(JsMessage.getValue("reversebo.copyerror"), tableId);
		}
		
		//复制配置信息到正式表中
		(new TableCfgBO()).copyTableCfg(tableId, true);
		(new FieldCfgBO()).copyFieldCfg(tableId, true, true);
		(new IndexCfgBO()).copyIndexCfg(tableId, true, true);
		
		//修改记录状态为“完成”
		DmConfig.updateState("dm_tablecfg", tableId, "6");
		DmConfig.updateState("dm_fieldcfg", tableId, "6");
		DmConfig.updateState("dm_indexcfg", tableId, "6");
		
		return true;
	}
	
	/**
	 * 方向生成指定表的配置信息
	 * @param tableNames -- 表名
	 * @param userid -- 用户ID
	 * @return
	 */
	public String reverse(String[] tableNames, String userid) {
		if (tableNames == null || tableNames.length == 0) {
			//"选择需要反向生成的表名组为空！"
			setMessage(JsMessage.getValue("reversebo.backnull"));
			return _returnFaild;
		}
		//当前用户id
		_userid = userid;
		//取数据库信息解析对象
		_metaData = DmFactory.getMetaData(_dsname);
		//取所有没有配置的表对象
		List<Map<String,String>> lsTable = _metaData.getTableMeta(_dsname, true);
		
		for (int i = 0, n = tableNames.length; i < n; i++) {
			String tableName = tableNames[i];
			
			Map<String,String> mpTable = getTableMeta(tableName, lsTable);
			if (mpTable == null || mpTable.isEmpty()) {
				//"表【{0}】的元信息为空，不能反向生成配置信息！"
				setMessage(JsMessage.getValue("reversebo.metanull"), tableName);
				return _returnFaild;
			}
			
			//生成表配置信息
			String tableId = createTableCfg(mpTable);
			if (tableId == null || tableId.length() == 0) {
				//"反向生成【{0}】表配置信息出错！"
				setMessage(JsMessage.getValue("reversebo.tableerror"), tableName);
				return _returnFaild;
			}
			
			//生成字段配置信息
			if (!createFieldCfg(tableId, tableName)) {
				//"反向生成【{0}】表的字段配置信息出错！"
				setMessage(JsMessage.getValue("reversebo.fielderror"), tableName);
				return _returnFaild;
			}
			
			//生成索引配置信息
			if (!createIndexCfg(tableId, tableName)) {
				//"反向生成【{0}】表的索引配置信息出错！"
				setMessage(JsMessage.getValue("reversebo.indexerror"), tableName);
				return _returnFaild;
			}
		}
		
		return _returnSuccess;
	}

	/**
	 * 新增表配置信息
	 * @param mpTable -- 表信息
	 * @return
	 */
	private String createTableCfg(Map<String,String> mpTable) {
		StringBuilder sbInsert = new StringBuilder();
		sbInsert.append("insert into dm_tablecfg ( ");
		sbInsert.append("table_id, table_name, table_title, table_memo, table_space, key_field, ");
		sbInsert.append("state,  add_userid, add_date, ds_name, table_type) ");
		sbInsert.append("values (?, ?, ?, ?, ?, ?, '0', ?, ?, ?, '0') ");
		
		DaoParam param = _dao.createParam(sbInsert.toString());
		
		String tableId = _keyCreator.createKey("dm_tablecfg");
		String tableName = mpTable.get("table_name");
		String tableTitle = mpTable.get("table_title");
		String tableMemo = "";
		
		param.addStringValue(tableId);
		param.addStringValue(tableName);
		
		if (tableTitle.length() > 20) {
			tableMemo = tableTitle;
			tableTitle = "";
		}
		param.addStringValue(tableTitle);
		param.addStringValue(tableMemo);
		
		param.addStringValue(mpTable.get("table_space"));
		
		//设置主键字段
		String keyField = "";
		Map<String,String> mpKey = _metaData.getKeyMeta(tableName, _dsname);
		if (!mpKey.isEmpty()) {
			keyField = mpKey.get("key_field");
		}
		param.addStringValue(keyField);
		param.addStringValue(_userid);
		param.addDateValue(DateUtil.getTodaySec());
		param.addStringValue(_dsname);
		
		boolean bret = _dao.update(param);
		if (!bret) return null;
		
		return tableId;
	}
	
	/**
	 * 反向生成表的字段配置信息
	 * @param tableId -- 表配置ID
	 * @param tableName -- 表名
	 * @return
	 */
	private boolean createFieldCfg(String tableId, String tableName) {
		List<Map<String,String>> lsField = _metaData.getFieldMeta(tableName, _dsname);
		
		for (int i = 0, n = lsField.size(); i < n; i++) {
			Map<String,String> mpField = lsField.get(i);
			
			if (!oneFieldCfg(tableId, mpField)) return false;
		}
		
		return true;
	}
	
	/**
	 * 新增一条字段记录
	 * @param tableId -- 表配置ID
	 * @param mpField -- 字段配置信息
	 * @return
	 */
	private boolean oneFieldCfg(String tableId, Map<String,String> mpField) {
		StringBuilder sbInsert = new StringBuilder();
		sbInsert.append("insert into dm_fieldcfg ( ");
		sbInsert.append("field_id, field_name, field_title, field_memo, data_type, data_size, ");
		sbInsert.append("data_scale, default_value, field_type, nullable, ");
		sbInsert.append("field_index, table_id, state, add_userid, add_date) ");
		sbInsert.append("values(?, ?, ?, ?, ?, ?,  ?, ?, ?, ?,  ?, ?, '0', ?, ?)");
		
		DaoParam param = _dao.createParam(sbInsert.toString());
		
		String fieldId = _keyCreator.createKey("dm_fieldcfg");
		String fieldName = mpField.get("field_name");
		String fieldTitle = mpField.get("field_title");
		String fieldMemo = "";
		
		param.addStringValue(fieldId);
		param.addStringValue(fieldName);
		//如果字段标题长度大于20个汉字时，保存到备注中
		if (fieldTitle.length() > 20) {
			fieldMemo = fieldTitle;
			fieldTitle = "";
		}
		param.addStringValue(fieldTitle);
		param.addStringValue(fieldMemo);
		
		//处理字段长度与小数位
		String dataSize = mpField.get("data_size");
		String dataScale = MapUtil.getValue(mpField, "data_scale");
		
		//取数据类型，需要转换
		String dataType =mpField.get("data_type");
		dataType = _metaData.getDataType(dataType);
		
		//如果是number类型，且dataScale=0，则是int类型
		if (dataType.equals("number") && dataScale.equals("0")) {
			dataType = "int";
		}
		param.addStringValue(dataType);
		
		param.addStringValue(dataSize);
		param.addStringValue(dataScale);
		
		//取缺省值
		String defaultValue = MapUtil.getValue(mpField, "default_value");
		param.addStringValue(defaultValue);
		
		//取字段类别
		String fieldType = isSystemField(fieldName) ? "1" : "0";
		param.addStringValue(fieldType);
		
		param.addStringValue(mpField.get("nullable"));
		param.addStringValue(mpField.get("column_id"));
		param.addStringValue(tableId);
		param.addStringValue(_userid);
		param.addDateValue(DateUtil.getTodaySec());
		
		return _dao.update(param);
	}
	
	/**
	 * 反向生成表的索引配置信息
	 * @param tableId -- 表配置ID
	 * @param tableName -- 表名
	 * @return
	 */
	private boolean createIndexCfg(String tableId, String tableName) {
		List<Map<String,String>> lsIndex = _metaData.getIndexMeta(tableName, _dsname);
		
		for (int i = 0, n = lsIndex.size(); i < n; i++) {
			Map<String,String> mpIndex = lsIndex.get(i);
			
			if (!oneIndexCfg(tableId, mpIndex)) return false;
		}
		
		return true;
	}
	
	/**
	 * 生成索引配置信息
	 * @param tableId -- 表配置ID
	 * @param mpIndex -- 索引信息
	 * @return
	 */
	private boolean oneIndexCfg(String tableId, Map<String,String> mpIndex) {
		StringBuilder sbInsert = new StringBuilder();
		sbInsert.append("insert into dm_indexcfg ( ");
		sbInsert.append("index_id, index_name, isunique, index_field, ");
		sbInsert.append("table_id, state, add_userid, add_date) ");
		sbInsert.append("values(?, ?, ?, ?, ?, '0', ?, ?)");
		
		DaoParam param = _dao.createParam(sbInsert.toString());
		
		String indexId = _keyCreator.createKey("dm_indexcfg");
		//存在索引字段为空的情况，忽略
		String indexField = mpIndex.get("index_field");
		if (indexField == null || indexField.trim().length() == 0) {
			return true;
		}
		
		param.addStringValue(indexId);
		param.addStringValue(mpIndex.get("index_name"));
		param.addStringValue(mpIndex.get("isunique"));
		param.addStringValue(indexField);
		param.addStringValue(tableId);
		param.addStringValue(_userid);
		param.addDateValue(DateUtil.getTodaySec());
		
		return _dao.update(param);
	}
	
	/**
	 * 取表的元信息
	 * @param tableName -- 表名
	 * @param lsTable -- 所有表信息
	 * @return
	 */
	private Map<String,String> getTableMeta(String tableName, List<Map<String,String>> lsTable) {
		for (int i = 0, n = lsTable.size(); i < n; i++) {
			Map<String,String> mpTable = lsTable.get(i);
			
			String name = mpTable.get("table_name");
			if (tableName.equals(name)) {
				return mpTable;
			}
		}
		
		return null;
	}
	
	/**
	 * 检查字段是否是系统字段
	 * @param fieldName -- 字段名
	 * @return
	 */
	private boolean isSystemField(String fieldName) {
		if (_sysField == null || _sysField.isEmpty()) {
			String sql = "select * from dm_fieldcfg where table_id in " +
					"(select table_id from dm_tablecfg where table_name = 'sys_field') ";
			_sysField = _dao.query(_dao.createParam(sql));
		} else {
			for (int i = 0, n = _sysField.size(); i < n; i++) {
				String name = _sysField.get(i).get("field_name");
				if (name.equals(fieldName)) return true;
			}
		}
		
		return false;
	}
}
