/**
 * 
 */
package org.jxstar.test.base;


/**
 * @author bingco 2009-6-16
 *
 * 测试线程，用于执行测试用例。
 */
public class TestThread extends Thread {
	private static int _doCount = 0;
	
	private int _checkTime = 200;
	private TestInf _testCase = null;
	
	public TestThread(String checkTime, String className) {
		if (checkTime == null || checkTime.length() == 0) {
			checkTime = "200";
		}
		_checkTime = Integer.parseInt(checkTime);
		
		if (className == null || className.length() == 0) {
			showLog("TestThread：创建测试用例的类名为空！");
			return;
		}
		
		//创建测试用例对象
		_testCase = createTestCase(className);
		if (_testCase == null) {
			showLog("TestThread：创建测试用例的对象失败！");
			return;
		}
		
		showLog("======已创建测试用例：" + _testCase.toString());
	}
	
	public void run() {	
		//String hashCode = Integer.toString(Thread.currentThread().hashCode());		
		//_log.showLog("======已创建线程号码：" + hashCode);
		
		while (true) {
			try {
				sleep(_checkTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}		
			
			_testCase.execute();
			_doCount++;
			//showLog("======执行任务总次数：" + _doCount);
		}
	}
	
	//创建测试用例对象	
	@SuppressWarnings("rawtypes")
	private TestInf createTestCase(String sClassName) {
		TestInf ret = null;	
		
			try {
				Class clazz = Class.forName(sClassName);

				ret = (TestInf) clazz.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				return ret;
			}
			
			return ret;			
	}
	
	private void showLog(String shint) {
		System.out.println(shint);
	}
}
