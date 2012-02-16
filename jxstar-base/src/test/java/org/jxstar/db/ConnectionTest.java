package org.jxstar.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.transaction.TransactionException;
import org.jxstar.dao.transaction.TransactionManager;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.log.Log;
import org.jxstar.util.system.SystemInitUtil;

public class ConnectionTest {
	private static int TNUM = 3;
	//private static Log _log = Log.getInstance();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		
		TransactionManager tranMng = (TransactionManager) SystemFactory
			.createSystemObject("TransactionManager");
		
		Connection conn = null;
		try {
			conn = tranMng.getTransactionObject().getConnection();
		} catch (TransactionException e1) {
			e1.printStackTrace();
		}
		try {
			System.out.println("---------------conn=" + conn.getTransactionIsolation());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*tranMng.startTran();
		try {		
			if (insert()) {
			//if (delete()) {
				tranMng.commitTran();
			} else {
				tranMng.rollbackTran();
			}
		} catch (TransactionException e) {
			e.printStackTrace();
		}	*/	

		for (int i = 1; i < 3; i++) {
			ConnThread thr = new ConnThread(i);
			thr.start();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static boolean testQuery() {
		BaseDao dao = BaseDao.getInstance();
		String sql = "select fun_base.fun_id, fun_base.module_id, fun_base.fun_name," + 
			"fun_base.fun_index, fun_base.reg_type, fun_base.subfun_id, fun_base.layout_page," + 
			"fun_base.grid_page, fun_base.form_page, fun_base.table_name, fun_base.pk_col," + 
			"fun_base.code_col, fun_base.code_prefix, fun_base.fk_col, fun_base.valid_flag," + 
			"fun_base.audit_col, fun_base.copy_col, fun_base.from_sql, fun_base.where_sql," + 
			"fun_base.order_sql, fun_base.group_sql, fun_base.is_userinfo, fun_base.is_archive," + 
			"fun_base.ds_name, fun_base.val_subid " + 
			"from fun_base where (reg_type < 'z')  order by fun_base.fun_index";
		
		DaoParam param = dao.createParam(sql);
		List<Map<String, String>> ls = dao.query(param);
		if (ls != null && !ls.isEmpty()) {
			for (int j = 0; j < ls.size(); j++) {
				Map<String, String> mp = ls.get(j);
				System.out.println(mp.toString());
			}
		}
		return true;
	}
	
	public static boolean query() {
		BaseDao dao = BaseDao.getInstance();
		
		for (int i = 1; i < TNUM; i++) {
			String sql = "select user_id, user_name from table" + i;
			DaoParam param = dao.createParam(sql);
			List<Map<String, String>> ls = dao.query(param);
			if (ls != null && !ls.isEmpty()) {
				for (int j = 0; j < ls.size(); j++) {
					Map<String, String> mp = ls.get(j);
					System.out.println(mp.toString());
				}
			}
			
		}
		
		return true;
	}		
	
	public static boolean delete() {
		BaseDao dao = BaseDao.getInstance();
		
		for (int i = 1; i < TNUM; i++) {
			String sql = "delete from table" + i;
			DaoParam param = dao.createParam(sql);
			if (!dao.update(param)) {
				return false;
			}
		}
		
		return true;
	}		
	
	public static boolean insert() {
		BaseDao dao = BaseDao.getInstance();
		
		for (int i = 1; i < TNUM; i++) {
			for (int j = 0; j < 10; j++) {
				String sql = "insert into table"+i+"(user_id, user_name) values ('1"+j+"', '东宏"+j+"')";
				DaoParam param = dao.createParam(sql);
				boolean bret = dao.update(param);
				if (!bret) return false;
			}			
		}
		
		return true;
	}	
	
	@SuppressWarnings("rawtypes")
	public static int dataNum(int index) {
		BaseDao dao = BaseDao.getInstance();
		String sql = "select * from table" + index;
		DaoParam param = dao.createParam(sql);
		List ls = dao.query(param);
		
		Map mp = (Map) ls.get(index*100-1);
		String sUserID = (String) mp.get("user_id");
		String sID = "1" + Integer.toString(index*100-1);
		if (! sUserID.equals(sID)) {
			System.out.println("indexthd = " + index + " sUserID = " + sUserID + " sID = " + sID);
		}
		
		return ls.size();
	}	
	
	public static void init() {
		//String realPath = "D:/workspace/03app/dhsdp";
		String sFileName = "conf/server.xml";
		
		//初始化日志对象
		Log.getInstance().init("conf/log.properties");
		SystemInitUtil.initSystem(sFileName, false);
	}
	
	public static boolean update() {
		BaseDao dao = BaseDao.getInstance();
		
		String sql = "update table1 set user_name = '11' where user_id = '10'";
		DaoParam param = dao.createParam(sql);
		return dao.update(param);
	}
}
