package org.jxstar.db;

import java.util.List;
import java.util.Map;


import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.transaction.TransactionManager;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.log.Log;


public class ConnThread extends Thread {
	private static Log _log = Log.getInstance();
	//线程序号
	private int _indexthd = 0;
	
	public ConnThread(int index) {
		_indexthd = index;
		_log.showWarn("thread index " + _indexthd + " is created! ");
	}
	
	public void run() {		
		System.out.println("Thread.hashCode=" + Thread.currentThread().hashCode());
		
		//测试脏读
		/*if (_indexthd == 1) {
			zd_update("tzb");
		}
		if (_indexthd == 2) {
			zd_query("tzb");
		}*/
		
		//测试不可重复读
		/*if (_indexthd == 1) {
			kd_query("tzb");
			try {
				sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			kd_query("tzb");
		}
		if (_indexthd == 2) {
			kd_update("aaa");
		}*/
		
		//测试虚读
		if (_indexthd == 1) {
			xd_query();
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//xd_query();
		}
		if (_indexthd == 2) {
			xd_update();
		}
		
/*		TransactionManager tranMng = (TransactionManager) SystemFactory
			.createSystemObject("TransactionManager");
		tranMng.startTran();
		try {
			if (update(_indexthd)) {
				tranMng.commitTran();
			} else {
				tranMng.rollbackTran();
			}
		} catch (TransactionException e) {
			e.printStackTrace();
		}*/
/*		while(true) {
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//int index = (_indexthd%10 == 0) ? 10 : _indexthd%10;
//			int index = _indexthd%10;
//			int num = ConnectionTest.dataNum(index);
//			if (num != index*100) {
//				_log.showError("-------data num = " + num + " indexthd = " + _indexthd);
//			}
		}*/
	}
	
	/**
	 * 取一个表中的数据量
	 * @param index
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public int dataNum(int index) {
		BaseDao dao = BaseDao.getInstance();
		String sql = "select * from table" + index;
		DaoParam param = dao.createParam(sql);
		List ls = dao.query(param);
		
		/*Map mp = (Map) ls.get(index*100-1);
		String sUserID = (String) mp.get("user_id");
		String sID = "1" + Integer.toString(index*100-1);
		if (! sUserID.equals(sID)) {
			System.out.println("indexthd = " + _indexthd + " sUserID = " + sUserID + " sID = " + sID);
		}*/
		
		return ls.size();
	}
	
	/**
	 * 测试多线程的更新语句.
	 * @param index
	 * @return
	 */
	public boolean update(int index) {
		BaseDao dao = BaseDao.getInstance();
		
		int it = index*5;
		int max = 6;
		if (index == 1) max = 7;
		for (int i = 1; i < max; i++) {
			String sql = "delete from table" + (it+i);
			System.out.println("--------------" + sql);
			DaoParam param = dao.createParam(sql);
			if (!dao.update(param)) {
				return false;
			}
		}
		
		return true;
	}
	
	//是否为脏读测试：线程1调用
	public void zd_update(String value) {
		BaseDao dao = BaseDao.getInstance();
		TransactionManager tranMng = (TransactionManager) SystemFactory
			.createSystemObject("TransactionManager");
		
		String sql = "update table1 set user_name = '"+ value +"' where user_id = '10'";
		DaoParam param = dao.createParam(sql);
		tranMng.startTran();
		dao.update(param);
	}
	
	//是否为脏读测试：线程2调用
	public void zd_query(String value) {
		BaseDao dao = BaseDao.getInstance();
		String sql = "select user_name from table1 where user_id = '10'";
		DaoParam param = dao.createParam(sql);
		Map<String, String> mp = dao.queryMap(param);
		
		String user_name = mp.get("user_name");
		System.out.println("是否为脏读：" + user_name.equals(value) + " user_name=" + user_name + " value=" + value);
	}
	
	//是否为不可重复读测试：线程1调用一次后，执行线程2，再执行线程1
	public void kd_query(String value) {
		BaseDao dao = BaseDao.getInstance();
		String sql = "select user_name from table1 where user_id = '10'";
		DaoParam param = dao.createParam(sql);
		Map<String, String> mp = dao.queryMap(param);
		
		String user_name = mp.get("user_name");
		System.out.println("是否可重复读：" + user_name.equals(value) + " user_name=" + user_name + " value=" + value);
	}
	
	//是否为不可重复读测试：线程2调用
	public void kd_update(String value) {
		BaseDao dao = BaseDao.getInstance();
		
		String sql = "update table1 set user_name = '"+ value +"' where user_id = '10'";
		DaoParam param = dao.createParam(sql);
		dao.update(param);
	}		
	
	//是否为虚读测试：线程1调用
	public void xd_query() {
		BaseDao dao = BaseDao.getInstance();
		String sql = "select count(*) as cnt from table1";
		DaoParam param = dao.createParam(sql);
		Map<String, String> mp = dao.queryMap(param);
		
		String cnt = mp.get("cnt");
		System.out.println("是否虚读，记录数量：" + cnt);
		
		/*TransactionManager tranMng = (TransactionManager) SystemFactory
			.createSystemObject("TransactionManager");
		try {
			tranMng.commitTran();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}	
	
	//是否为虚读测试：线程2调用
	public void xd_update() {
		BaseDao dao = BaseDao.getInstance();
		
		String sql = "insert into table1(user_id, user_name) values ('122', '东宏aa')";
		DaoParam param = dao.createParam(sql);
		dao.update(param);
	}		
}
