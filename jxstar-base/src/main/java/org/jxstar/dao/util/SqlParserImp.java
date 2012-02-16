/*
 * SqlParserImp.java 2008-4-8
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao.util;

import org.jxstar.util.config.SQLParseConfig;
import org.jxstar.util.log.Log;

/**
 * 解析SQL语句中的函数,根据数据类型进行替换函数内容,
 * 解决SQL语句中函数的跨数据库的问题.
 * 
 * @author TonyTan
 * @version 1.0, 2008-4-8
 */
public class SqlParserImp implements SqlParser {
	//日志工具
	private static Log _log = Log.getInstance();
	//函数参数的自定义标识
	private static final String[] PARAMID_ARRAY = 
						new String[]{"@v1", "@v2", "@v3", "@v4", "@v5"};
	//函数标示符
	private static final char _funstartflag = '{';
	private static final char _funendflag   = '}';	
		
	//数据库类型标识
	private String _dbType = DBTypeUtil.getDbmsType();	

	/**
	 * 系统缺省情况下使用该构造函数的对象,有系统对象工厂生成.
	 */
	public SqlParserImp(){}

	/**
	 * 在特殊情况下,可以由该构造函数直接生成对象.
	 * 
	 * @param sDataSource - 数据源名
	 */
	public SqlParserImp(String sDataSource) {
		_dbType = DBTypeUtil.getDbmsType(sDataSource);
	}
	
	public String parse(String sSQL) throws SQLParseException {
		return parse(sSQL, new String[]{});
	}	
	
	public String parse(String sSQL, String sExclusion) 
					throws SQLParseException {
		return parse(sSQL, new String[]{sExclusion});
	}
	
	/**
	 * 解析SQL语句中的函数.
	 * 
	 * @param sSQL - 被解析的SQL
	 * @param asExclusion - 不参与解析的字符串数组
	 * @return String - 返回解析后的SQL，如果出错了则不解析SQL
	 */
	public String parse(String sSQL, String[] asExclusion)
						throws SQLParseException {
		if (sSQL == null) {
			throw new SQLParseException("parseSQLFunIdent param sSQL is null! ");
		}
		if (asExclusion == null) {
			throw new SQLParseException("parseSQLFunIdent param asExclusion is null! ");
		}
		
		//SQL语句中没有{, 判断为没有自定义函数标识
		if (sSQL.indexOf(_funstartflag) < 0) return sSQL;
		//把解析后的SQL存到该Buffer中
		StringBuilder sbParsedSQL = new StringBuilder();	
		
		char ch;
		for(int i = 0, n = sSQL.length(); i < n; i++) {
			ch = sSQL.charAt(i);//读取SQL语句中的字符
			
			if (ch == _funstartflag) {//如果为{, 表示找到了自定义函数标识
			   StringBuilder sbSubFunParseSQL = new StringBuilder();
			   int ilen = parseSubSQLFunIdent(sSQL.substring(i, sSQL.length()), 
					   sbSubFunParseSQL, asExclusion);
			   sbParsedSQL.append(sbSubFunParseSQL.toString());
			   if (ilen < 0) {
				   throw new SQLParseException("parse sql custom function faild! ");
			   }
			   
			   i += ilen;
			} else {
				sbParsedSQL.append(ch);
			}
		}
		
		return sbParsedSQL.toString();
	}
	
