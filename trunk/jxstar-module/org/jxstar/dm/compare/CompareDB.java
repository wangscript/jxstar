/*
 * CompareDB.java 2010-12-26
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.compare;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.dm.DmException;
import org.jxstar.dm.DmFactory;
import org.jxstar.dm.MetaData;
import org.jxstar.dm.util.DmUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 比较数据库配置信息与数据库系统表中的信息的差异。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-26
 */
public class CompareDB extends CompareData {
	private BaseDao _dao = BaseDao.getInstance();
	//数据源名
	private String _dsname = "default";
	//元数据对象
	private MetaData _metaData = DmFactory.getMetaData(_dsname);
	
	/**
	 * 比较表配置
	 * @return
	 */
	public List<String> compareTable() throws DmException {
    	//取表配置的字段信息
    	List<Map<String,String>> lsField = FactoryUtil.newList();
    	lsField.add(fieldMap("table_name", "string"));
    	lsField.add(fieldMap("table_title", "string"));
    	//有些数据库没有表空间
    	//lsField.add(fieldMap("table_space", "string"));
    	
        //取来源表的主键字段名
    	String srcTable = "v_table_info";
        String srcKeyField = "table_name";
    	
        //取目标表的主键字段名
    	String tagTable = "dm_tablecfg";
        String tagKeyField = "table_name";
        
        //取来源表的数据
    	String srcsql = "select * from v_table_info where table_name in " +
    			"(select table_name from dm_table where table_type < '9') order by table_name";
    	DaoParam srcParam = _dao.createParam(srcsql);
    	List<Map<String,String>> lsSrc = _dao.query(srcParam);
    	
    	//取目标表的数据
    	String tagsql = "select * from dm_tablecfg where table_type < '9' order by table_name";
    	DaoParam tagParam = _dao.createParam(tagsql);
    	List<Map<String,String>> lsTag = _dao.query(tagParam);
        
    	List<String> lssql = FactoryUtil.newList();
    	
    	//=========比较数据库表对象与dm_tablecfg配置信息的差异=========
    	lssql.add("--"+JsMessage.getValue("comparedb.comptable")+"\r\n");
    	List<String> lstable = compareCfg(srcTable, srcKeyField, tagTable, tagKeyField, lsSrc, lsTag, lsField);
    	if (lstable.isEmpty()) {//无差异
    		lssql.add("--"+JsMessage.getValue("comparedata.nodiff")+"\r\n\r\n");
    	} else {
    		lssql.addAll(lstable);
    	}
    	
		//=========比较数据库字段对象与dm_fieldcfg配置信息的差异=========
    	lssql.add("--"+JsMessage.getValue("comparedb.compfield")+"\r\n");
    	List<String> lsfield = FactoryUtil.newList();
    	for (int i = 0, n = lsSrc.size(); i < n; i++) {
    		Map<String,String> mpsrc = lsSrc.get(i);
    		String tableName = mpsrc.get("table_name");
    		
    		List<String> lstmp = compareField(tableName);
    		if (!lstmp.isEmpty()) {
    			//表【{0}】的字段差异
    			lsfield.add("--"+JsMessage.getValue("comparedb.fielddiff", tableName)+"\r\n");
    			lsfield.addAll(lstmp);
    		}
    	}
    	if (lsfield.isEmpty()) {//无差异
    		lssql.add("--"+JsMessage.getValue("comparedata.nodiff")+"\r\n\r\n");
    	} else {
    		lssql.addAll(lsfield);
    	}
    	
    	//=========比较数据库索引对象与dm_indexcfg配置信息的差异=========
    	lssql.add("--"+JsMessage.getValue("comparedb.compindex")+"\r\n");
    	List<String> lsindex = FactoryUtil.newList();
    	for (int i = 0, n = lsSrc.size(); i < n; i++) {
    		Map<String,String> mpsrc = lsSrc.get(i);
    		String tableName = mpsrc.get("table_name");
    		
    		List<String> lstmp = compareIndex(tableName);
    		if (!lstmp.isEmpty()) {
    			//表【{0}】的索引差异
    			lsindex.add("--"+JsMessage.getValue("comparedb.indexdiff", tableName)+"\r\n");
    			lsindex.addAll(lstmp);
    		}
    	}
    	if (lsindex.isEmpty()) {//无差异
    		lssql.add("--"+JsMessage.getValue("comparedata.nodiff")+"\r\n\r\n");
    	} else {
    		lssql.addAll(lsindex);
    	}
    	
    	//=========比较数据库主键字段与dm_tablecfg.key_field字段值的差异=========
    	lssql.add("--"+JsMessage.getValue("comparedb.compkey")+"\r\n");
    	lsTag = _dao.query(tagParam);	//目标记录在表配置比较时删除了，需要重新加载
    	List<String> lskey = FactoryUtil.newList();
    	for (int i = 0, n = lsTag.size(); i < n; i++) {
    		Map<String,String> mptag = lsTag.get(i);
    		String tableName = mptag.get("table_name");
    		String keyField = mptag.get("key_field");
    		
    		Map<String,String> mpKey = _metaData.getKeyMeta(tableName, _dsname);
    		String dbkey = "";
    		if (mpKey != null && !mpKey.isEmpty()) {
    			dbkey = mpKey.get("key_field");
    		}
    		
    		if (!keyField.equals(dbkey)) {
    			//表【{0}】的主键差异
    			lskey.add("--"+JsMessage.getValue("comparedb.keydiff", tableName)+"\r\n");
    			//db主键为【{0}】，cfg主键为【{1}】
    			lskey.add("--"+JsMessage.getValue("comparedb.keydb", dbkey, keyField)+"\r\n");
    		}
    	}
    	if (lskey.isEmpty()) {//无差异
    		lssql.add("--"+JsMessage.getValue("comparedata.nodiff")+"\r\n\r\n");
    	} else {
    		lssql.addAll(lskey);
    	}
    	
    	return lssql;
	}
	
