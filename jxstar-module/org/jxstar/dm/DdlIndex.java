/*
 * IndexOracle.java 2010-12-18
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm;

import java.util.List;
import java.util.Map;

import org.jxstar.dm.util.DmConfig;
import org.jxstar.util.MapUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 数据库索引对象处理类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-18
 */
public class DdlIndex {
	//模板解析对象
	protected DmParser _parser = null;
	
	/**
	 * 构建索引配置对象
	 * @param parser
	 */
	public DdlIndex(DmParser parser) {
		_parser = parser;
	}
	
	/**
	 * 构建主键创建的SQL
	 * @param mpTable -- 当前表配置
	 * @return
	 * @throws DmException
	 */
	public String buildKey(Map<String,String> mpTable) throws DmException {
		if (mpTable == null || mpTable.isEmpty()) {
			//"表的配置信息为空！"
			throw new DmException(JsMessage.getValue("ddlindex.notable"));
		}

		return _parser.parseTemplet("create_primary_key", mpTable);
	}
	
	/**
	 * 构建索引创建的SQL
	 * @param mpTable -- 当前表配置
	 * @return
	 * @throws DmException
	 */
	public List<String> buildIndexs(Map<String,String> mpTable) throws DmException {
		List<String> lssql = FactoryUtil.newList();
		
		if (mpTable == null || mpTable.isEmpty()) {
			throw new DmException(JsMessage.getValue("ddlindex.notable"));
		}
		String tableId = mpTable.get("table_id");
		String tableName = mpTable.get("table_name");
		String tableSpace = mpTable.get("table_space");
		
		List<Map<String,String>> lsIndex = DmConfig.getIndexCfg(tableId);
		if (lsIndex.isEmpty()) {
			return lssql;
		}
		
		for (int i = 0, n = lsIndex.size(); i < n; i++) {
			Map<String,String> mpIndex = lsIndex.get(i);
			
			//添加表名与表空间
			mpIndex.put("table_name", tableName);
			mpIndex.put("table_space", tableSpace);
			
			//是否唯一索引
			String isunique = MapUtil.getValue(mpIndex, "isunique");
			
			if (isunique.equals("1")) {
				lssql.add(_parser.parseTemplet("create_index_unique", mpIndex));
			} else {
				lssql.add(_parser.parseTemplet("create_index", mpIndex));
			}
		}
		
		return lssql;
	}
	
	/**
	 * 构建索引修改的SQL
	 * @param mpTable -- 当前表配置
	 * @return
	 * @throws DmException
	 */
	public List<String> compareIndexs(Map<String,String> mpTable) throws DmException {
		List<String> lssql = FactoryUtil.newList();
		
		//取当前表配置
		String tableId = mpTable.get("table_id");
		String tableName = mpTable.get("table_name");
		String tableSpace = mpTable.get("table_space");
		
		//取新增与修改的索引配置信息
		List<Map<String,String>> lsIndex = DmConfig.getIndexCfg(tableId, "state in ('1', '2')");
		
		//取原表的索引配置信息
		List<Map<String,String>> lsIndexOld = DmConfig.getIndexOldCfg(tableId);
		
		//取新增、修改索引的SQL
		for (int i = 0, n = lsIndex.size(); i < n; i++) {
			Map<String,String> mpIndex = lsIndex.get(i);
			
			//添加表名、表空间
			mpIndex.put("table_name", tableName);
			mpIndex.put("table_space", tableSpace);
			
			//取一个字段的更新语句
			lssql.addAll(compareOneIndex(mpIndex, lsIndexOld));
		}
		
		//取删除的索引
		lsIndex = DmConfig.getIndexCfg(tableId, "state = '3'");
		
		//取删除索引的SQL	
		for (int i = 0, n = lsIndex.size(); i < n; i++) {
			Map<String,String> mpIndex = lsIndex.get(i);
			
			//添加表名、表空间
			mpIndex.put("table_name", tableName);
			mpIndex.put("table_space", tableSpace);
			
			//是否唯一索引
			String isunique = MapUtil.getValue(mpIndex, "isunique");
			//取删除索引的SQL
			if (isunique.equals("1")) {
				lssql.add(_parser.parseTemplet("drop_index_unique", mpIndex));
			} else {
				lssql.add(_parser.parseTemplet("drop_index", mpIndex));
			}
		}
		
		return lssql;
	}
	