	/**
	 * 解析{后的自定义函数字符串
	 * @param sSubSQL - {后的字符串
	 * @param sFunParseSQL - 自定义函数标识的解析对象
	 * @param asExclusion - 不参与解析的字符串
	 * @return int
	 */
	private int parseSubSQLFunIdent(String sSubSQL, 
			StringBuilder sbFunParseSQL, String[] asExclusion)
			throws SQLParseException {
		if (sSubSQL == null) return -1;	
		//把自定义函数的标识存到该Buffer中
		StringBuilder sbFunIdent = new StringBuilder();
		//把自定义函数的参数存到该Buffer中
		StringBuilder sbParam = new StringBuilder();
		//自定义函数标识的解析对象
		String sFunParseSQL = "";
		
		//state状态标识  1--表示在{之后, 2--表示在}之后, 
		//              3--表示在(之后, )之后变为1.
		//pi函数参数序号 
		char ch, state = 1, pi = 0, pisub = 0;
		for(int i = 0, n = sSubSQL.length(); i < n; i++) {
			ch = sSubSQL.charAt(i);//读取SQL语句中的字符
			 
			switch (state) {
			   case 1://开始处理自定义函数标识
				   sbFunIdent.append(ch);
				   
				   if (ch == _funendflag) {
					   //获取自定义函数标识的解析对象
					   sFunParseSQL = getFunParseSQL(sbFunIdent.toString(), asExclusion);
					   //根据配置信息决定是否解析函数的参数
					   if (isParseParam(sbFunIdent.toString(), asExclusion)) {
						   state = 2;
					   } else {
						   sbFunParseSQL = sbFunParseSQL.append(sFunParseSQL);
						   return i;
					   }
					   //清除自定义函数标识
					   sbFunIdent = sbFunIdent.delete(0, sbFunIdent.length());
					   
					   continue;
				   }
				   break;
			   case 2://开始查找自定义函数的参数, 过滤}(之间的空格
				   if (ch == '(') {
					   state = 3;
					   continue;
				   } else if (ch != ' ') {//如果}(之间不是空, 则认为没有(参数
					   sbFunParseSQL = sbFunParseSQL.append(sFunParseSQL);
					   return i;
				   }
				   break;
			   case 3://处理自定义函数的参数
				   if (ch == _funstartflag) { 
					   //取当前到结束的字符串继续解析
					   StringBuilder sbSubFunParseSQL = new StringBuilder();
					   int ilen = parseSubSQLFunIdent(sSubSQL.substring(i, sSubSQL.length()), 
							   sbSubFunParseSQL, asExclusion);
					   if (ilen < 0) {//如果子自定义函数解析出错,则直接退回
						   return -1;
					   }
					   
					   sFunParseSQL = sFunParseSQL.replaceFirst(PARAMID_ARRAY[pisub], 
							   sbSubFunParseSQL.toString());
					   if (++pisub > 4) {
						   _log.showWarn("自定义函数参数超过了五个！");
						   return -1;
					   }
					   
					   i += ilen;
					   
					   continue;
				   }
				   
				   if (ch == ',' || ch == ')') {
					   //替换解析函数字符串的参数
					   sFunParseSQL = sFunParseSQL.replaceFirst(PARAMID_ARRAY[pi], 
							   		  sbParam.toString());
					   sbParam = sbParam.delete(0, sbParam.length());

					   if (ch == ')') {
						   //把解析后的函数字符串拼加到SQL语句中
						   sbFunParseSQL = sbFunParseSQL.append(sFunParseSQL);
						   return i;
					   } else { 
						   if (++pi > 4) {
							   throw new SQLParseException("custom function param num > 5 ! ");
						   }
					   }
					   
					   continue;
				   }

				   sbParam.append(ch);
				   break;
			}
		}
		
		return -1;
	}
	
	/**
	 * 根据自定义函数标识取到对应数据库的解析字符串
	 * 
	 * @param sFunIdent
	 * @return
	 */
	private String getFunParseSQL(String sFunIdent) throws SQLParseException {
		String sql = SQLParseConfig.getFunName(sFunIdent, _dbType);
		if (sql == null || sql.length() == 0) {
			throw new SQLParseException("not find custom function name: " + sFunIdent + " ! ");
		}
		
		return sql;
	}
	
	/**
	 * 根据自定义函数标识取到对应数据库的解析字符串
	 * 
	 * @param sFunIdent - 函数标示
	 * @param asExclusion - 不参与解析的字符串
	 * @return String
	 */
	private String getFunParseSQL(String sFunIdent, String[] asExclusion) 
								throws SQLParseException {
		for (int i = 0; i < asExclusion.length; i++) {
			if (sFunIdent.equals(asExclusion[i])) {
				return sFunIdent;
			}
		}
		
		return getFunParseSQL(sFunIdent);
	}
	
	/**
	 * 根据函数标示取是否解析参数设置值.
	 * 
	 * @param sFunIdent - 函数标示
	 * @return boolean
	 */
	private boolean isParseParam(String sFunIdent) {
		return SQLParseConfig.isParseParam(sFunIdent);
	}
	
	/**
	 * 根据函数标示取是否解析参数设置值.
	 * 
	 * @param sFunIdent - 函数标示
	 * @param asExclusion - 不参与解析的字符串
	 * @return boolean
	 */
	private boolean isParseParam(String sFunIdent, String[] asExclusion) {
		for (int i = 0; i < asExclusion.length; i++) {
			if (sFunIdent.equals(asExclusion[i])) {
				return false;
			}
		}
		
		return isParseParam(sFunIdent);
	}
}