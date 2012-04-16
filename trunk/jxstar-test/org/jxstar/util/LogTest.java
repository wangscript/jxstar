package org.jxstar.util;

import java.lang.reflect.Method;

import org.jxstar.util.log.Log;





public class LogTest {
	private static Log log = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String realPath1 = "D:/Tomcat6/webapps/jxstar";
		SystemInitTest.initSystem(realPath1);
		log = Log.getInstance();

		String id = "1111" + 100;
		id = id.substring(id.length()-4, id.length());
		log.showDebug(id);
		
		log.showWarn("sdfsdfsd");
		log.showError("sdfsdfsd", new Exception("那时要"));
		//DbHandler dao = (DbHandler) SystemFactory.createSystemObject("DbHandler");
		//dao.update("update fun_base set ='1'  where fun_base.fun_id = ?", new String[]{"tm3232023", "string"});

		//dao.queryOneRow("select * from fun_base");
	}
	
	public static void s(String s) {
		
	}

	@SuppressWarnings({"rawtypes"})
	public static Object loadclass(String classname) {
		try {
			//创建资源工具的class对象
			Class clzz = Class.forName(classname);
			//调用资源工具的初始化方法
			Method method = clzz.getMethod("hello", new Class[]{String.class});
			return method.invoke(null, new Object[]{"tanzhi"});
		} catch (Exception e) {
			log.showError(e);
			return null;
		}
	}
}
