/*
 * FieldOracle.java 2010-12-17
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
 * 数据库字段对象处理类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-17
 */
public class DdlField {
	//模板解析对象
	protected DmParser _parser = null;
	
	/**
	 * 构建字段配置对象
	 * @param parser
	 */
	public DdlField(DmParser parser) {
		_parser = parser;
	}
	
	/**
	 * 解析字段标题定义语句
	 * @param mpTable -- 表信息
	 * @return
	 * @throws DmException
	 */
	public List<String> buildFieldTitles(Map<String,String> mpTable) throws DmException {
		//取当前表配置
		String tableId = mpTable.get("table_id");
		String tableName = mpTable.get("table_name");
		
		List<Map<String,String>> lsField = DmConfig.getFieldCfg(tableId);
		if (lsField.isEmpty()) {
			//"没有找到【{0}】表的字段配置信息！"
			throw new DmException(JsMessage.getValue("ddlfield.nofield"), tableName);
		}
		
		List<String> lssql = FactoryUtil.newList();
		for (int i = 0, n = lsField.size(); i < n; i++) {
			Map<String,String> mpField = lsField.get(i);
			
			//添加表名
			mpField.put("table_name", tableName);
			
			//解析建字段SQL
			lssql.add(_parser.parseTemplet("column_title", mpField));
		}
		
		return lssql;
	}
	
	/**
	 * 解析创建表用的字段SQL
	 * @param mpTable -- 表信息
	 * @return
	 * @throws DmException
	 */
	public String buildFields(Map<String,String> mpTable) throws DmException {
		//取当前表配置
		String tableId = mpTable.get("table_id");
		String tableName = mpTable.get("table_name");
		
		List<Map<String,String>> lsField = DmConfig.getFieldCfg(tableId);
		if (lsField.isEmpty()) {
			throw new DmException(JsMessage.getValue("ddlfield.nofield"), tableName);
		}
		
		StringBuilder sbField = new StringBuilder();
		for (int i = 0, n = lsField.size(); i < n; i++) {
			Map<String,String> mpField = lsField.get(i);
			
			//添加表名
			mpField.put("table_name", tableName);
			
			//解析建字段SQL
			String templet = _parser.parseTemplet("create_column", mpField);
			
			sbField.append("\t");
			if (i == n-1) {
			//最后一个字段要去掉,与换行符
				templet = templet.trim();
				templet = templet.substring(0, templet.length()-1);
				sbField.append(templet);
			} else {
				sbField.append(templet);
			}
		}
		
		return sbField.toString();
	}
	
	/**
	 * 构建该表创建的字段定义SQL
	 * @param mpTable -- 表信息
	 * @return
	 * @throws DmException
	 */
	public List<String> compareFields(Map<String,String> mpTable) throws DmException {
		List<String> lssql = FactoryUtil.newList();
		
		//取当前表配置
		String tableId = mpTable.get("table_id");
		String tableName = mpTable.get("table_name");
		
		//取新增与修改的字段配置信息
		List<Map<String,String>> lsField = DmConfig.getFieldCfg(tableId, "state in ('1', '2')");

		//取原表的字段配置信息
		List<Map<String,String>> lsFieldOld = DmConfig.getFieldOldCfg(tableId);
		
		//构建新增字段、修改字段的SQL
		for (int i = 0, n = lsField.size(); i < n; i++) {
			Map<String,String> mpField = lsField.get(i);
			//添加表名
			mpField.put("table_name", tableName);
			
			//取一个字段的更新语句
			lssql.addAll(compareOneField(mpField, lsFieldOld));
		}
		
		//取删除的字段
		lsField = DmConfig.getFieldCfg(tableId, "state = '3'");
		
		//构建删除字段的SQL		
		for (int i = 0, n = lsField.size(); i < n; i++) {
			Map<String,String> mpField = lsField.get(i);
			//添加表名
			mpField.put("table_name", tableName);
			
			//取字段删除的SQL
			lssql.add(_parser.parseTemplet("drop_column", mpField));
		}
		
		return lssql;
	}
	