	/**
	 * 比较字段配置
	 * @param tableName -- 表名
	 * @return
	 */
	public List<String> compareField(String tableName) throws DmException {
    	//取字段配置的字段信息
    	List<Map<String,String>> lsField = FactoryUtil.newList();
    	lsField.add(fieldMap("field_name", "string"));
    	lsField.add(fieldMap("field_title", "string"));
    	lsField.add(fieldMap("data_type", "string"));
    	lsField.add(fieldMap("data_size", "int"));
    	lsField.add(fieldMap("data_scale", "int"));
    	lsField.add(fieldMap("nullable", "string"));
    	lsField.add(fieldMap("default_value", "string"));
    	
        //取来源表的主键字段名
    	String srcTable = "v_column_info";
        String srcKeyField = "field_name";
    	
        //取目标表的主键字段名
    	String tagTable = "dm_fieldcfg";
        String tagKeyField = "field_name";
        
        //取来源表的数据
    	String srcsql = "select * from v_column_info where table_name = ? order by field_name";
    	DaoParam srcParam = _dao.createParam(srcsql);
    	srcParam.addStringValue(tableName);
    	List<Map<String,String>> lsSrc = _dao.query(srcParam);
    	//Oracle、SQLServer中处理缺省值中的换行符号
    	lsSrc = clearDefaultChar(lsSrc);
    	
    	//取表配置ID
    	String tableId = TableConfig.getTableId(tableName);
    	if (tableId == null || tableId.length() == 0) {
    		//"表【{0}】的配置ID为空！"
    		throw new DmException(JsMessage.getValue("comparedb.cfgidnull"), tableName);
    	}
    	
    	//取目标表的数据
    	String tagsql = "select * from dm_fieldcfg where table_id = ? order by field_name";
    	DaoParam tagParam = _dao.createParam(tagsql);
    	tagParam.addStringValue(tableId);
    	List<Map<String,String>> lsTag = _dao.query(tagParam);

        //取比较后的SQL列表
        List<String> lssql = compareCfg(srcTable, srcKeyField, tagTable, tagKeyField, 
        									lsSrc, lsTag, lsField);
        //在生成的SQL中添加表ID
        return addTableWhere(lssql, tableId);
	}
	
