/*
 * CommonTest.java 2008-5-17
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;

/**
 * 
 * 
 * @author TonyTan
 * @version 1.0, 2008-5-17
 */
public class CommonTest { 
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean b = FileUtil.exists("d:/form_sj_start");
		System.out.println("========" + b);

		//SystemInitTest.initSystem("d:/Tomcat6/webapps/jxstar");
		//CommonTest test = new CommonTest();
		//for (int i = 0; i < 1000; i++) {
		//	String fix = test.createRandomNum();
			/*if (fix.length() < 4) {
				System.out.println("=========只有3个字符！！" + fix);
			}
			if (i % 1000 == 0)*/
		//		System.out.println("输出了" + fix);
		//}
		
		//System.out.println("----" + parseStr("卡号为【[std_code]】的工艺卡已制定，等待您审核。", "aaa"));
		
		System.out.println("========" + DateUtil.getTodaySec());
	}
	/*
	private String createRandomNum() {
		int random =  (int) (Math.random() * 1000);
		StringBuilder fix = new StringBuilder(Integer.toString(random));
		
		int len = 3 - fix.length();
		for (int i = 0; i < len; fix.insert(0, '0'), i++);
		
		fix.append('a');
		return fix.toString();
	}
	*/
	public static String parseStr(String src, String value) {
		Pattern p = Pattern.compile("\\[[^}]+\\]");
		Matcher m = p.matcher(src);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String name = m.group();
			name = name.substring(1, name.length()-1);
			if (value == null) value = "[no field]";
	
			m.appendReplacement(sb, value);
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
	public static void createTestData() {
		BaseDao _dao = BaseDao.getInstance();
		
		String id1, id2, id3, sql;
		for (int i = 1; i <= 100; i++) {
			id1 = "00" + i;
			id1 = "2" + id1.substring(id1.length()-3, id1.length());
			
			sql = "insert into funall_module(module_id, module_name, "
				+"module_index, module_level, has_child) values"
				+"('"+id1+"', '测试数据-"+i+"', "+ i +", 1, '1')";
			
			DaoParam param = _dao.createParam(sql);
			_dao.update(param);
			
			System.out.println("id1 = " + id1);
			
			for (int j = 1; j <= 100; j++) {
				id2 = "0000" + j;
				id2 = id1 + id2.substring(id2.length()-4, id2.length());
				
				sql = "insert into funall_module(module_id, module_name, "
					+"module_index, module_level, has_child) values"
					+"('"+id2+"', '测试数据-"+i+"-"+ j +"', "+ j +", 2, '1')";
				
				DaoParam param1 = _dao.createParam(sql);
				_dao.update(param1);
				System.out.println("id2 = " + id2);
				
				for (int k = 1; k <= 100; k++) {
					id3 = "0000" + k;
					id3 = id2 + id3.substring(id3.length()-4, id3.length());
					
					sql = "insert into funall_module(module_id, module_name, "
						+"module_index, module_level, has_child) values"
						+"('"+ id3 +"', '测试数据-"+i+"-"+ j +"-"+ k +"', "+ k +", 3, '0')";
					
					DaoParam param2 = _dao.createParam(sql);
					_dao.update(param2);
					System.out.println("id3 = " + id3);
				}
			}
		}
	}
	
	public static String getSQLPageQuery(String aSourceSQL, long pageNum, int pageRows){
		
		aSourceSQL = aSourceSQL.toLowerCase();
		
		String strPK = "fun_base.fun_id";
		String[] strSQL = getSQLPart(aSourceSQL);
		
		//创建内部检索sql
		StringBuilder strInnerSelect = new StringBuilder("select top " + (pageNum - 1 ) * pageRows + " " +strPK + " ");
		strInnerSelect.append(strSQL[1] + " ").append(strSQL[2] + " ").append(strSQL[3] + " ").append(strSQL[4]);
		
		//创建外部检索sql
		strSQL[0] = strSQL[0].replaceFirst("select ", "select top " + pageRows + " ");
		StringBuilder strQuery = new StringBuilder(); 
		strQuery.append(strSQL[0] + " ").append(strSQL[1] + " ");
		if (strSQL[2].length() == 0){
			strSQL[2] = " where ";
		}else{
			strSQL[2] = strSQL[2].replaceFirst("where ", " where ( ");
			strSQL[2] = strSQL[2] + ") and ";
		}
		strQuery.append(strSQL[2]+ " ").append(strPK + " not in (").append(strInnerSelect).append(") ").append(strSQL[3] + " ").append(strSQL[4]);
		
//		_log.showLog("Retrieve sql = " + strQuery.toString());
		return strQuery.toString();
	}
	
	private static String[] getSQLPart(String aSource){
		String[] strSQL = new String[5];
		int index = aSource.indexOf("from ");
		int index1 = aSource.indexOf("where ");
		strSQL[0] = aSource.substring(0,index); //获取select字句
		if (index1 > 0){ //如果存在where字句
			strSQL[1] = aSource.substring(index, index1); //from字句
			index = index1;
			
			//获取where字句
			index1 = aSource.indexOf("group by ");
			if(index1 >0) {
				strSQL[2] = aSource.substring(index, index1); //where字句
				index = index1;
				index1 = aSource.indexOf("order by ");
				if (index1 > 0){
					strSQL[3] = aSource.substring(index, index1);
					strSQL[4] = aSource.substring(index1, aSource.length());
				}else{
					strSQL[3] = aSource.substring(index, aSource.length());
					strSQL[4] = "";
				}
			}else{
				index1 = aSource.indexOf("order by ");
				strSQL[3] = "";
				if (index1 > 0){
					strSQL[2] = aSource.substring(index, index1);
					strSQL[4] = aSource.substring(index1, aSource.length());
				}else{
					strSQL[2] = aSource.substring(index, aSource.length());
					strSQL[4] = "";
				}
			}
		}else{
			index1 = aSource.indexOf("group by ");
			if (index1 > 0){
				strSQL[1] = aSource.substring(index, index1);
				strSQL[2] = "";
				index = index1;
				index1 = aSource.indexOf("order by ");
				if (index1 > 0){
					strSQL[3] = aSource.substring(index, index1);
					strSQL[4] = aSource.substring(index1, aSource.length());
				}else{
					strSQL[3] = aSource.substring(index, aSource.length());
					strSQL[4] = "";
				}
			}else{
				index1 = aSource.indexOf("order by ");
				if (index1 > 0){
					strSQL[1] = aSource.substring(index, index1);
					strSQL[2] = "";
					strSQL[3] = "";
					strSQL[4] = aSource.substring(index1, aSource.length());
				}else{
					strSQL[1] = aSource.substring(index, aSource.length());
					strSQL[2] = "";
					strSQL[3] = "";
					strSQL[4] = "";
				}
			}
		}
		
		return strSQL;
	}
}