	/**
	 * 比较一个字段与原配置信息差异的SQL
	 * @param mpField -- 当前字段信息
	 * @param lsFieldOld -- 原字段信息表
	 * @return
	 * @throws DmException
	 */
	protected List<String> compareOneField(Map<String,String> mpField, 
						List<Map<String,String>> lsFieldOld) throws DmException {
		List<String> lssql = FactoryUtil.newList();
		
		String fieldId = mpField.get("field_id");
		
		Map<String,String> mpFieldOld = getOldField(fieldId, lsFieldOld);
		//如果为空，说明是新增的字段
		if (mpFieldOld == null) {
		//取添加字段的SQL
			lssql.add(_parser.parseTemplet("add_column", mpField));
			lssql.add(_parser.parseTemplet("column_title", mpField));
		} else {
		//取字段内容修改的SQL
			lssql.addAll(fieldModifySql(mpField, mpFieldOld));
		}
		
		return lssql;
	}
	
	/**
	 * 获取字段修改内容
	 * @param mpField -- 新字段内容
	 * @param mpFieldOld -- 原字段内容
	 * @return
	 */
	protected List<String> fieldModifySql(Map<String,String> mpField, 
						Map<String,String> mpFieldOld) throws DmException {
		List<String> lssql = FactoryUtil.newList();
		
		//判断字段名是否修改
		String fieldName = MapUtil.getValue(mpField, "field_name");
		String fieldNameOld = MapUtil.getValue(mpFieldOld, "field_name");
		if (!fieldName.equalsIgnoreCase(fieldNameOld)) {
			mpField.put("old_field_name", fieldNameOld);
			lssql.add(_parser.parseTemplet("alter_column_name", mpField));
		}
		
		//判断字段标题是否修改
		String fieldTitle = MapUtil.getValue(mpField, "field_title");
		String fieldTitleOld = MapUtil.getValue(mpFieldOld, "field_title");
		if (!fieldTitle.equals(fieldTitleOld)) {
			lssql.add(_parser.parseTemplet("column_title", mpField));
		}
		
		//判断数据类型是否修改
		String dataType = MapUtil.getValue(mpField, "data_type");
		String dataTypeOld = MapUtil.getValue(mpFieldOld, "data_type");
		String dataSize = MapUtil.getValue(mpField, "data_size");
		String dataSizeOld = MapUtil.getValue(mpFieldOld, "data_size");
		String dataScale = MapUtil.getValue(mpField, "data_scale");
		String dataScaleOld = MapUtil.getValue(mpFieldOld, "data_scale");
		if (!dataType.equals(dataTypeOld) || !dataSize.equals(dataSizeOld) || !dataScale.equals(dataScaleOld)) {
			lssql.add(_parser.parseTemplet("alter_column_type", mpField));
		}
		
		//判断缺省值是否修改
		String defaultValue = MapUtil.getValue(mpField, "default_value");
		String defaultValueOld = MapUtil.getValue(mpFieldOld, "default_value");
		if (!defaultValue.equals(defaultValueOld)) {
			lssql.add(_parser.parseTemplet("alter_column_default", mpField));
		}
		
		//判断必填值是否修改
		String nullable = MapUtil.getValue(mpField, "nullable");
		String nullableOld = MapUtil.getValue(mpFieldOld, "nullable");
		if (!nullable.equals(nullableOld)) {
			lssql.add(_parser.parseTemplet("alter_column_nullable", mpField));
		}
		
		return lssql;
	}
	
	/**
	 * 根据字段ID在原字段配置信息表取字段配置信息
	 * @param fieldId -- 字段ID
	 * @param lsFieldOld -- 字段原配置信息
	 * @return
	 */
	protected Map<String,String> getOldField(String fieldId, List<Map<String,String>> lsFieldOld) {
		for (int i = 0, n = lsFieldOld.size(); i < n; i++) {
			Map<String,String> mpFieldOld = lsFieldOld.get(i);
			
			String fieldOldId = mpFieldOld.get("field_id");
			if (fieldOldId.equals(fieldId)) {
				return mpFieldOld;
			}
		}
		
		return null;
	}
}