	/**
	 * 比较索引配置
	 * @param tableName -- 表名
	 * @return
	 */
	public List<String> compareIndex(String tableName) throws DmException {
    	//取字段配置的字段信息
    	List<Map<String,String>> lsField = FactoryUtil.newList();
    	lsField.add(fieldMap("index_name", "string"));
    	lsField.add(fieldMap("index_field", "string"));
    	lsField.add(fieldMap("isunique", "string"));
    	
        //取来源表的主键字段名
    	String srcTable = "";
        String srcKeyField = "index_name";
    	
        //取目标表的主键字段名
    	String tagTable = "dm_indexcfg";
        String tagKeyField = "index_name";
        
        //取来源表的数据
    	List<Map<String,String>> lsSrc = _metaData.getIndexMeta(tableName, _dsname);
    	
    	//取表配置ID
    	String tableId = TableConfig.getTableId(tableName);
    	if (tableId == null || tableId.length() == 0) {
    		//"表【{0}】的配置ID为空！"
    		throw new DmException(JsMessage.getValue("comparedb.cfgidnull"), tableName);
    	}
    	
    	//取目标表的数据
    	String tagsql = "select * from dm_indexcfg where table_id = ? order by index_name";
    	DaoParam tagParam = _dao.createParam(tagsql);
    	tagParam.addStringValue(tableId);
    	List<Map<String,String>> lsTag = _dao.query(tagParam);
        //取比较后的SQL列表
    	List<String> lssql = compareCfg(srcTable, srcKeyField, tagTable, tagKeyField, 
    										lsSrc, lsTag, lsField);
    	//在生成的SQL中添加表ID
        return addTableWhere(lssql, tableId);
	}
	
	/**
	 * 比较一个表中的数据差异，并生成更新SQL
	 * @param srcTable -- 来源表名
	 * @param srcKeyField -- 来源主键名
	 * @param tagTable -- 目标表名
	 * @param tagKeyField -- 目标主键名
	 * @param lsField -- 需要比较的字段
	 * @return
	 * @throws DmException
	 */
    public List<String> compareCfg(String srcTable, String srcKeyField, 
			  				String tagTable, String tagKeyField, 
			  				List<Map<String,String>> lsSrcData,
			  				List<Map<String,String>> lsTagData,
			  				List<Map<String,String>> lsField) 
			  				throws DmException {
    	//来源记录是否在目标记录中存在
    	boolean isexist = false;
    	//保存比较出来的SQL
    	List<String> lssql = FactoryUtil.newList();
    	//构建数据比较对象
    	//CompareData compareData = new CompareData();
    	
    	//查找来源表中新增的记录与与目标表中内容不同的记录，如果相同则在目标数据中移除这条
    	for (int i = 0, n = lsSrcData.size(); i < n; i++) {
    		Map<String,String> mpSrc = lsSrcData.get(i);
    		String srcKey = mpSrc.get(srcKeyField);
    		
    		for (int j = 0, m = lsTagData.size(); j < m; j++) {
    			Map<String,String> mpTag = lsTagData.get(j);
    			String tagKey = mpTag.get(tagKeyField);
    			
    			//如果主键值相等，则需要比较每个字段内容相同
    			if (srcKey.equals(tagKey)) {
    				lssql.addAll(updateSql(lsField, mpSrc, mpTag, tagTable, tagKeyField, tagKey));
    				
    				lsTagData.remove(j);
    				isexist = true;
    				break;
    			}
    		}
    		//如果在目标表中不存在，说明是新增的记录
    		if (!isexist) {
    			lssql.add(insertSql(lsField, mpSrc, tagTable));
    		}
    		
    		isexist = false;
    	}
    	
    	//取来源表中没有而目标表中有的记录，生成删除语句
    	for (int i = 0, n = lsTagData.size(); i < n; i++) {
    		Map<String,String> mpTag = lsTagData.get(i);
    		String tagKey = mpTag.get(tagKeyField);
    		
    		lssql.add(deleteSql(tagTable, tagKeyField, tagKey));
    	}
    	
    	return lssql;
    }
    
