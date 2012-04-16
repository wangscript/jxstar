package org.jxstar.web.element;

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.jxstar.util.FileUtil;



public class CommonTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			String send = Math.random()*1000 + "";
			send = send.split("\\.")[0];
			System.out.println(send);
			
			StringBuffer bb = new StringBuffer('(');
			bb.append("ab" + '+' + ')');
			System.out.println("bb=" + bb.toString());
			
			StringBuilder aa = new StringBuilder('(');
			aa.append("ab" + '+' + ')');
			System.out.println("aa=" + aa.toString());
			
			String strVal = "2010-11-02 14:40:00.0";
			System.out.println(strVal.substring(strVal.length()-2, strVal.length()));
			System.out.println(strVal.substring(0, strVal.length()-2));
			
			String value = "设           备           目           录";
			System.out.println(value.replaceAll("\\s", "&nbsp;"));
			
			String fileName = "/eam/asset/grid_card_param.js";
			int si = fileName.indexOf('/', 2);
			int ei = fileName.indexOf('.');
			if (si > 0 && ei > 0) {
				fileName = fileName.substring(si, ei) + ".xls";
			}
			System.out.println(fileName);
			
			String col_code = ".bbcc";
			System.out.println(col_code.split("\\.")[1]);
			
			String clientMethod = "bb('aa', 'cc')";
			String args1 = clientMethod.substring(clientMethod.indexOf("(")+1, clientMethod.indexOf(")"));
			System.out.println(args1);
			
			String file = "d:/Tomcat6/webapps/jxstar/eam/system/grid_sys_attach1.js";
			file = file.substring(0, file.lastIndexOf('/'));
			System.out.println(file);
			FileUtil.createPath(file);
			
			File file1 = new File("C:/JXSTARDOC/run_mal_record/maximo- 权限.bmp");
			FileUtil.getValidFile(file1);
			
			try {
				CommonTest.conv();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/**
	 * 把GBK转换为UTF-8
	 * @param value -- 字符值
	 * @return
	 */
	public static String convCharSet(String value) {
		if (value == null || value.length() == 0) return value;
	    try {
	    	value = new String(value.getBytes("gbk"), "iso-8859-1");
	    	value = new String(value.getBytes("iso-8859-1"), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	private static void conv() throws UnsupportedEncodingException  {
	    //给定某3个汉字   
	    String src = "你好啊";   
	    //String src = "一二三";   
	       
	    //浏览器进行utf-8编码，并传送到服务器   
	    byte[] bytes1 = src.getBytes("utf-8");   
	    System.out.println(bytes1.length);//9   
	       
	    //tomcat以gbk方式解码(这个片段的说明仅针对gbk处理汉字的情况)   
	    //如果一对汉字字节不符合gbk编码规范，则每个字节使用'?'(ascii 63)代替   
	    //万幸的话，只是最后一个(第9个)字节因不能成对,变成问号(比如当src="你好啊"时)   
	    //不幸的话，中间某些字节就通不过gbk编码规范出现'?'了(比如当src="一二三"时)   
	    //总之temp的最后一位必定是问号'?'   
	    String temp = new String(bytes1, "gbk");    
	       
	   //你的action中的代码   
	    //由于以上的tomcat以gbk解释utf-8不能成功   
	    //所以此时bytes2和bytes1不一样   
	    byte[] bytes2 = temp.getBytes("gbk");   
	    System.out.println(bytes2.length);   
	    for (int i = 0; i < bytes1.length; i++) {   
	        System.out.print(bytes1[i] & 0xff);   
	       System.out.print("\t");   
	    }   
	    System.out.println();   
	    for (int i = 0; i < bytes2.length; i++) {   
	        System.out.print(bytes2[i] & 0xff);   
	        System.out.print("\t");   
	    }   
	    System.out.println();   
	  
	    //构建出来的dest自然不是原先的src   
	    String dest = new String(bytes2, "utf-8");   
	    System.out.println(dest); 
	}
}
