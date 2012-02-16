/**
 * 
 */
package org.jxstar.db;

import java.util.Map;


import org.jxstar.dao.DaoParam;
import org.jxstar.test.base.TestBase;

/**
 * @author bingco
 *
 * 执行错误的SQL，测试连接池是否存在泄漏。
 */
public class TestConnPoolError extends TestBase {

	/**
	 * 执行一次更新一次查询SQL
	 * @return
	 */
	protected boolean exeTest() {
		String sValue = "DongHong";
		
		String sql = "update fun_base set module_id = ? where fun_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(sValue).addStringValue("login");
		boolean bret = _dao.update(param);
		if (!bret) {
			_log.showDebug("=========TestConnPool.exeSQL 测试更新失败！");
		}
		
		String sql1 = "select module_id from fun_base where fun_id = ?";
		DaoParam param1 = _dao.createParam(sql1);
		param1.addStringValue("login").setDsName("default");
		Map<String, String> mp = _dao.queryMap(param1);
		if (mp == null || mp.isEmpty()) {
			_log.showDebug("=========TestConnPool.exeSQL 测试查询失败！");
			return false;
		}
		
		String sType = mp.get("module_id");
		_log.showDebug("=========TestConnPool.exeSQL 检查更新的值不正确！sType="+sType);		
		
		return true;
	}
}
