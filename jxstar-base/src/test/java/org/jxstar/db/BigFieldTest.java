package org.jxstar.db;

import java.util.List;
import java.util.Map;


import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.util.BigFieldUtil;
import org.jxstar.service.define.DefineName;
import org.jxstar.test.AbstractTest;

public class BigFieldTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*String data = "sdfsf";

		String sql = "update fun_design set page_content = ? " +
		"where fun_id = 'sys_fun_base' and page_type = 'form' ";
		
		String sql1 = "update fun_dd set cc = ? where id = '1'";

		BigFieldUtil.updateStream(sql1, data, DefineName.DESIGN_NAME);
		
		String ssql = "select page_content from fun_design where fun_id = 'sys_fun_base' and page_type = 'grid'";
		String ss = BigFieldUtil.readStream(ssql, "page_content", DefineName.DESIGN_NAME);
		System.out.println(ss);*/
		
		tran();
	}

	@SuppressWarnings("rawtypes")
	private static void tran() {
		BaseDao _dao = BaseDao.getInstance();
		
		DaoParam param = new DaoParam();
		param.setSql("select design_id from fun_design");
		List ls = _dao.query(param);
		
		for (int i = 0; i < ls.size(); i++) {
			Map mp = (Map)ls.get(i);
			
			String id = (String) mp.get("design_id");
			
			String sql1 = "select page_content1 from fun_design where design_id = '"+id+"'";
			String cont = BigFieldUtil.readStream(sql1, "page_content1", DefineName.DESIGN_NAME);
			
			String usql = "update fun_design set page_content = ? where design_id = '"+id+"'";
			BigFieldUtil.updateStream(usql, cont, DefineName.DESIGN_NAME);
		}
	}
}
