/**
 * 
 */
package org.jxstar.test.base;


import org.jxstar.dao.BaseDao;
import org.jxstar.dao.transaction.TransactionException;
import org.jxstar.dao.transaction.TransactionManager;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.log.Log;

/**
 * @author bingco 2009-6-16
 *
 * 测试用例的基础类
 */
public abstract class TestBase implements TestInf {
	protected static Log _log = Log.getInstance();
	protected static BaseDao _dao = BaseDao.getInstance();
	
	/**
	 * 测试用例要测试内容。
	 * @return boolean
	 */
	public boolean execute() {
		TransactionManager _tranMng = (TransactionManager) SystemFactory
			.createSystemObject("TransactionManager");
		
		try {
			_tranMng.startTran();
			
			boolean bret = exeTest();
			if (bret) {
				_tranMng.commitTran();
			}
			else {
				_tranMng.rollbackTran();
			}
		} catch (TransactionException e) {
			e.printStackTrace();
			
			try {
				_tranMng.rollbackTran();
			} catch (TransactionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return true;
	}
	
	/**
	 * 执行测试内容
	 */
	protected abstract boolean exeTest();
}
