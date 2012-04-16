package org.jxstar.control;

import java.util.Iterator;
import java.util.Map;


import org.jxstar.dao.pool.DataSourceConfigManager;
import org.jxstar.util.MapUtil;
import org.jxstar.util.config.SQLParseConfig;
import org.jxstar.util.config.SystemConfig;
import org.jxstar.util.log.Log;
import org.jxstar.util.resource.JsMessage;
import org.jxstar.util.system.SystemInitUtil;

/**
 * @author TonyTan 2008-4-27
 * 
 * 系统初始化的测试类
 */
public class SystemInitTest {

	@SuppressWarnings({"rawtypes" })
	public static void main(String[] args) {
		String configFile = "conf/server.xml";
		//String realPath = "D:/Tomcat6/webapps/jxstar";
		String logFile = "log.properties";
		
		//初始化日志对象
		Log.getInstance().init(logFile);
		
		//初始化系统对象
		SystemInitUtil.initSystem(configFile, false);
		
		//输出配置文件的对象
		Map mpConfig = SystemConfig.getConfigMap();
		System.out.println("server.xml=\n\t" + MapUtil.toString(mpConfig));
		mpConfig = SQLParseConfig.getConfigMap();
		System.out.println("funsql.xml=\n\t" + MapUtil.toString(mpConfig));
		
		//输出数据源对象
		DataSourceConfigManager dscm = DataSourceConfigManager.getInstance();
		System.out.println(dscm.getDataSourceConfig("default").toString());
		System.out.println(dscm.getDataSourceConfig("mysql_test").toString());
		System.out.println(dscm.getDataSourceConfig("sql_test").toString());
		
		//测试资源文件对象
		System.out.println(JsMessage.getValue("functionbm.newkeynull"));
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static void printMap(Map mp) {
		Iterator itr = mp.keySet().iterator();
		while(itr.hasNext()) {
			String sName = (String) itr.next();
			System.out.println(sName + "=" + mp.get(sName));
		}
	}
}
