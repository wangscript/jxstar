package org.jxstar.task.load;


import org.jxstar.task.TaskException;
import org.jxstar.test.base.TestThread;
import org.jxstar.util.log.Log;
import org.jxstar.util.system.SystemInitUtil;

/**
 * 加载启动测试线程.
 */
public class SystemLoadTest {
	//private static Log _log = Log.getInstance();
	
	/**
	 * 构造函数
	 */
	public SystemLoadTest() {
		super();
	}
	
	/**
	 * 启动加载项
	 */
/*	public boolean load() {
		//是否启动测试
		String isRun = (String)this._initLoadParam.get("isRun");
		if (isRun == null || isRun.length() == 0 || !isRun.equals("true")) {
			_log.showLog("=======是否启动测试=" + isRun);
			return true;
		}
		
		//任务执行的间隔时间ms
		String checkTime = (String)this._initLoadParam.get("checkTime");		
		//需要启动的测试线程数
		String runThreadNum = (String)this._initLoadParam.get("runThreadNum");
		//需要执行测试用例类名
		String testClassName = (String)this._initLoadParam.get("testClassName");
		
		return load(checkTime, runThreadNum, testClassName);
	}*/
	
	public boolean load(String checkTime, String sThreadNum, String sClassName) 
		throws TaskException {
		if (checkTime == null || checkTime.length() == 0) {
			checkTime = "200";
		}
		
		if (sThreadNum == null || sThreadNum.length() == 0) {
			throw new TaskException("SystemLoadTest.load(): 参数[runThreadNum]未定义！");
		}
		int threadNum = Integer.parseInt(sThreadNum);
		
		//需要执行测试用例类名
		if (sClassName == null || sClassName.length() == 0) {
			throw new TaskException("SystemLoadTest.load(): 参数[testClassName]未定义！");
		}
		System.out.println("======启动的测试线程数=" + sThreadNum + " 测试类名=" + sClassName + " 间隔时间ms=" + checkTime);
		
		//启动测试线程
		for (int i = 0; i < threadNum; i++) {
			try { 
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
			
			//_log.showDebug("======已启动线程数：" + (i+1));				
			System.out.println("======已启动线程数：" + (i+1));
			TestThread tt = new TestThread(checkTime, sClassName);
			tt.start();
		}
		
		return true;
	}
	
	public static void main(String[] args) {
		//初始化环境对象
		init();
		
		SystemLoadTest test = new SystemLoadTest();
		try {
			test.load("200", "50", "org.jxstar.db.TestConnPoolError");
		} catch (TaskException e) {
			e.printStackTrace();
		}
	}
	
	public static void init() {
		//String realPath = "D:/Tomcat6/webapps/jxstar";
		String sFileName = "conf/server.xml";
		
		//初始化日志对象
		Log.getInstance().init("conf/log.properties");
		SystemInitUtil.initSystem(sFileName, false);
	}
}
