package org.jxstar.service.studio;

import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.service.event.LoginEvent;
import org.jxstar.util.MapUtil;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 扩展登录事件，把实施单位信息写到会话中
 *
 * @author TonyTan
 * @version 1.0, 2011-11-4
 */
public class MyLoginEvent extends LoginEvent {
	private static final long serialVersionUID = 1L;

	/**
	 * 扩展会话中的返回信息
	 * @param mpUser
	 * @return
	 */
	protected Map<String,String> returnData(final Map<String,String> mpUser) {
		Map<String,String> retData = FactoryUtil.newMap();
		
		String deptId = MapUtil.getValue(mpUser, "dept_id");
		if (deptId.length() < 4) return retData;
		
		String rootid = deptId.substring(0, 4);
		//如果是本部单位，则直接取当前部门
		if (rootid.equals("1001")) {
			retData.put("orgid", mpUser.get("dept_id"));
			retData.put("orgcode", mpUser.get("dept_code"));
			retData.put("orgname", mpUser.get("dept_name"));
		} else {
			String sql = "select dept_id, dept_code, dept_name from sys_dept where dept_id = ?";
			DaoParam param = _dao.createParam(sql);
			param.addStringValue(rootid);
			Map<String,String> mpData = _dao.queryMap(param);
			
			retData.put("orgid", MapUtil.getValue(mpData, "dept_id"));
			retData.put("orgcode", MapUtil.getValue(mpData, "dept_code"));
			retData.put("orgname", MapUtil.getValue(mpData, "dept_name"));
		}
		
		return retData;
	}
}
