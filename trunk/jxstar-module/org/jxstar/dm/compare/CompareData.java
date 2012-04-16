/*
 * CompareData.java 2010-12-24
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.compare;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.util.SQLParseException;
import org.jxstar.dao.util.SqlParserImp;
import org.jxstar.dm.DmException;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 表记录对比工具，找出新增、删除、修改的记录，处理方法是：
 * 1、要求比较双方的表结构必须相同，否则可能会有异常，表配置信息从DM数据模型库中取；
 * 2、如果A表与B表比较，取出A表所有记录与B表所有记录，逐条比较：
 *    如果A表的主键值在B表存在： 则每个字段值比较，如果不同，则生成该字段值修改的SQL；
 *    如果A表的主键值在B表不存在： 则构建新增语句；
 *    如果B表的主键值在A表不存在： 则构建删除语句；
 *   
 * @author TonyTan
 * @version 1.0, 2010-12-24
 */
public class CompareData {
	protected BaseDao _dao = BaseDao.getInstance();
	
	//目标数据源，用于解析SQL函数
	protected String _targetDs = "target";

	/**
	 * 比较一个表中的数据差异，并生成更新SQL
	 * @param srcTable -- 来源表名
	 * @param srcDsName -- 来源数据源
	 * @param tagTable -- 目标表名
	 * @param tagDsName -- 目标数据源
	 * @return
	 * @throws DmException
	 */
    public List<String> compareSQL(String srcTable, String srcDsName, 
    						  String tagTable, String tagDsName) throws DmException {
    	if (srcTable == null || srcTable.length() == 0 ||
    			srcDsName == null || srcDsName.length() == 0) {
    		//"参数错误：来源表名或数据源名为空！"
    		throw new DmException(JsMessage.getValue("comparedata.srcparamnull"));
    	}
    	if (tagTable == null || tagTable.length() == 0 ||
    			tagDsName == null || tagDsName.length() == 0) {
    		//"参数错误：目标表名或数据源名为空！"
    		throw new DmException(JsMessage.getValue("comparedata.tagparamnull"));
    	}
    	
    	_targetDs = tagDsName;
    	
    	//取配置表中的字段信息
    	List<Map<String,String>> lsField = TableConfig.getFieldCfg(srcTable, srcDsName);
    	if(lsField == null || lsField.isEmpty()) {
    		//"来源表【{0}】没有字段配置信息！"
        	throw new DmException(JsMessage.getValue("comparedata.srcnofield"), srcTable);
        }
    	
        //取来源表的主键字段名
        String srcKeyField = TableConfig.getKeyField(srcTable, srcDsName);
        if(srcKeyField == null || srcKeyField.length() == 0) {
        	//"来源表【{0}】配置信息中没有定义主键字段！"
        	throw new DmException(JsMessage.getValue("comparedata.srcnokey"), srcTable);
        }
        
        //取来源表的数据
    	String srcsql = "select * from " + srcTable + " order by " + srcKeyField;
    	DaoParam srcParam = _dao.createParam(srcsql);
    	srcParam.setDsName(srcDsName);
    	List<Map<String,String>> lsSrc = _dao.query(srcParam);
    	
        //取目标表的主键字段名
        String tagKeyField = TableConfig.getKeyField(tagTable, tagDsName);
        if(tagKeyField == null || tagKeyField.length() == 0) {
        	//"目标表【{0}】配置信息中没有定义主键字段！"
        	throw new DmException(JsMessage.getValue("comparedata.tagnokey"), tagTable);
        }
    	
    	//取目标表的数据
    	String tagsql = "select * from " + tagTable + " order by " + tagKeyField;
    	DaoParam tagParam = _dao.createParam(tagsql);
    	tagParam.setDsName(tagDsName);
    	List<Map<String,String>> lsTag = _dao.query(tagParam);
    	
    	//来源记录是否在目标记录中存在
    	boolean isexist = false;
    	//保存比较出来的SQL
    	List<String> lssql = FactoryUtil.newList();
    	
    	//查找来源表中新增的记录与与目标表中内容不同的记录，如果相同则在目标数据中移除这条
    	for (int i = 0, n = lsSrc.size(); i < n; i++) {
    		Map<String,String> mpSrc = lsSrc.get(i);
    		String srcKey = mpSrc.get(srcKeyField);
    		
    		for (int j = 0, m = lsTag.size(); j < m; j++) {
    			Map<String,String> mpTag = lsTag.get(j);
    			String tagKey = mpTag.get(tagKeyField);
    			
    			//如果主键值相等，则需要比较每个字段内容相同
    			if (srcKey.equals(tagKey)) {
    				lssql.addAll(updateSql(lsField, mpSrc, mpTag, tagTable, tagKeyField, tagKey));
    				
    				lsTag.remove(j);
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
    	for (int i = 0, n = lsTag.size(); i < n; i++) {
    		Map<String,String> mpTag = lsTag.get(i);
    		String tagKey = mpTag.get(tagKeyField);
    		
    		lssql.add(deleteSql(tagTable, tagKeyField, tagKey));
    	}
    	//如果为空直接返回
    	if (lssql.isEmpty()) {
    		lssql.add("--"+ JsMessage.getValue("comparedata.nodiff") +"\r\n\r\n");//无差异
    		return lssql;
    	}
    	
    	//添加提交语句
    	lssql.add("commit;\r\n\r\n");
    	
    	return lssql;
    }

    /**
     * 构建删除记录的SQL
     * @param tagTable -- 目标表名
     * @param tagKeyField -- 目标主键字段
     * @param tagKey -- 目标主键值
     * @return
     */
    public String deleteSql(String tagTable, String tagKeyField, String tagKey) 
    					throws DmException {
    	if (tagTable == null || tagTable.length() == 0 ||
    			tagKeyField == null || tagKeyField.length() == 0 ||
    			tagKey == null || tagKey.length() == 0) {
    		//"目标表名、目标表主键字段、主键值可能为空！"
    		throw new DmException(JsMessage.getValue("comparedata.tagkeynull"));
    	}
    	
        StringBuilder sbdel = new StringBuilder();
        sbdel.append("delete from " + tagTable);
        sbdel.append(" where " + tagKeyField + " = '" + tagKey + "';\r\n\r\n");

        return sbdel.toString();
    }

    /**
     * 构建新增记录的SQL
     * @param lsData -- 新增的数据
     * @param targetTable -- 目标表名
     * @return
     * @throws DmException
     */
    public String insertSql(List<Map<String,String>> lsField, Map<String,String> srcData, 
    				String targetTable) throws DmException {
    	if (targetTable == null || targetTable.length() == 0) {
    		//"目标表名为空！"
    		throw new DmException(JsMessage.getValue("comparedata.tagtablenull"));
    	}
    	if (lsField == null || lsField.isEmpty()) {
    		//"目标表【{0}】的字段信息为空！"
    		throw new DmException(JsMessage.getValue("comparedata.tagfieldnull"), targetTable);
    	}
    	if (srcData == null || srcData.isEmpty()) {
    		//"目标表【{0}】的来源数据为空！"
    		throw new DmException(JsMessage.getValue("comparedata.srcdatanull"), targetTable);
    	}
    	
    	//新增记录SQL的字段与值
    	StringBuilder sbfield = new StringBuilder();
    	StringBuilder sbvalue = new StringBuilder();
    	
        for(int j = 0, m = lsField.size(); j < m; j++) {
        	Map<String,String> mpField = lsField.get(j);
            if(mpField == null || mpField.isEmpty()) continue;
            
            //取字段名与数据类型
            String field = mpField.get("field_name");
            String type = mpField.get("data_type");
            
            //取字段值，并根据数据类型转换为SQL
            String value = srcData.get(field);
            value = converValue(value, type);
            
            //拼接字段SQL与值SQL
            sbfield.append(field + ", ");
            sbvalue.append(value + ", ");
        }
        //去掉最后的", "符号
    	String allfield = sbfield.substring(0, sbfield.length() - 2);
    	String allvalue = sbvalue.substring(0, sbvalue.length() - 2);
        	
    	//构建新增一条记录的SQL
    	StringBuilder sbinsert = new StringBuilder();
    	sbinsert.append("insert into " + targetTable + " (");
    	sbinsert.append(allfield + ")\r\n "+ "values(" + allvalue + ");\r\n\r\n");

        return sbinsert.toString();
    }
    
    /**
     * 构建更新语句
     * @param lsField -- 配置表中的字段信息
     * @param srcData -- 来源记录值
     * @param tagData -- 目标记录值
     * @param tagTable -- 目标表名
     * @param tagKeyField -- 目标主键字段
     * @param tagKey -- 主键值
     * @return
     * @throws DmException
     */
    public List<String> updateSql(List<Map<String,String>> lsField, Map<String,String> srcData, 
    		Map<String,String> tagData, String tagTable, String tagKeyField, String tagKey) 
    		throws DmException {
    	if (tagTable == null || tagTable.length() == 0 ||
    			tagKeyField == null || tagKeyField.length() == 0 ||
    			tagKey == null || tagKey.length() == 0) {
    		throw new DmException(JsMessage.getValue("comparedata.tagkeynull"));
    	}
    	if (lsField == null || lsField.isEmpty()) {
    		//"目标表【{0}】的字段信息为空！"
    		throw new DmException(JsMessage.getValue("comparedata.tagfieldnull"), tagTable);
    	}
    	if (srcData == null || srcData.isEmpty()) {
    		//"目标表【{0}】的来源数据为空！"
    		throw new DmException(JsMessage.getValue("comparedata.srcdatanull"), tagTable);
    	}
    	if (tagData == null || tagData.isEmpty()) {
    		//"目标表【{0}】的目标数据为空！"
    		throw new DmException(JsMessage.getValue("comparedata.tagdatanull"), tagTable);
    	}
    	
    	List<String> lssql = FactoryUtil.newList();
    	
    	for (int i = 0, n = lsField.size(); i < n; i++) {
    		Map<String,String> mpField = lsField.get(i);
    		
    		//取字段名与数据类型
    		String fieldName = mpField.get("field_name");
    		String dataType = mpField.get("data_type");
    		
    		//判断配置字段在数据值中是否存在
    		if (!srcData.keySet().contains(fieldName)) {
    			//"表【{0}】记录中没有【{1}】配置字段的值！"
     			throw new DmException(JsMessage.getValue("comparedata.tablenofield"), tagTable, fieldName);
     		}
    		
    		//取来源值与目标值
    		String srcValue = srcData.get(fieldName);
    		String tagValue = tagData.get(fieldName);
    		
    		//如果该字段的值不等，则需要生成更新语句
    		if (!compareValue(srcValue, tagValue, mpField)) {
    			String value = converValue(srcValue, dataType);
    			//原字段值
    			lssql.add("--"+JsMessage.getValue("comparedata.oldfield")+"：'" + tagValue + "'\r\n");
    			lssql.add("update " + tagTable + " set " + fieldName + " = " + value + 
    					  " where " + tagKeyField + " = '" + tagKey + "';\r\n\r\n");
    		}
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
		
		return srcValue.length() == tagValue.length() && srcValue.equals(tagValue);
    }
    
    /**
     * 把数据值中的特殊符号转换为SQL语句中能识别的符号。
     * @param value -- 需要转换的值
     * @return
     */
    private String converChar(String value) {
        String conver = new String(value);
        conver = conver.replaceAll("'", "''");
        conver = conver.replaceAll("&", "'||chr(38)||'");
        conver = conver.replaceAll("\n", " ");
        return conver;
    }
    
    /**
     * 根据数据类型，把数据值转换为SQL语句中可以直接用的字符串。
     * @param value -- 数据值
     * @param type -- 数据类型
     * @return
     */
    private String converValue(String value, String type) {
    	if (value == null || value.length() == 0) {
    		return "null";
    	}
    	
    	String ret = value;
    	//数据类型有：int, number, char, varchar, date, blob
    	if (type.equals("number") || type.equals("int")) {
    		ret = value;
    	} else if (type.equals("date")) {
    		SqlParserImp sqlParser = new SqlParserImp(_targetDs);
    		//日期格式值需要解析为函数是形式
    		try {
	    		if (value.length() > 10) {
	    			ret = sqlParser.parse("{TO_DATETIME}('"+ value +"')");
	    		} else {
	    			ret = sqlParser.parse("{TO_DATE}('"+ value +"')");
	    		}
    		} catch (SQLParseException e) {
				e.printStackTrace();
			}
    	} else {
    		ret = "'" + converChar(value) + "'";
    	}
    	
    	return ret;
    }
}
