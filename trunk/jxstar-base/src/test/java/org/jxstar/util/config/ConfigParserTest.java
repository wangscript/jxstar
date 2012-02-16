package org.jxstar.util.config;

import org.jxstar.util.config.SystemConfigParser;



public class ConfigParserTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String sFileName = "E:/jxstar/web-inf/conf/server.xml";
		SystemConfigParser parser = new SystemConfigParser();
		parser.init(sFileName);
		parser.readConfig();
	}

}