	/**
	 * 比较一个索引与原配置信息差异的SQL
	 * @param mpIndex -- 当前索引信息
	 * @param lsIndexOld -- 原索引信息表
	 * @return
	 * @throws DmException
	 */
	protected List<String> compareOneIndex(Map<String,String> mpIndex, 
						List<Map<String,String>> lsIndexOld) throws DmException {
		List<String> lssql = FactoryUtil.newList();
		
		String indexId = mpIndex.get("index_id");
		
		Map<String,String> mpIndexOld = getOldIndex(indexId, lsIndexOld);
		
		//如果为空，说明是新增的索引
		if (mpIndexOld == null) {
			//是否唯一索引
			String isunique = mpIndex.get("isunique");
			//取创建索引的SQL
			if (isunique.equals("1")) {
				lssql.add(_parser.parseTemplet("create_index_unique", mpIndex));
			} else {
				lssql.add(_parser.parseTemplet("create_index", mpIndex));
			}
		} else {
		//取修改索引的SQL
			//添加表名
			String tableName = mpIndex.get("table_name");
			mpIndexOld.put("table_name", tableName);
			
			lssql.addAll(indexModifySql(mpIndex, mpIndexOld));
		}
		
		return lssql;
	}
	
	/**
	 * 获取索引修改内容
	 * @param mpIndex -- 新索引内容
	 * @param mpIndexOld -- 原索引内容
	 * @return
	 */
	protected List<String> indexModifySql(Map<String,String> mpIndex, 
						Map<String,String> mpIndexOld) throws DmException {
		List<String> lssql = FactoryUtil.newList();
		
		String indexName = MapUtil.getValue(mpIndex, "index_name");
		String indexNameOld = MapUtil.getValue(mpIndexOld, "index_name");
		
		String indexField = MapUtil.getValue(mpIndex, "index_field");
		String indexFieldOld = MapUtil.getValue(mpIndexOld, "index_field");
		
		String isunique = MapUtil.getValue(mpIndex, "isunique");
		String isuniqueOld = MapUtil.getValue(mpIndexOld, "isunique");
		
		//判断索引名、索引字段、索引类型是否修改
		if (!indexName.equalsIgnoreCase(indexNameOld) ||
				!indexField.equalsIgnoreCase(indexFieldOld) ||
				!isunique.equalsIgnoreCase(isuniqueOld)) {
			//删除原索引
			if (isuniqueOld.equals("1")) {
				lssql.add(_parser.parseTemplet("drop_index_unique", mpIndexOld));
			} else {
				lssql.add(_parser.parseTemplet("drop_index", mpIndexOld));
			}

			//是否唯一索引
			//重新创建索引
			if (isunique.equals("1")) {
				lssql.add(_parser.parseTemplet("create_index_unique", mpIndex));
			} else {
				lssql.add(_parser.parseTemplet("create_index", mpIndex));
			}
		}
		
		return lssql;
	}
	
	/**
	 * 根据索引ID在原索引配置信息表取索引配置信息
	 * @param indexId -- 索引ID
	 * @param lsIndexOld -- 索引原配置信息
	 * @return
	 */
	private Map<String,String> getOldIndex(String indexId, List<Map<String,String>> lsIndexOld) {
		for (int i = 0, n = lsIndexOld.size(); i < n; i++) {
			Map<String,String> mpIndexOld = lsIndexOld.get(i);
			
			String indexOldId = mpIndexOld.get("index_id");
			if (indexOldId.equals(indexId)) {
				return mpIndexOld;
			}
		}
		
		return null;
	}
}