    /**
     * 来源值与目标值比较的方法，返回true表示相同，子类可以继承，修改比较算法
     * @param srcValue -- 来源值
     * @param tagValue -- 目标值
     * @param mpField -- 字段信息
     * @return
     */
    protected boolean compareValue(String srcValue, String tagValue,
    						Map<String,String> mpField) throws DmException {
    	if (mpField == null) {
    		//"值比较方法的字段信息参数为空！"
    		throw new DmException(JsMessage.getValue("comparedata.paramnull"));
    	}
    	
		if (srcValue == null) srcValue = "";
		if (tagValue == null) tagValue = "";
		
		//取字段名
		String fieldName = mpField.get("field_name");
		
		//如果是数据类型字段，则需要考虑字段类型的兼容性
		if (fieldName.equals("data_type") && _metaData != null) {
			srcValue = _metaData.getDataType(srcValue);
			//是数值类型比较，则忽略整数比较
			if (srcValue.equals("number") || tagValue.equals("int")) {
				return true;
			}
		} else if (fieldName.equals("data_scale")) {
			if (srcValue.length() == 0) srcValue = "0";
			if (tagValue.length() == 0) tagValue = "0";
		}
		if (fieldName.equals("default_value")) {
			//System.out.println("srcValue="+srcValue+";tagValue=" +tagValue);
		}
		
		return srcValue.length() == tagValue.length() && srcValue.equals(tagValue);
    }
    
    /**
     * 构建字段信息对象
     * @param fieldName -- 字段名
     * @param dataType -- 数据类型
     * @return
     */
    private Map<String,String> fieldMap(String fieldName, String dataType) {
    	Map<String,String> mpField = FactoryUtil.newMap();
    	mpField.put("field_name", fieldName);
    	mpField.put("data_type", dataType);
    	
    	return mpField;
    }
    
    /**
     * 给SQL语句添加表ID的过滤语句
     * @param lssql
     * @param tableId
     * return
     */
    private List<String> addTableWhere(List<String> lssql, String tableId) {
    	if (lssql == null || lssql.isEmpty()) return lssql;
    	
    	List<String> lsnew = FactoryUtil.newList();
    	for (int i = 0; i < lssql.size(); i++) {
    		String sql = lssql.get(i);
    		//如果是注释，则跳过
    		if (sql.trim().charAt(0) == '-') {
    			lsnew.add(sql);
    			continue;
    		}
    		
    		//找where 的位置，在其后添加[table_id = '' and ]SQL语句
    		int wi = sql.indexOf(" where ");
    		if (wi >= 0) {
    			sql = sql.substring(0, wi) + " where table_id = '"+ tableId +"' and " + sql.substring(wi+7, sql.length());
    		}
    		lsnew.add(sql);
    	}
    	
    	return lsnew;
    }
    
    /**
     * Oracle数据库用，取到的缺省值后面有换行符号
     * @param lsData
     * @return
     */
    private List<Map<String,String>> clearDefaultChar(List<Map<String,String>> lsData) {
    	if (lsData == null || lsData.isEmpty()) return lsData;
    	
    	for (Map<String,String> mpData : lsData) {
    		String key = "default_value";
    		String value = mpData.get(key);
    		if (value == null) value = "";
    		
    		if (value.length() > 0) {
    			value = value.trim();
    			if (DmUtil.hasYinHao(value)) {
    				value = value.substring(1, value.length()-1);
    			} else if (value.equals("null")) {
    				value = "";
    			}
    		}
    		
    		mpData.put(key, value);
    	}
    	
    	return lsData;
    }
}
