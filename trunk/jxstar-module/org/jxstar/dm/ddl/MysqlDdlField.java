/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.dm.ddl;

import java.util.List;
import java.util.Map;

import org.jxstar.dm.DdlField;
import org.jxstar.dm.DmException;
import org.jxstar.dm.DmParser;
import org.jxstar.util.MapUtil;
import org.jxstar.util.factory.FactoryUtil;

/**
 * Mysql数据库在处理字段描述信息时与Oracle不同
 *
 * @author TonyTan
 * @version 1.0, 2012-4-9
 */
public class MysqlDdlField extends DdlField {

	public MysqlDdlField(DmParser parser) {
		super(parser);
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
		} else {
		//取字段内容修改的SQL
			lssql.addAll(fieldModifySql(mpField, mpFieldOld));
		}
		
		return lssql;
	}
	
	/**
	 * 获取字段修改内容，Mysql字段中每个信息修改都需要重新构建字段所有信息的
	 * @param mpField -- 新字段内容
	 * @param mpFieldOld -- 原字段内容
	 * @return
	 */
	protected List<String> fieldModifySql(Map<String,String> mpField, 
						Map<String,String> mpFieldOld) throws DmException {
		List<String> lssql = FactoryUtil.newList();
		String sql = "";
		
		//判断字段名是否修改
		String fieldName = MapUtil.getValue(mpField, "field_name");
		String fieldNameOld = MapUtil.getValue(mpFieldOld, "field_name");
		if (!fieldName.equalsIgnoreCase(fieldNameOld)) {
			mpField.put("old_field_name", fieldNameOld);
			sql = _parser.parseTemplet("alter_column_name", mpField);
		}
		
		//判断字段标题是否修改
		String fieldTitle = MapUtil.getValue(mpField, "field_title");
		String fieldTitleOld = MapUtil.getValue(mpFieldOld, "field_title");
		if (sql.length() == 0 && !fieldTitle.equals(fieldTitleOld)) {
			sql = _parser.parseTemplet("alter_column", mpField);
		}
		
		//判断数据类型是否修改
		String dataType = MapUtil.getValue(mpField, "data_type");
		String dataTypeOld = MapUtil.getValue(mpFieldOld, "data_type");
		String dataSize = MapUtil.getValue(mpField, "data_size");
		String dataSizeOld = MapUtil.getValue(mpFieldOld, "data_size");
		String dataScale = MapUtil.getValue(mpField, "data_scale");
		String dataScaleOld = MapUtil.getValue(mpFieldOld, "data_scale");
		if (sql.length() == 0 && !dataType.equals(dataTypeOld) || 
				!dataSize.equals(dataSizeOld) || !dataScale.equals(dataScaleOld)) {
			sql = _parser.parseTemplet("alter_column", mpField);
		}
		
		//判断缺省值是否修改
		String defaultValue = MapUtil.getValue(mpField, "default_value");
		String defaultValueOld = MapUtil.getValue(mpFieldOld, "default_value");
		if (sql.length() == 0 && !defaultValue.equals(defaultValueOld)) {
			sql = _parser.parseTemplet("alter_column", mpField);
		}
		
		//判断必填值是否修改
		String nullable = MapUtil.getValue(mpField, "nullable");
		String nullableOld = MapUtil.getValue(mpFieldOld, "nullable");
		if (sql.length() == 0 && !nullable.equals(nullableOld)) {
			sql = _parser.parseTemplet("alter_column", mpField);
		}
		
		if (sql != null && sql.length() > 0) {
			lssql.add(sql);
		}
		
		return lssql;
	}
}
